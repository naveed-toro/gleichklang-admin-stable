package de.binaerebauten.gleichklang.memberweb.view.component;

import com.google.common.collect.Multimap;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question.Requirement;
import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.view.I18N;

import java.util.List;
import java.util.Set;

/**
 * Created by rgoerner on 27.09.16.
 */
public class MyProfileComponent extends CustomComponent
{
    private final VerticalLayout myProfileContent;
    private final IncompleteQuestionnaireComponent incompleteQuestionnaireComponent;
    private final MissingAvatarComponent missingAvatarComponent;
    private final MissingAudioComponent missingAudioComponent;
    private final VerticalLayout myProfileWrapper;

    private int missingAvatars;
    private int missingAudio;

    public MyProfileComponent(IncompleteQuestionnaireComponent.ShowIncompleteQuestionsHandler handler, MissingAvatarComponent.MissingAvatarHandler missingAvatarHandler,MissingAudioComponent.MissingAudioHandler missingAudioHandler)
    {
        myProfileWrapper = new VerticalLayout();
        myProfileWrapper.setSizeFull();
        myProfileWrapper.setStyleName(CssStyle.PANEL_MY_PROFILE.getStyleName());

        incompleteQuestionnaireComponent = new IncompleteQuestionnaireComponent(handler);
        missingAvatarComponent = new MissingAvatarComponent(missingAvatarHandler);
        missingAudioComponent = new MissingAudioComponent(missingAudioHandler);
        myProfileContent = new VerticalLayout();
        myProfileContent.setSizeFull();

        Component picture = createHeaderImage();
        myProfileWrapper.addComponent(picture);
        myProfileWrapper.setComponentAlignment(picture, Alignment.TOP_CENTER);
        myProfileWrapper.addComponent(createHeaderText());

        final Label seperator = new Label("<hr />", ContentMode.HTML);
        seperator.setStyleName(CssStyle.COMPONENT_SEPERATOR.getStyleName());

        myProfileContent.addComponents(incompleteQuestionnaireComponent, seperator, missingAvatarComponent,missingAudioComponent);
        myProfileWrapper.addComponent(myProfileContent);

        setCompositionRoot(myProfileWrapper);

        missingAvatars = 0;
        missingAudio = 0;
    }

    private Component createHeaderImage()
    {
        final HorizontalLayout picWrapper = new HorizontalLayout();
        picWrapper.setSizeFull();
        picWrapper.setStyleName(CssStyle.QUESTIONNAIRE_PLACEHOLDER.getStyleName());
        return picWrapper;
    }

    private Component createHeaderText()
    {
        final HorizontalLayout headerText = new HorizontalLayout();
        headerText.addComponent(new Label(I18N.MYPROFILE_COMPONENT_LABEL.msg()));
        headerText.addStyleName(CssStyle.PANEL_HEADER.getStyleName());
        headerText.addStyleName(CssStyle.INCOMPLETE_QUESTIONNAIRE_YELLOW.getStyleName());

        return headerText;
    }

    private void setDefaultContent()
    {
        myProfileContent.removeAllComponents();
        final Label label = new Label(I18N.MYPROFILE_DEFAULT_TEXT.msg());
        label.addStyleName(CssStyle.PANEL_DEFAULT_TEXT.getStyleName());
        myProfileContent.addComponent(label);
    }

    public void setIncompleteQuestionnaires(Multimap<Questionnaire, Requirement> incompleteQuestionnaires)
    {

        if (incompleteQuestionnaires != null && !incompleteQuestionnaires.isEmpty())
        {
            incompleteQuestionnaireComponent.createIncompleteQuestionnaires(incompleteQuestionnaires);
        }
        else
            myProfileContent.removeComponent(incompleteQuestionnaireComponent);
    }

    public void setMissingAvatars(List<RecommendationCategory> recommendationCategories)
    {
        missingAvatars = 0;

        if (recommendationCategories != null && !recommendationCategories.isEmpty())
        {
            missingAvatarComponent.setMissingRecommendationCategories(recommendationCategories);
            missingAvatars = recommendationCategories.size();
        }
        else
            myProfileContent.removeComponent(missingAvatarComponent);
    }

    public void setMissingAudio(Set<RecommendationCategory> recommendationCategories)
    {
        missingAudio = 0;

        if (recommendationCategories != null && !recommendationCategories.isEmpty())
        {
            missingAudioComponent.setMissingRecommendationCategories(recommendationCategories);
            missingAudio = recommendationCategories.size();
        }
        else
            myProfileContent.removeComponent(missingAudioComponent);
    }

    public void checkContent()
    {
       if (incompleteQuestionnaireComponent.getNrOfIncompleteQuestionnaires() + missingAvatars == 0 && incompleteQuestionnaireComponent.getNrOfIncompleteQuestionnaires() + missingAudio ==0)
       {
           setDefaultContent();
       }
        incompleteQuestionnaireComponent.setVisible(incompleteQuestionnaireComponent.getNrOfIncompleteQuestionnaires() > 0);

    }
}
