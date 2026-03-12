package de.binaerebauten.gleichklang.memberweb.view;

import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.memberweb.view.LastRelationshipView.LastRelationshipViewListener;
import de.binaerebauten.gleichklang.memberweb.view.component.RelationshipTable.RelationshipTableHandler;

public interface LastRelationshipView extends NavigateView<LastRelationshipViewListener>
{
	interface LastRelationshipViewListener extends NavigateView.NavigateViewListener
	{
	}
	
	void setLastRelationshipHandler(LazyBeanFilteredItemsHandler<Relationship> handler, RelationshipTableHandler relationshipTableHandler);
	
	void updateItem(Relationship relationship);
}
