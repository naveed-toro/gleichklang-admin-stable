package de.binaerebauten.gleichklang.core.model.filter;

import de.binaerebauten.gleichklang.core.model.DeletableEntity;
import de.binaerebauten.gleichklang.core.utils.filter.FilterVisitor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.*;
import java.util.HashSet;
import java.util.Set;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class TemplateFilter extends AbstractFilter implements DeletableEntity<Long>, Filterable
{
	@XmlAttribute
	@Column
	@NotNull
	private boolean deleted = false;

	@XmlAttribute
	@Column(name = "template_name")
	@NotNull
	private String name;

	@XmlIDREF
	@ManyToOne
	@JoinColumn(name = "template_filter_id")
	@NotNull
	private AbstractFilter filter;

	@XmlElementWrapper
	@XmlElement(name = "templateContext")
	@ElementCollection(targetClass = TemplateContext.class, fetch = FetchType.EAGER)
	@CollectionTable(name = "template_context", joinColumns = @JoinColumn(name = "template_filter_id"))
	@Column(name = "template_context")
	@Enumerated(EnumType.STRING)
	private Set<TemplateContext> templateContexts = new HashSet<>();

	@Override
	public AbstractFilter getFilter()
	{
		return filter;
	}

	@Override
	public void setFilter(AbstractFilter filter)
	{
		this.filter = filter;
	}

	public String getName()
	{
		return (name == null ? "" : name) + (deleted ? " (gelöscht)" : "");
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public <T> T accept(FilterVisitor<T> filterVisitor)
	{
		return filterVisitor.visit(this);
	}

	@Override
	public boolean isDeleted()
	{
		return deleted;
	}

	@Override
	public void setDeleted(boolean deleted)
	{
		this.deleted = deleted;
	}

	public Set<TemplateContext> getTemplateContexts()
	{
		return templateContexts;
	}

	public void setTemplateContexts(Set<TemplateContext> templateContexts)
	{
		this.templateContexts = templateContexts;
	}
}
