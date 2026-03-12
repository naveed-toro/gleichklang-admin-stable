package de.binaerebauten.gleichklang.core.view.component;

import com.google.common.base.Strings;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

/**
 * Header component for questionnaires with icons for
 * question and recommendation category
 *
 * @author dwinkler
 */
public class QuestionnaireHeader extends CustomComponent {

    final Button focusPlaceholderButton;

    public QuestionnaireHeader(Questionnaire questionnaire) {
        final VerticalLayout rootLayout = new VerticalLayout();
        rootLayout.addStyleName("root-wrapper");

        final Component iconLayout = iconLayout(questionnaire.getIcon(), questionnaire.getRecommendationCategory());
        final Component captionLayout = captionLayout(questionnaire.getName(), questionnaire.getRecommendationCategory());
        final Component descriptionLayout = descriptionLayout(questionnaire.getDescription());

        focusPlaceholderButton = new Button();
        focusPlaceholderButton.setStyleName(CssStyle.FOCUS_BTN.getStyleName());

        rootLayout.addComponents(focusPlaceholderButton, iconLayout, captionLayout, descriptionLayout);

        setCompositionRoot(rootLayout);
        setStyleName(CssStyle.QUESTIONNAIRE_MAIN_HEADER.getStyleName());
    }

    private Component iconLayout(Questionnaire.Icon icon, RecommendationCategory recommendationCategory) {
        final HorizontalLayout iconLayout = new HorizontalLayout();
        iconLayout.setStyleName(CssStyle.QUESTIONNAIRE_ICON_WRAPPER.getStyleName());
        iconLayout.setWidth(100, Unit.PERCENTAGE);

        if (icon == null && recommendationCategory == null) {
//            iconLayout.addStyleName(CssStyle.FORM_PART_EMPTY.getStyleName());
            iconLayout.setVisible(false);
            return iconLayout;
        }

        final HorizontalLayout subwrapper = new HorizontalLayout();
        subwrapper.setSizeUndefined();
        iconLayout.addComponent(subwrapper);
        iconLayout.setComponentAlignment(subwrapper, Alignment.MIDDLE_CENTER);

        if (icon != null) {
            final Image questionnaireIcon = new Image();
            questionnaireIcon.setSource(icon.getResource());
            questionnaireIcon.addStyleName(CssStyle.QUESTIONNAIRE_ICON.getStyleName());
            subwrapper.addComponents(questionnaireIcon);
        }

        if (recommendationCategory != null) {
            final Image categoryIcon = new Image();
            categoryIcon.setSource(recommendationCategory.getOutlinedIcon());
            categoryIcon.addStyleName(CssStyle.QUESTIONNAIRE_ICON.getStyleName());
            subwrapper.addComponents(categoryIcon);
        }

        return iconLayout;
    }

    private Component captionLayout(String caption, RecommendationCategory recommendationCategory) {
        final Label questionnaireCaption = new Label();
        questionnaireCaption.setStyleName(CssStyle.QUESTIONNAIRE_LABEL.getStyleName());
        questionnaireCaption.setContentMode(ContentMode.HTML);

        String captionString = caption;
        if (recommendationCategory != null) {
            captionString = captionString.concat(String.format("<span class=\"category\"> / %s", recommendationCategory.getName()));
        }
        questionnaireCaption.setValue(captionString);
        questionnaireCaption.setVisible(!Strings.isNullOrEmpty(caption));

        return questionnaireCaption;
    }

    private Component descriptionLayout(String description) {
        final Label descriptionLabel = new Label();
        descriptionLabel.setStyleName(CssStyle.QUESTIONNAIRE_DESCRIPTION.getStyleName());
        descriptionLabel.setContentMode(ContentMode.HTML);
        descriptionLabel.setValue(description);
        descriptionLabel.setVisible(!Strings.isNullOrEmpty(description));

        return descriptionLabel;
    }

    public void focusHeader()
    {
        focusPlaceholderButton.focus();
    }
}
