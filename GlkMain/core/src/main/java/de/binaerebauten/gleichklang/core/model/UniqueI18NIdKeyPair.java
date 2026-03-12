package de.binaerebauten.gleichklang.core.model;

/**
 * Project: serialization
 * Created by Domi on 27.05.2016.
 */
public class UniqueI18NIdKeyPair implements IdKeyPairResult {
    private I18NEntity.BaseName baseName;
    private I18NEntity.Language language;
    private String keyValue;
    private Long idValue;

    public UniqueI18NIdKeyPair(I18NEntity.BaseName baseName, Long idValue, String keyValue, I18NEntity.Language language) {
        this.baseName = baseName;
        this.idValue = idValue;
        this.keyValue = keyValue;
        this.language = language;
    }

    public I18NEntity.BaseName getBaseName() {
        return baseName;
    }

    public I18NEntity.Language getLanguage() {
        return language;
    }

    public Long getIdValue() {
        return idValue;
    }

    public String getKeyValue() {
        return baseName + ":" + language + ":" + keyValue;
    }

    @Override
    public Class getClassType() {
        return I18NEntity.class;
    }
}
