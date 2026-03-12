package de.binaerebauten.gleichklang.adminweb.view;

import de.binaerebauten.gleichklang.adminweb.view.ScammingView.ScammingViewListener;
import de.binaerebauten.gleichklang.core.model.message.Scamming;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;

import java.util.List;

public interface ScammingView extends NavigateView<ScammingViewListener>
{
	interface ScammingViewListener extends NavigateView.NavigateViewListener
	{
		void search(long messageCount, long durationSeconds, boolean containsEmail, List<String> keywords);
		
		void refresh();
		
		void openUser(User user);
	}
	
	void addResult(LazyBeanFilteredItemsHandler<Scamming> scammingHandler);
	
	void initValues(long messageCount, long durationSeconds, boolean containsEmail, String keywords);
}
