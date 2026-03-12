package de.binaerebauten.gleichklang.memberweb.view;

import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.view.NavigateView;

import java.util.Set;
import java.util.SortedMap;

/**
 * Created by rgoerner on 19.09.16.
 */
public interface ActiveCategoriesView  extends NavigateView<ActiveCategoriesView.ActiveCategoriesViewListener>
{
    interface ActiveCategoriesViewListener extends NavigateView.NavigateViewListener
    {
        void saveCategories(Set<RecommendationCategory> recommendationCategories) throws ValidationException;
    }

    void setCategories(SortedMap<RecommendationCategory, Boolean> categories);
}
