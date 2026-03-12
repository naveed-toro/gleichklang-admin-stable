package de.binaerebauten.gleichklang.memberweb.view;

import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.FooterCommandBar;
import de.binaerebauten.gleichklang.core.view.component.FormPanel;
import de.binaerebauten.gleichklang.core.view.component.user.RecommendationBreakComponent;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * Created by rgoerner on 20.09.16.
 */
public class RecommendationBreakViewImpl extends AbstractNavigateView<RecommendationBreakView.RecommendationBreakViewListener> implements RecommendationBreakView
{
    final VerticalLayout layout;

    public RecommendationBreakViewImpl()
    {
        layout = new VerticalLayout();
        layout.setSpacing(true);
        setCompositionRoot(layout);
    }

    @Override
    public void setRecommendationBreaks(SortedMap<RecommendationCategory, Boolean> categories, Map<RecommendationCategory, LocalDate> recommendationBreaks)
    {
        final FormPanel formPanel = new FormPanel(I18N.BREAKPANEL_CAPTION_TITLE.msg());
        formPanel.setDescription(I18N.BREAKPANEL_CAPTION_DESCRIPTION.msg());
        formPanel.addStyleName(CssStyle.GK_PANEL.getStyleName());
        formPanel.addStyleName(CssStyle.RECOMMENDATION_VIEW.getStyleName());

        final List<RecommendationBreakComponent> recommendationBreakComponents = RecommendationBreakComponent.createRecommendationBreakComponents(categories, recommendationBreaks);
        formPanel.addFormElements(recommendationBreakComponents);

        layout.addComponent(formPanel);

        final SaveHelper saveHelper = new SaveHelper(() -> saveBreaks(recommendationBreakComponents));
        final FooterCommandBar commandBar = new FooterCommandBar(saveHelper.getSaveButton());
        layout.addComponent(commandBar);
    }

    @Override
    public void reset()
    {

    }

    private void saveBreaks(List<RecommendationBreakComponent> recommendationBreakComponents)
    {
        final Map<RecommendationCategory, LocalDate> recommendationBreaks = RecommendationBreakComponent.getRecommendationBreaks(recommendationBreakComponents);

        fireEvent(eventAction -> eventAction.saveRecommendationBreaks(recommendationBreaks));
    }
}
