package de.binaerebauten.gleichklang.memberweb.view;

import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.utils.filter.FilterSpecificationBuilder;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.component.FilterControlComponent.FilterControlHandler;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.memberweb.view.RelationshipView.RelationshipViewListener;
import de.binaerebauten.gleichklang.memberweb.view.component.RelationshipTable.RelationshipTableHandler;

import java.util.Collection;

public interface RelationshipView extends NavigateView<RelationshipViewListener>
{
	interface RelationshipViewListener extends NavigateView.NavigateViewListener
	{
	}
	
	void setRelationshipHandler(LazyBeanFilteredItemsHandler<Relationship> handler, RelationshipTableHandler relationshipTableHandler);
	
	void setActiveRecommendationCategories(Collection<RecommendationCategory> categories, String category);

	void setFilterHandlerAndBuilder(FilterControlHandler filterControlHandler, FilterSpecificationBuilder filterSpecificationBuilder);
	
	void updateItem(Relationship relationship);
	
	RecommendationCategory getSelectedCategory();
}
