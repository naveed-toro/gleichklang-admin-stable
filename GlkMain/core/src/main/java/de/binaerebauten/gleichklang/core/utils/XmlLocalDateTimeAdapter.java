package de.binaerebauten.gleichklang.core.utils;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;

/**
 * XML Adapter for converting marshaling/unmarshaling LocalDateTime.
 */
public class XmlLocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

    /**
     * Converts given DateTime String into LocalDateTime object.
     *
     * @param value XML DateTime Value
     * @return unmarshaled date time object
     * @throws Exception
     */
    @Override
    public LocalDateTime unmarshal(String value) throws Exception {
        return LocalDateTime.parse(value);
    }

    /**
     * Converts a given LocalDateTime to String.
     *
     * If the value is null LocalDateTime is set to 1.1.1970 00:00:00.
     *
     * @param value local date time object
     * @return marshaled DateTime object
     * @throws Exception
     */
    @Override
    public String marshal(LocalDateTime value) throws Exception {
        if (value == null) return null;
        return value.toString();
    }
}
