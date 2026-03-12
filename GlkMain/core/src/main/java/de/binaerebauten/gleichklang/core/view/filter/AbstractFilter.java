package de.binaerebauten.gleichklang.core.view.filter;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemComponent;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.SingularAttribute;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractFilter<T extends BaseEntity, V, A> implements Specification<T>
{
	public interface ValueChangeListener<V>
	{
		void valueChange(V value);
	}
	
	private final Set<ValueChangeListener<V>> valueChangeListeners = new HashSet<>();
	private LazyBeanItemComponent<T> itemComponent = null;
	private V value = null;
	private final List<SingularAttribute<? super T, A>> attributes = new ArrayList<>();
	
	public AbstractFilter()
	{
	}
	
	public AbstractFilter(SingularAttribute<? super T, A> attribute)
	{
		Objects.requireNonNull(attribute);
		
		this.attributes.add(attribute);
	}
	
	public AbstractFilter(Collection<SingularAttribute<? super T, A>> attributes)
	{
		Objects.requireNonNull(attributes);
		
		this.attributes.addAll(attributes);
	}
	
	public V getValue()
	{
		return value;
	}
	
	public void setValue(V value)
	{
		final boolean dirty = !Objects.equals(this.value, value);
		
		this.value = value;
		
		if (dirty)
		{
			onValueChanged();
		}
	}
	
	protected void onValueChanged()
	{
		if(itemComponent != null) itemComponent.refresh();
		valueChangeListeners.forEach(valueChangeListener -> valueChangeListener.valueChange(value));
	}
	
	public void setItemComponent(LazyBeanItemComponent<T> itemComponent)
	{
		if (Objects.equals(this.itemComponent, itemComponent)) return;
		
		if (this.itemComponent != null) this.itemComponent.removeFilter(this);
		if (itemComponent != null) itemComponent.addFilter(this);
		
		this.itemComponent = itemComponent;
	}
	
	protected Collection<Path<A>> getSimplePath(Root<T> root)
	{
		return attributes.stream().map(root::get).collect(Collectors.toList());
	}
	
	public void addValueChangeListener(ValueChangeListener<V> valueChangeListener)
	{
		valueChangeListeners.add(valueChangeListener);
	}
	
	public void removeValueChangeListener(ValueChangeListener<V> valueChangeListener)
	{
		valueChangeListeners.remove(valueChangeListener);
	}
	
	public void removeAllValueChangeListener()
	{
		valueChangeListeners.clear();
	}
	
	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb)
	{
		if (getValue() == null) return null;
		
		return getSimplePath(root).stream().map(path -> toPredicate(path, query, cb)).filter(Objects::nonNull).reduce(cb::or).orElse(null);
	}
	
	protected abstract Predicate toPredicate(Path<A> path, CriteriaQuery<?> query, CriteriaBuilder cb);
}
