package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.ui.CustomComponent;
import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanItemsHandler;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.SizeChangeListener;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.metamodel.SingularAttribute;
import java.util.*;
import java.util.function.Function;

public abstract class LazyBeanItemComponent<BEANTYPE extends BaseEntity> extends CustomComponent
{
	public interface ValueChangeListener<BEANTYPE extends BaseEntity>
	{
		void valueChange(Set<BEANTYPE> values);
	}
	
	public interface ColumnGenerator<BEANTYPE extends BaseEntity>
	{
		Object generateCell(LazyBeanItemComponent<BEANTYPE> source, BEANTYPE itemId, Object columnId);
	}
	
	public interface CellStyleGenerator<BEANTYPE extends BaseEntity>
	{
		CssStyle getStyle(LazyBeanItemComponent<BEANTYPE> source, BEANTYPE itemId, Object propertyId);
	}
	
	public interface ItemClickListener<BEANTYPE extends BaseEntity>
	{
		void itemClick(BEANTYPE itemId);
	}
	
	protected final Collection<Specification<BEANTYPE>> specifications = new HashSet<>();
	private final Collection<ValueChangeListener<BEANTYPE>> valueChangeListeners = new HashSet<>();
	private final Collection<ItemClickListener<BEANTYPE>> itemClickListeners = new HashSet<>();
	private final Collection<ItemClickListener<BEANTYPE>> itemDoubleClickListeners = new HashSet<>();
	private LazyBeanFilteredItemsHandler<BEANTYPE> filteredHandler = null;
	private final List<SizeChangeListener> sizeChangeListeners = new ArrayList<>();
	private int size = 0;
	
	public abstract void refresh();
	
	/**
	 * Remove all existing filters and add a new one. This simplify the
	 * replacing of a single filter. The container will be refreshed after
	 * setting the filter
	 *
	 * @param specification
	 * @see <a href="http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#specifications">Spring
	 * Data - Specifications</a>
	 */
	public void setFilter(Specification<BEANTYPE> specification)
	{
		if (specifications.isEmpty() && specification == null) return;
		if (specifications.size() == 1 && specifications.contains(specification))
			return;
		
		specifications.clear();
		if (specification != null)
		{
			specifications.add(specification);
		}
		
		clearValue();
		refresh();
	}
	
	/**
	 * Replace a filter with another. So only one container refresh is necessary
	 * and will proceed automatically
	 *
	 * @param oldSpecification
	 * @param newSpecification
	 * @see <a href="http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#specifications">Spring
	 * Data - Specifications</a>
	 */
	public void replaceFilter(Specification<BEANTYPE> oldSpecification, Specification<BEANTYPE> newSpecification)
	{
		if (Objects.equals(oldSpecification, newSpecification)) return;
		
		boolean change = false;
		
		if (oldSpecification != null)
		{
			change = specifications.remove(oldSpecification);
		}
		if (newSpecification != null)
		{
			if (!specifications.contains(newSpecification))
			{
				change = true;
				specifications.add(newSpecification);
			}
		}
		
		if (change)
		{
			clearValue();
			refresh();
		}
	}
	
	/**
	 * Add a filter and refresh the container
	 *
	 * @param specification
	 * @see <a href="http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#specifications">Spring
	 * Data - Specifications</a>
	 */
	public void addFilter(Specification<BEANTYPE> specification)
	{
		if (specification == null) return;
		
		if (specifications.add(specification))
		{
			clearValue();
			refresh();
		}
	}
	
	/**
	 * Remove a filter and refresh the container
	 *
	 * @param specification
	 */
	public void removeFilter(Specification<BEANTYPE> specification)
	{
		if (specification == null) return;
		
		if (specifications.remove(specification))
		{
			clearValue();
			refresh();
		}
	}
	
	/**
	 * Remove all filter and refresh the container
	 */
	public void removeAllFilters()
	{
		if (!specifications.isEmpty())
		{
			specifications.clear();
			clearValue();
			refresh();
		}
	}
	
