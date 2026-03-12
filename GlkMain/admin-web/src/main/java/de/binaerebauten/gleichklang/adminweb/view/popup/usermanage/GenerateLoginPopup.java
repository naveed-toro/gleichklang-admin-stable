package de.binaerebauten.gleichklang.adminweb.view.popup.usermanage;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Link;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.core.view.popup.GenericPopup;

public class GenerateLoginPopup extends GenericPopup
{
	public GenerateLoginPopup(String loginUrl)
	{
		final Link link = new Link(loginUrl, new ExternalResource(loginUrl));
		link.setTargetName("_blank");
		
		final Button closeButton = new Button("Schließen");
		closeButton.addClickListener(event -> close());
		
		setPopupContent(link);
		addFooterComponent(closeButton);
		
		addStyleName(CssStyle.MESSAGE_BOX.getStyleName());
	}
}
