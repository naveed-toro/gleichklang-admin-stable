package de.binaerebauten.gleichklang.core.utils;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;

public class ParametersHolder
{
	public enum ParameterKey
	{
		EMAIL,
		MESSAGE,
		DEBUG,
		CONFIRM,
		UNSUBSCRIBED,
		URL,
		PASSWORD,
		REGISTER,
		FORGOT_PASSWORD;
		
		public String getValue()
		{
			return name().toLowerCase();
		}
	}
	
	private final Map<String, String[]> parametersMap;
	
	public ParametersHolder(Map<String, String[]> parametersMap)
	{
		this.parametersMap = new HashMap<>(parametersMap);
	}
	
	public ParametersHolder()
	{
		this.parametersMap = new HashMap<>();
	}
	
	public String getFirstParameter(ParameterKey key)
	{
		return getFirstParameter(key.getValue());
	}
	
	private String getFirstParameter(String key)
	{
		final String[] values = this.parametersMap.get(key);
		if (values != null && values.length > 0)
		{
			return values[0];
		}
		
		return null;
	}
	
	public boolean hasParameter(ParameterKey key)
	{
		return this.parametersMap.containsKey(key.getValue());
	}
	
	private void addParameter(String key)
	{
		this.parametersMap.put(key, null);
	}
	
	private void addParameter(String key, String value)
	{
		this.parametersMap.put(key, new String[] { value });
	}
	
	public void addParameter(ParameterKey key, String value)
	{
		addParameter(key.getValue(), value);
	}
	
	public void addParameter(ParameterKey key)
	{
		addParameter(key.getValue());
	}
	
	public MultiValueMap<String, String> getMultiMap()
	{
		final MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		for(Map.Entry<String, String[]> entry : parametersMap.entrySet())
		{
			final List<String> valueList = entry.getValue() != null ? Arrays.asList(entry.getValue()) : Collections.emptyList();
			map.put(entry.getKey(), valueList);
		}
		
		return map;
	}
}
