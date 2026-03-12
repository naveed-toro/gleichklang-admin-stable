package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.shared.ui.link.LinkConstants;
import com.vaadin.ui.Link;

import javax.validation.constraints.NotNull;
import java.io.IOException;

/**
 * OnDemand Link Component
 *
 * Stream Resource will be generated when Link is clicked
 */
public class OnDemandLink extends Link {


    public interface OnDemandStreamSource extends StreamSource {
        String getFilename();
        String getMimeType();
    }

    private static final long serialVersionUID = 1L;
    private final OnDemandStreamSource onDemandStreamSource;


    public OnDemandLink(String caption, @NotNull OnDemandStreamSource onDemandStreamSource) {
        super(caption, new StreamResource(onDemandStreamSource, ""));
        this.onDemandStreamSource = onDemandStreamSource;
    }

    @Override
    public boolean handleConnectorRequest(VaadinRequest request, VaadinResponse response, String path) throws IOException {
        getResource().setFilename(onDemandStreamSource.getFilename());
        getResource().setMIMEType(onDemandStreamSource.getMimeType());
        return super.handleConnectorRequest(request, response, path);
    }

    public StreamResource getResource() {
        return (StreamResource) this.getResource(LinkConstants.HREF_RESOURCE);
    }
}
