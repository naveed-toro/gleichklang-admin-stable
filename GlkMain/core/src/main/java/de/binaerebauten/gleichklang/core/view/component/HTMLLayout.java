package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.UI;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class HTMLLayout extends AbstractNavigateView
{

	private static final Logger LOG = LoggerFactory.getLogger(HTMLLayout.class);

	private final String templateName;
	private final CustomLayout root;

	public HTMLLayout(String templateName){
		this.root = new CustomLayout(templateName);
		this.templateName = templateName;
		setCompositionRoot(this.root);
	}

	public boolean isResourceExists(UI currentUi){
		String templateResource = String.format("layouts/%s.html", templateName);
		ThemeResource themeResource = new ThemeResource(templateResource) ;

		InputStream resource = null;
		try {
			resource = currentUi.getSession().getService().getThemeResourceAsStream(
                    currentUi, currentUi.getTheme(), themeResource.getResourceId());
		} catch (IOException e) {
			LOG.error(e.getLocalizedMessage());
		}
		return resource != null;
	}

	public CustomLayout getRoot()
	{
		return root;
	}
}