	public LazyBeanFilteredItemsHandler<BEANTYPE> getHandler()
	{
		return this.filteredHandler;
	}
	
	/**
	 * Sets the handler for getting the list lazily. If set to <code>null</code>
	 * the content will be cleared.
	 *
	 * @param handler
	 */
	public void setHandler(LazyBeanItemsHandler<BEANTYPE> handler)
	{
		this.filteredHandler = handler == null ? null : (specification, pageable) -> handler.getItems(pageable);
		refresh();
	}
	
	public Specification<BEANTYPE> getSpecification()
	{
		return LazyBeanItemContainer.getSpecification(specifications);
	}
	
	/**
	 * Sets the handler for getting the filtered list lazily. If set to
	 * <code>null</code> the content will be cleared.
	 *
	 * @param filteredHandler
	 * @see <a href="http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#specifications">Spring
	 * Data - Specifications</a>
	 */
	public void setHandler(LazyBeanFilteredItemsHandler<BEANTYPE> filteredHandler)
	{
		this.filteredHandler = filteredHandler;
		refresh();
	}
	
	/**
	 * Register a new {@link ValueChangeListener}. The listener will return the
	 * new values (selection).
	 */
	public void addValueChangeListener(ValueChangeListener<BEANTYPE> listener)
	{
		valueChangeListeners.add(listener);
	}
	
	public void removeValueChangeListener(ValueChangeListener<BEANTYPE> listener)
	{
		valueChangeListeners.remove(listener);
	}
	
	protected void fireValueChangeEvent()
	{
		final Set<BEANTYPE> values = getValues();
		valueChangeListeners.forEach(l -> l.valueChange(values));
	}
	
	/**
	 * Gets the selected beans
	 *
	 * @return
	 */
	public abstract Set<BEANTYPE> getValues();
	
	/**
	 * Gets the selected bean (in multiselect mode this function fail)
	 *
	 * @return
	 */
	public abstract BEANTYPE getValue();
	
	public abstract void clearValue();
	
	/**
	 * Setter for property selectable. <p> <p> The table is not selectable by
	 * default. </p>
	 *
	 * @param selectable the New value of property selectable.
	 */
	public abstract void setSelectable(boolean selectable);
	
	/**
	 * Sets the multiselect mode. Setting multiselect mode false may lose
	 * selection information: if selected items set contains one or more
	 * selected items, only one of the selected items is kept as selected.
	 * <p>
	 * Subclasses of AbstractSelect can choose not to support changing the
	 * multiselect mode, and may throw {@link UnsupportedOperationException}.
	 *
	 * @param multiSelect the New value of property multiSelect.
	 */
	public abstract void setMultiSelect(boolean multiSelect);
	
	/**
	 * Adds a generated column to the Table. <p> A generated column is a column
	 * that exists only in the Table, not as a property in the underlying
	 * Container. It shows up just as a regular column. </p> <p> A generated
	 * column will override a property with the same id, so that the generated
	 * column is shown instead of the column representing the property. Note
	 * that getContainerProperty() will still get the real property. </p> <p>
	 * Table will not listen to value change events from properties overridden
	 * by generated columns. If the content of your generated column depends on
	 * properties that are not directly visible in the table, attach value
	 * change listener to update the content on all depended properties.
	 * Otherwise your UI might not get updated as expected. </p> <p> Also note
	 * that getVisibleColumns() will return the generated columns, while
	 * getContainerPropertyIds() will not. </p>
	 *
	 * @param id              the id of the column to be added
	 * @param generatedColumn the {@link ColumnGenerator} to use for this
	 *                        column
	 */
	public abstract void addGeneratedColumn(String header, Object id, ColumnGenerator<BEANTYPE> generatedColumn);
	
	public void addGeneratedColumn(Object id, ColumnGenerator<BEANTYPE> generatedColumn)
	{
		String header = null;
		if(id instanceof String)
		{
			header = (String) id;
		}
		addGeneratedColumn(header, id, generatedColumn);
	}
	
