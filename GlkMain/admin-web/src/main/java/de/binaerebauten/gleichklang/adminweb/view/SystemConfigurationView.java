package de.binaerebauten.gleichklang.adminweb.view;

import de.binaerebauten.gleichklang.adminweb.view.QuestionnaireAdminView.QuestionnaireAdminViewListener;
import de.binaerebauten.gleichklang.core.model.questionnaire.ChoiceGroup;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question;
import de.binaerebauten.gleichklang.core.model.questionnaire.QuestionGroup;
import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import de.binaerebauten.gleichklang.core.model.systemconfig.EmailDomainMapping;
import de.binaerebauten.gleichklang.core.model.systemconfig.EmailTemplateMapping;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.core.view.component.TableControl.MoveHandler;

public interface SystemConfigurationView extends NavigateView<SystemConfigurationView.SystemConfigurationViewListener>
{
    interface SystemConfigurationViewListener extends NavigateView.NavigateViewListener
    {
       void  addNewEmailDomainMapping();

        void editEmailDomainMapping(EmailDomainMapping mapping);
        void deleteEmailDomainMapping(EmailDomainMapping mapping);
        void activeInactiveEmailDomainMapping(EmailDomainMapping mapping);
        void deleteEmailTemplate(EmailTemplateMapping mapping);
        void activeInactiveEmailTemplateMapping(EmailTemplateMapping mapping);
        void editEmailTemplate(EmailTemplateMapping mapping);

        /*void newQuestionGroup(Questionnaire questionnaire);

        void editQuestionGroup(QuestionGroup questionGroup);

        /*void deleteQuestionGroup(QuestionGroup questionGroup);

        void undeleteQuestionGroup(QuestionGroup questionGroup);

        void newQuestion(QuestionGroup questionGroup);

        void editQuestion(Question question);

        void deleteQuestion(Question question);

        void undeleteQuestion(Question question);

        void newChoiceGroup();

        void editChoiceGroup(ChoiceGroup choiceGroup);

        void deleteChoiceGroup(ChoiceGroup choiceGroup);

        void undeleteChoiceGroup(ChoiceGroup choiceGroup); */
    }

    void setEmailDomainMappingsHandler(LazyBeanFilteredItemsHandler<EmailDomainMapping> handler);
    void setEmailTemplateMappingsHandler(LazyBeanFilteredItemsHandler<EmailTemplateMapping> handler);


}
