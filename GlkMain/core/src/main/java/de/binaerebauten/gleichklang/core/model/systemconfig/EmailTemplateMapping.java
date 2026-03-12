package de.binaerebauten.gleichklang.core.model.systemconfig;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.DeletableEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "email_template_mapping")
public class EmailTemplateMapping extends BaseEntity implements DeletableEntity<Long>
{

    @Column(name = "id", nullable = false , updatable = false, insertable = false)
    private Long  id;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "template_name", nullable = false)
    private String templateName;

    public Long getTemplateFooter() {
        return templateFooter;
    }

    public void setTemplateFooter(Long templateFooter) {
        this.templateFooter = templateFooter;
    }

    @Column(name = "template_footer", nullable = true)
    private Long templateFooter;

    public String getTemplateDescription() {
        return templateDescription;
    }

    private void setTemplateDescription(String templateDescription) {
        this.templateDescription = templateDescription;
    }

    @Column(name = "template_description", nullable = false)
    private String templateDescription;

    public String getTemplateLanguage() {
        return templateLanguage;
    }

    public void setTemplateLanguage(String templateLanguage) {
        this.templateLanguage = templateLanguage;
    }

    @Column(name = "template_language", nullable = false)
    private String templateLanguage;

    @Column(name = "deleted", nullable = true)
    private boolean deleted;

    @Column(name = "active", nullable = true)
    private boolean active;


    @Column(name = "template_text", nullable = false)
    private String templateText;


    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getTemplateText() {
        return templateText;
    }

    private void setTemplateText(String templateText) {
        this.templateText = templateText;
    }
}