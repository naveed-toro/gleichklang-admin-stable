package de.binaerebauten.gleichklang.memberweb.view;

import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.view.NavigateView;

import java.time.LocalDate;
import java.util.Map;
import java.util.SortedMap;

/**
 * Created by rgoerner on 19.09.16.
 */
public interface RecommendationBreakView extends NavigateView<RecommendationBreakView.RecommendationBreakViewListener>
{
    interface RecommendationBreakViewListener extends NavigateView.NavigateViewListener
    {
        void saveRecommendationBreaks(Map<RecommendationCategory, LocalDate> recommendationBreaks);
    }

    void setRecommendationBreaks(SortedMap<RecommendationCategory, Boolean> categories, Map<RecommendationCategory, LocalDate> recommendationBreaks);

    void reset();
}
