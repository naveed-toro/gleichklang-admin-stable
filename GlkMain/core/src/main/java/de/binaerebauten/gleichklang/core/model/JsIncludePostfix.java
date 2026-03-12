package de.binaerebauten.gleichklang.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "js_include_postfix")
public class JsIncludePostfix extends BaseEntity{

    @Column(name = "key_name")
    private String keyName;

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }
}
