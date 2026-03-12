package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.server.StreamResource;

/**
 * Created by Domi on 04.10.2016.
 */
public interface OnDemandStreamSource extends StreamResource.StreamSource {
    String getFileName();
    String getMimeType();
}
