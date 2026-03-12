package de.binaerebauten.gleichklang.core.utils;

import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.UI;

import java.net.URL;

/**
 * Created by Domi on 22.09.2016.
 */
public class ExtendedThemeResource extends ThemeResource {
    /**
     * Creates a resource.
     *
     * @param resourceId the Id of the resource.
     */
    public ExtendedThemeResource(String resourceId) {
        super(resourceId);
    }

    public String getAbsolutePath() {
        final String absPath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
        final String currentTheme = UI.getCurrent().getTheme();

        return String.format("%s/VAADIN/themes/%s/%s", absPath, currentTheme, getResourceId());
    }
}
