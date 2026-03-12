package de.binaerebauten.gleichklang.core.model.cache;


import javax.persistence.Column;

/**
 * Created by Domi on 04.10.2016.
 */
public class TextCachedValue extends CachedValue<String> {

    @Column(name = "text_value")
    private String textValue;

    public TextCachedValue() {
    }

    public TextCachedValue(String cacheKey, String textValue) {
        this.textValue = textValue;
        setCacheKey(cacheKey);
    }

    @Override
    public String getValue() {
        return this.textValue;
    }

    @Override
    public void setValue(String value) {
        this.textValue = value;
    }
}
