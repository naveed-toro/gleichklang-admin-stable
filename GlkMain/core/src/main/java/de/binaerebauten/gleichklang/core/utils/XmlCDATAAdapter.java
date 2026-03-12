package de.binaerebauten.gleichklang.core.utils;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Project: gleichklang-parent
 * Created by Domi on 19.05.2016.
 */
public class XmlCDATAAdapter extends XmlAdapter<String, String> {

    private static final String CDATA_BEGIN = "<![CDATA[";
    private static final String CDATA_END = "]]>";

    @Override
    public String unmarshal(String value) throws Exception {
        return value;
    }

    @Override
    public String marshal(String value) throws Exception {
        return CDATA_BEGIN + value + CDATA_END;
    }
}
