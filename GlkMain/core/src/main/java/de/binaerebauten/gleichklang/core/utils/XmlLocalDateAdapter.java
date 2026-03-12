package de.binaerebauten.gleichklang.core.utils;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * XML Adapter for converting marshaling/unmarshaling LocalDate.
 */
public class XmlLocalDateAdapter extends XmlAdapter<String, LocalDate> {

    /**
     * Converts given Date String into LocalDate object.
     *
     * @param value XML DateTime Value
     * @return unmarshaled date object
     * @throws Exception
     */
    @Override
    public LocalDate unmarshal(String value) throws Exception {
        return LocalDate.parse(value);
    }

    /**
     * Converts a given LocalDate to String.
     *
     * If the value is null LocalDate is set to 1.1.1970.
     *
     * @param value local date object
     * @return marshaled Date object
     * @throws Exception
     */
    @Override
    public String marshal(LocalDate value) throws Exception {
        if (value == null) return LocalDate.of(1970,1,1).toString();
        return value.toString();
    }
}
