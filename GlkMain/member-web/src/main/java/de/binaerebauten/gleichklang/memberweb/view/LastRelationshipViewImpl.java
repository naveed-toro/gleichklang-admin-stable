package de.binaerebauten.gleichklang.memberweb.view;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.matching.Relationship_;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.view.LastRelationshipView.LastRelationshipViewListener;
import de.binaerebauten.gleichklang.memberweb.view.component.RelationshipTable;
import de.binaerebauten.gleichklang.memberweb.view.component.RelationshipTable.RelationshipTableHandler;

public class LastRelationshipViewImpl extends AbstractNavigateView<LastRelationshipViewListener> implements LastRelationshipView
{
	private final RelationshipTable relationshipTable;
	
	public LastRelationshipViewImpl(Device device)
	{
		relationshipTable = createRelationshipTable(device);
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setStyleName(CssStyle.RELATIONSHIP_VIEW_WRAPPER.getStyleName());
        layout.addStyleName(CssStyle.LASTRELATIONSHIPVIEW_WRAPPER.getStyleName());


        final Button focusPlaceholder = new Button();
        focusPlaceholder.addStyleName(CssStyle.FOCUS_BTN.getStyleName());

		final Button goToTopButton = new Button();
		goToTopButton.addStyleName(CssStyle.GO_TO_TOP.getStyleName());
		goToTopButton.addStyleName(CssStyle.GREEN.getStyleName());
		goToTopButton.setIcon(FontAwesome.CHEVRON_UP);
		goToTopButton.setCaption(I18N.GOTOTOP.msg());
		goToTopButton.addClickListener(event -> focusPlaceholder.focus());

		layout.addComponent(focusPlaceholder);
		layout.addComponent(createHeader());
		layout.addComponent(relationshipTable);
		layout.addComponent(goToTopButton);
		
		setCompositionRoot(layout);
	}
	
	private RelationshipTable createRelationshipTable(Device device)
	{
		final RelationshipTable relationshipTable = new RelationshipTable(device);
		
		relationshipTable.setSortPropertyId(false, Relationship_.lastViewedDate);
		relationshipTable.setMaxResults(20);
		
		return relationshipTable;
	}
	
	private Component createHeader()
	{
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSizeUndefined();
		final Label label = new Label(I18N.LASTRELATIONSHIPVIEW_HEADER.getName());
		layout.setStyleName(CssStyle.VIEW_HEADER.getStyleName());
		layout.addComponent(label);
		
		return layout;
	}
	
	@Override
	public void updateItem(Relationship relationship)
	{
		relationshipTable.refreshItem(relationship);
	}
	
	@Override
	public void setLastRelationshipHandler(LazyBeanFilteredItemsHandler<Relationship> handler, RelationshipTableHandler relationshipTableHandler)
	{
		relationshipTable.setRelationshipHandler(handler, relationshipTableHandler);
	}
	
	@Override
	public void onDeviceChanged(Device device)
	{
		super.onDeviceChanged(device);
		relationshipTable.onDeviceChanged(device);
	}
}
