package de.binaerebauten.gleichklang.memberweb.view.popup;

import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.view.popup.GenericPopup;

public class AskForDeletePopup extends GenericPopup
{
	public interface AskForDeletePopupListener
	{
		void removeUser(User user);
		
		void reuseUser(User user);
	}
	
	public AskForDeletePopup(AskForDeletePopupListener listener, User user)
	{
		final HorizontalLayout footerLayout = new HorizontalLayout();
		footerLayout.setSpacing(true);
		
		setPopupContent(new Label(I18N.ASKFORDELETEPOPUP_CAPTION_DESCRIPTION.msg()));
		setFooter(footerLayout);
		
		footerLayout.addComponent(new Button(I18N.ASKFORDELETEPOPUP_ACTION_REUSE.msg(), e ->
		{
			listener.reuseUser(user);
			close();
		}));
		footerLayout.addComponent(new Button(I18N.ASKFORDELETEPOPUP_ACTION_DELETE.msg(), e ->
		{
			listener.removeUser(user);
			close();
		}));
	}
}
