package de.binaerebauten.gleichklang.core.view;

import de.binaerebauten.gleichklang.core.view.component.NavigationComponent;

public interface DeactivateProlongationView<T extends DeactivateProlongationView.DeactivateProlongationViewListener> extends NavigateView<T>
{
	interface DeactivateProlongationViewListener extends NavigateViewListener
	{
		void goToOverviewPage();
	}
	
}
