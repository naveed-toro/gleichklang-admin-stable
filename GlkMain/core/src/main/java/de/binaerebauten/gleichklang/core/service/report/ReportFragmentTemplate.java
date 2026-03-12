package de.binaerebauten.gleichklang.core.service.report;

import de.binaerebauten.gleichklang.core.service.template.VelocityTemplate;

/**
 * Enum for identifying the report templates
 */
public enum ReportFragmentTemplate implements VelocityTemplate {

    INTRODUCTION("introduction.html.vm"),

    DESCRIPTION_PERSOENLICH("description_persoenlichkeit.html.vm"),
    DESCRIPRION_PERSOENLICH_DETAILS("attributes_details_persoenlichkeit.html.vm"),

    DESCRIPTION_GESELLSCHAFT("description_gesellschaft.html.vm"),
    DESCRIPRION_GESELLSCHAFT_DETAILS("attributes_details_gesellschaft.html.vm"),

    DESCRIPTION_PARTNERSCHAFT("description_partnerschaft.html.vm"),
    DESCRIPRION_PARTNERSCHAFT_DETAILS("attributes_details_partnerschaft.html.vm"),

    DESCRITPION_FREUNDSCHADT("description_freundschaft.html.vm"),
    DESCRIPRION_FREUNDSCHAFT_DETAILS("attributes_details_freundschaft.html.vm");

    private final String templateName;

    private static final String REPORT_FRAGMENT_BASE_LOCATION = "report/templates";


    ReportFragmentTemplate(String templateName) {
        this.templateName = templateName;
    }

    @Override
    public String getTemplateName() {
        return templateName;
    }

    @Override
    public String getBaseLocation() {
        return REPORT_FRAGMENT_BASE_LOCATION;
    }


}
