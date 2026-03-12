package de.binaerebauten.gleichklang.adminweb.service.serialization;

import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.filter.TemplateFilter;
import de.binaerebauten.gleichklang.core.model.locatable.LocatableEntity;
import de.binaerebauten.gleichklang.core.model.matching.AbstractQuestionsMapping;
import de.binaerebauten.gleichklang.core.model.matching.Activator;
import de.binaerebauten.gleichklang.core.model.matching.I18N;
import de.binaerebauten.gleichklang.core.model.matching.MatchingMatrix;
import de.binaerebauten.gleichklang.core.model.payment.Product;
import de.binaerebauten.gleichklang.core.model.questionnaire.ChoiceGroup;
import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import de.binaerebauten.gleichklang.core.monitoring.BuildInfo;

import javax.xml.bind.annotation.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Project: Import Export
 * Created by Domi on 09.06.2016.
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "gleichklang")
public class SerializationContainer {

    @XmlElementWrapper
    @XmlElement(name = "questionnaire")
    private Set<Questionnaire> questionnaires = null;

    @XmlElement(name = "choiceGroup")
    @XmlElementWrapper
    private Set<ChoiceGroup> choiceGroups = null;

    @XmlElement(name = "matchingMatrix")
    @XmlElementWrapper
    private Set<MatchingMatrix> matchingMatrices = null;

    @XmlElement(name = "questionMapping")
    @XmlElementWrapper
    private Set<AbstractQuestionsMapping> questionMappings = null;

    @XmlElementWrapper
    @XmlElement(name = "i18nEntity")
    private Set<I18NEntity> translations = null;

    @XmlElementWrapper
    @XmlElement(name = "product")
    private Set<Product> products = null;

    @XmlElementWrapper
    @XmlElement(name = "activator")
    private Set<Activator> activators = null;

    @XmlElementWrapper
    @XmlElement(name = "filter")
    private Set<AbstractFilter> filters = null;

    @XmlElementWrapper
    @XmlElement(name = "locatableEntity")
    private Set<LocatableEntity> locatableEntities = null;

    @XmlAttribute
    private String version = "";

    @XmlAttribute
    private String revision = "";


    public Set<Questionnaire> getQuestionnaires() {
        return questionnaires;
    }

    public void setQuestionnaires(Set<Questionnaire> questionnaires) {
        this.questionnaires = questionnaires;
    }

    public Set<I18NEntity> getTranslations() {
        return translations;
    }

    public void setTranslations(Set<I18NEntity> translations) {
        this.translations = translations;
    }

    public Set<ChoiceGroup> getChoiceGroups() {
        return choiceGroups;
    }

    public void setChoiceGroups(Set<ChoiceGroup> choiceGroups) {
        this.choiceGroups = choiceGroups;
    }

    public Set<MatchingMatrix> getMatchingMatrices() {
        return matchingMatrices;
    }

    public void setMatchingMatrices(Set<MatchingMatrix> matchingMatrices) {
        this.matchingMatrices = matchingMatrices;
    }

    public Set<AbstractQuestionsMapping> getQuestionMappings() {
        return questionMappings;
    }

    public void setQuestionMappings(Set<AbstractQuestionsMapping> questionMappings) {
        this.questionMappings = questionMappings;
    }

    public Set<Product> getProducts() {
        return products;
    }

    public void setProducts(Set<Product> products) {
        this.products = products;
    }

    public Set<Activator> getActivators() {
        return activators;
    }

    public void setActivators(Set<Activator> activators) {
        this.activators = activators;
    }

    public Set<AbstractFilter> getFilters() {
        return filters;
    }

    public void setFilters(Set<AbstractFilter> filters) {
        this.filters = filters;
    }

    public Set<LocatableEntity> getLocatableEntities() {
        return locatableEntities;
    }

    public void setLocatableEntities(Set<LocatableEntity> locatableEntities) {
        this.locatableEntities = locatableEntities;
    }

