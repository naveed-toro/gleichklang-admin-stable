package de.binaerebauten.gleichklang.core.utils;

import javax.persistence.metamodel.Attribute;

public class PropertyPathBuilder
{
	private PropertyPathBuilder()
	{}

	public static String getFieldName(Attribute<?, ?>... fields)
	{
		String fieldName = "";
		for (final Attribute<?, ?> field : fields)
		{
			fieldName += field.getName();
			fieldName += ".";
		}

		return fieldName.length() > 0 ? fieldName.substring(0, fieldName.length() - 1) : fieldName;
	}
}
