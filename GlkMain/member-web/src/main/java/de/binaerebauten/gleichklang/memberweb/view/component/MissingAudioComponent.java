package de.binaerebauten.gleichklang.memberweb.view.component;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.view.I18N;

import java.util.Objects;
import java.util.Set;

/**
 * Created by syed.
 */
public class MissingAudioComponent extends CustomComponent
{
    public interface MissingAudioHandler
    {
        void onAudioClicked(RecommendationCategory recommendationCategory);
    }

    private final VerticalLayout wrapper;
    private final MissingAudioHandler missingAudioHandler;

    public MissingAudioComponent(MissingAudioHandler missingAudioHandler)
    {
        Objects.requireNonNull(missingAudioHandler);

        this.missingAudioHandler = missingAudioHandler;

        wrapper = new VerticalLayout();
        wrapper.setStyleName(CssStyle.MISSING_AUDIO_COMPONENT.getStyleName());
        setCompositionRoot(wrapper);
    }


    public void setMissingRecommendationCategories(Set<RecommendationCategory> recommendationCategories)
    {
        wrapper.removeAllComponents();
        if(recommendationCategories.isEmpty())
        {
            wrapper.setVisible(false);
            return;
        }
        wrapper.addComponent(new Label(I18N.MISSING_AUDIO_COMPONENT.msg()));
        for(RecommendationCategory category : recommendationCategories)
        {
            final Button incompleteButton = new Button(I18N.MISSING_AUDIO_LABEL.msg() +" " +category.getName());
            incompleteButton.setIcon(new ThemeResource("img/blue.png"));
            incompleteButton.setStyleName(CssStyle.TEXT_BUTTON.getStyleName());
            incompleteButton.addClickListener(event -> missingAudioHandler.onAudioClicked(category));
            wrapper.addComponent(incompleteButton);
        }
    }
}