    public void addQuestionnaire(Questionnaire questionnaire) {
        Set<Questionnaire> questionnaireSet = new HashSet<>();
        questionnaireSet.add(questionnaire);
        addAllQuestionnaires(questionnaireSet);
    }

    public void addAllQuestionnaires(Set<Questionnaire> questionnaireSet) {
        if (this.questionnaires == null) {
            this.questionnaires = new HashSet<>();
        }
        this.questionnaires.addAll(questionnaireSet);
    }

    public void addTranslation(I18NEntity translation) {
        Set<I18NEntity> translationSet = new HashSet<>();
        translationSet.add(translation);
        addAllTranslations(translationSet);
    }

    public void addAllTranslations(Set<I18NEntity> translationSet) {
        if (this.translations == null) {
            this.translations = new HashSet<>();
        }

        this.translations.addAll(translationSet);
    }

    public void addChoiceGroup(ChoiceGroup choiceGroup) {
        Set<ChoiceGroup> choiceGroups = new HashSet<>();
        choiceGroups.add(choiceGroup);
        addAllChoiceGroups(choiceGroups);
    }

    public void addAllChoiceGroups(Set<ChoiceGroup> choiceGroups) {
        if (this.choiceGroups == null) {
            this.choiceGroups = new HashSet<>();
        }

        this.choiceGroups.addAll(choiceGroups);
    }

    public void addMatchingMatix(MatchingMatrix matchingMatrix) {
        Set<MatchingMatrix> matchingMatrices = new HashSet<>();
        matchingMatrices.add(matchingMatrix);
        addAllMatchingMatrices(matchingMatrices);
    }

    public void addAllMatchingMatrices(Set<MatchingMatrix> matchingMatrices) {
        if (this.matchingMatrices == null) {
            this.matchingMatrices = new HashSet<>();
        }

        this.matchingMatrices.addAll(matchingMatrices);
    }

    public void addQuestionMapping(AbstractQuestionsMapping questionsMapping) {
        Set<AbstractQuestionsMapping> questionsMappings = new HashSet<>();
        questionsMappings.add(questionsMapping);
        addAllQuestionMappings(questionsMappings);
    }

    public void addAllQuestionMappings(Set<AbstractQuestionsMapping> questionMappings) {
        if (this.questionMappings == null) {
            this.questionMappings = new HashSet<>();
        }

        this.questionMappings.addAll(questionMappings);
    }

    public void addProduct(Product product) {
        Set<Product> products = new HashSet<>();
        products.add(product);
        addAllProducts(products);
    }

    public void addAllProducts(Set<Product> products) {
        if (this.products == null) {
            this.products = new HashSet<>();
        }

        this.products.addAll(products);
    }

    public void addActivator(Activator activator) {
        Set<Activator> activators = new HashSet<>();
        activators.add(activator);
        addAllActivators(activators);
    }

    public void addAllActivators(Set<Activator> activators) {
        if (this.activators == null) {
            this.activators = new HashSet<>();
        }

        this.activators.addAll(activators);
    }

    public void addFilter(AbstractFilter filter) {
        Set<AbstractFilter> filters = new HashSet<>();
        filters.add(filter);
        addAllFilters(filters);
    }

    public void addAllFilters(Set<AbstractFilter> filters) {
        if (this.filters == null) {
            this.filters = new HashSet<>();
        }

        this.filters.addAll(filters);
    }

    public void addLocatableEntity(LocatableEntity locatableEntity) {
        Set<LocatableEntity> locatableEntities = new HashSet<>();
        locatableEntities.add(locatableEntity);
        addAllLocatableEntities(locatableEntities);
    }

    public void addAllLocatableEntities(Set<LocatableEntity> locatableEntities) {
        if (this.locatableEntities == null) {
            this.locatableEntities = new HashSet<>();
        }

        this.locatableEntities.addAll(locatableEntities);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }
}
