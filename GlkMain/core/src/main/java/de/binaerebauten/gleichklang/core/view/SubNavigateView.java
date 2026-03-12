package de.binaerebauten.gleichklang.core.view;

import de.binaerebauten.gleichklang.core.view.NavigateView.NavigateViewListener;
import de.binaerebauten.gleichklang.core.view.component.NavigationEnum;

public interface SubNavigateView<E extends Enum<E> & NavigationEnum, T extends NavigateViewListener> extends NavigateView<T>
{
	void selectSubNavigation(E navigationEnum);
}
