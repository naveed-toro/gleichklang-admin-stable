package de.binaerebauten.gleichklang.core.model;

/**
 * Project: serialization
 * Created by Domi on 27.05.2016.
 */
public class IdKeyPair implements IdKeyPairResult {
    private Long idValue;
    private String keyValue;
    private Class clazz;

    public IdKeyPair(Long idValue, String keyValue, Class clazz) {
        this.idValue = idValue;
        this.keyValue = keyValue;
        this.clazz = clazz;
    }

    public IdKeyPair(Long idValue, String keyValue) {
        this.idValue = idValue;
        this.keyValue = keyValue;
    }

    public Long getIdValue() {
        return idValue;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public Class getClassType() {
        return clazz;
    }
}
