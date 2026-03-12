package de.binaerebauten.gleichklang.core.presenter;

import de.binaerebauten.gleichklang.core.view.component.Popup;

public interface PopupOpener
{
	/**
	 * Open a popup if the popup is not still open.
	 *
	 * @param popup Popup to be open
	 * @return true, if the popup could be opened
	 */
	boolean tryOpenPopup(Popup popup);
}
