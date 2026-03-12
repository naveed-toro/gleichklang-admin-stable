package de.binaerebauten.gleichklang.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;

/**
 * Created by michael on 23/04/15.
 */
public class XMLParser
{
	private static final Logger LOG = LoggerFactory.getLogger(XMLParser.class);

	public static <T> T unmarshalXML(Class<T> aClass, InputStream inputStream)
	{
		JAXBContext jaxbContext;
		try
		{
			jaxbContext = JAXBContext.newInstance(aClass);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

			return (T) unmarshaller.unmarshal(inputStream);
		}
		catch (JAXBException e)
		{
			LOG.error("Could not unmarshal XML", e);
		}
		return null;
	}
}