	/**
	 * Adds a generated column to the Table. <p> A generated column is a column
	 * that exists only in the Table, not as a property in the underlying
	 * Container. It shows up just as a regular column. </p> <p> A generated
	 * column will override a property with the same id, so that the generated
	 * column is shown instead of the column representing the property. Note
	 * that getContainerProperty() will still get the real property. </p> <p>
	 * Table will not listen to value change events from properties overridden
	 * by generated columns. If the content of your generated column depends on
	 * properties that are not directly visible in the table, attach value
	 * change listener to update the content on all depended properties.
	 * Otherwise your UI might not get updated as expected. </p> <p> Also note
	 * that getVisibleColumns() will return the generated columns, while
	 * getContainerPropertyIds() will not. </p>
	 *
	 * @param id              the id of the column to be added
	 * @param generatedColumn the {@link Function <BEANTYPE, ?>}  to generate the
	 *                        column from the bean.
	 */
	public void addGeneratedColumn(Object id, Function<BEANTYPE, ?> generatedColumn)
	{
		String header = null;
		if(id instanceof String)
		{
			header = (String) id;
		}
		addGeneratedColumn(header, id, (source, itemId, columnId) -> generatedColumn.apply(itemId));
	}
	
	public void addGeneratedColumn(String header, Object id, Function<BEANTYPE, ?> generatedColumn)
	{
		addGeneratedColumn(header, id, (source, itemId, columnId) -> generatedColumn.apply(itemId));
	}
	
	/**
	 * Sets the currently sorted property id.
	 *
	 * @param ascending  <code>true</code> if ascending, <code>false</code> if
	 *                   descending.
	 * @param propertyId the currently sorted property id
	 */
	public abstract void setSortPropertyId(boolean ascending, SingularAttribute<?, ?>... propertyId);
	
	/**
	 * Sets the currently sorted column property id.
	 *
	 * @param propertyId the currently sorted property id
	 */
	public void setSortPropertyId(SingularAttribute<?, ?>... propertyId)
	{
		setSortPropertyId(true, propertyId);
	}
	
	public abstract void setColumnHeaderVisible(boolean visible);
	
	/**
	 * Set cell style generator for Table.
	 *
	 * @param cellStyleGenerator New cell style generator or null to remove
	 *                           generator.
	 */
	public abstract void setCellStyleGenerator(CellStyleGenerator<BEANTYPE> cellStyleGenerator);
	
	public void addItemClickListener(ItemClickListener<BEANTYPE> listener)
	{
		addItemClickListener(listener, false);
	}
	
	public void addItemClickListener(ItemClickListener<BEANTYPE> listener, boolean onlyDoubleClicked)
	{
		if(onlyDoubleClicked) itemDoubleClickListeners.add(listener);
		else itemClickListeners.add(listener);
	}
	
	public void removeItemClickListener(ItemClickListener<BEANTYPE> listener)
	{
		itemClickListeners.remove(listener);
		itemDoubleClickListeners.remove(listener);
	}
	
	protected void fireItemClickedEvent(BEANTYPE itemId, boolean doubleClicked)
	{
		itemClickListeners.forEach(l -> l.itemClick(itemId));
		if(doubleClicked) itemDoubleClickListeners.forEach(l -> l.itemClick(itemId));
	}
	
	public void addSizeChangedListener(SizeChangeListener sizeChangeListener)
	{
		Objects.requireNonNull(sizeChangeListener);
		sizeChangeListeners.add(sizeChangeListener);
	}
	
	protected void onSizeChanged(int size)
	{
		if(size == this.size) return;
		
		this.size = size;
		sizeChangeListeners.forEach(l -> l.onSizeChanged(size));
	}
	
	public int getSize()
	{
		return size;
	}
	
	/**
	 * Returns the values as map with the index in the table as key
	 *
	 * @return
	 */
	abstract Map<Integer, BEANTYPE> getValuesWithIndex();
	
	abstract void setValue(Map<Integer, BEANTYPE> valuesWithIndex);
	
	abstract void setSortableByMoveHandler(boolean enabled);
}