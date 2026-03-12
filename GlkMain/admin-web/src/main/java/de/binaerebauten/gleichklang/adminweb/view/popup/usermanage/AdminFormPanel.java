package de.binaerebauten.gleichklang.adminweb.view.popup.usermanage;

import com.google.common.base.Strings;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.view.component.HorizontalLine;

import java.util.Map;
import java.util.Map.Entry;

public class AdminFormPanel extends CustomComponent
{
	private final VerticalLayout layout;
	
	public AdminFormPanel()
	{
		this(null);
	}
	
	public AdminFormPanel(String caption)
	{
		layout = new VerticalLayout();
		layout.setMargin(true);
		
		if (!Strings.isNullOrEmpty(caption))
		{
			addHeadline(caption);
		}
		
		setCompositionRoot(layout);
	}
	
	public void addHeadline(String caption)
	{
		final VerticalLayout headlineLayout = new VerticalLayout();
		
		final HorizontalLine line = new HorizontalLine();
		final Label label = new BoldLabel(caption);
		
		headlineLayout.addComponents(new Label(), label, line);
		
		layout.addComponent(headlineLayout);
	}
	
	public void addMatrix(Map<String, Map<RecommendationCategory, ?>> matrix)
	{
		final GridLayout gridLayout = new GridLayout();
		gridLayout.setSpacing(true);
		gridLayout.setSizeFull();
		
		if (matrix == null) return;
		
		gridLayout.setColumns(RecommendationCategory.values().length + 1);
		gridLayout.setRows(matrix.size() + 1);
		
		for (RecommendationCategory category : RecommendationCategory.values())
		{
			gridLayout.addComponent(new Label(category.toString()), category.ordinal() + 1, 0);
		}
		
		int row = 1;
		
		for (Entry<String, Map<RecommendationCategory, ?>> entry : matrix.entrySet())
		{
			final String title = entry.getKey();
			gridLayout.addComponent(new BoldLabel(title), 0, row);
			
			for (Entry<RecommendationCategory, ?> valueEntry : entry.getValue().entrySet())
			{
				final Object value = valueEntry.getValue();
				final Component component = value instanceof Component ? (Component) value : new Label(value.toString());
				final int column = valueEntry.getKey().ordinal() + 1;
				
				gridLayout.addComponent(component, column, row);
			}
			
			row++;
		}
		
		this.layout.addComponent(gridLayout);
	}
	
	public void addLine(String caption, String value)
	{
		final HorizontalLayout lineLayout = new HorizontalLayout();
		lineLayout.setSpacing(true);
		
		lineLayout.addComponent(new BoldLabel(caption));
		lineLayout.addComponent(new Label(value));
		
		layout.addComponent(lineLayout);
	}
	
	public void addComponent(Component component)
	{
		layout.addComponent(component);
	}
	
	public void addValueList(Map<String, ?> valueMap)
	{
		final GridLayout gridLayout = new GridLayout();
		gridLayout.setSpacing(true);
		gridLayout.setSizeFull();
		
		if (valueMap == null) return;
		
		gridLayout.setColumns(2);
		gridLayout.setRows(valueMap.size());
		
		int row = 0;
		
		for (Map.Entry<String, ?> entry : valueMap.entrySet())
		{
			final String title = entry.getKey();
			gridLayout.addComponent(new BoldLabel(title), 0, row);
			
			final Object value = entry.getValue();
			final Component component = value instanceof Component ? (Component) value : new Label(value.toString());
			
			gridLayout.addComponent(component, 1, row);
			
			row++;
		}
		
		layout.addComponent(gridLayout);
	}
	
	public void addLineBreak()
	{
		layout.addComponent(new Label());
	}
}
