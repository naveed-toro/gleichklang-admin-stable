package de.binaerebauten.gleichklang.core.model;

/**
 * Project: Import Export
 * Created by Domi on 01.07.2016.
 */
public class TranslationIdentifier {

    private String baseName;
    private String oldKey;
    private String newKey;

    public TranslationIdentifier(String baseName, String oldKey, String newKey) {
        this.baseName = baseName;
        this.newKey = newKey;
        this.oldKey = oldKey;
    }

    public String getBaseName() {
        return baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public String getNewKey() {
        return newKey;
    }

    public void setNewKey(String newKey) {
        this.newKey = newKey;
    }

    public String getOldKey() {
        return oldKey;
    }

    public void setOldKey(String oldKey) {
        this.oldKey = oldKey;
    }
}
