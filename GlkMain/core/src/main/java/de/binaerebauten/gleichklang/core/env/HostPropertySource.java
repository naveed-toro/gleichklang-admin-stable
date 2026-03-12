package de.binaerebauten.gleichklang.core.env;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyAccessorUtils;
import org.springframework.core.env.EnumerablePropertySource;

import java.beans.PropertyDescriptor;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This property source provides access to a {@link InetAddress#getLocalHost()} object
 * under the property 'host'. This allows using the host in property place holders in
 * .properties files.
 */
public class HostPropertySource extends EnumerablePropertySource<InetAddress>
{
	private final static String HOST_PROPERTY_NAME = "host";

	private final BeanWrapper beanWrapper;

	private final String[] propertyNames;

	public HostPropertySource(String name) throws UnknownHostException
	{
		super(name, InetAddress.getLocalHost());

		beanWrapper = new BeanWrapperImpl(source);
		propertyNames = initPropertyNames();
	}

	@Override
	public String[] getPropertyNames()
	{
		return propertyNames;
	}

	@Override
	public Object getProperty(String name)
	{
		if (name.startsWith(HOST_PROPERTY_NAME))
		{
			int firstNestedPropertySeparatorIndex =
					PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex(name);
			boolean isNestedProperty = firstNestedPropertySeparatorIndex > 0
					&& firstNestedPropertySeparatorIndex + 1 < name.length();

			if (isNestedProperty)
			{
				String propertyName = name.substring(firstNestedPropertySeparatorIndex + 1);
				Object propertyValue = beanWrapper.getPropertyValue(propertyName);

				return propertyValue;
			}
		}
		return null;
	}

	private String[] initPropertyNames()
	{
		PropertyDescriptor[] propertyDescriptors = beanWrapper.getPropertyDescriptors();
		String[] result = new String[propertyDescriptors.length];

		for (int i = 0; i < propertyDescriptors.length; i++)
		{
			result[i] = HOST_PROPERTY_NAME + "." + propertyDescriptors[i].getName();
		}
		return result;
	}
}
