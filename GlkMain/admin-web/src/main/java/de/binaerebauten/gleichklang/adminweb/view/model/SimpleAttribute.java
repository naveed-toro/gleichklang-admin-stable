package de.binaerebauten.gleichklang.adminweb.view.model;

import org.hibernate.jpa.internal.metamodel.AbstractAttribute;

public class SimpleAttribute extends AbstractAttribute<Object, Object>
{
	public SimpleAttribute(String name)
	{
		super(name, null, null, null, null);
	}

	@Override
	public boolean isAssociation()
	{
		return false;
	}

	@Override
	public boolean isCollection()
	{
		return false;
	}
}
