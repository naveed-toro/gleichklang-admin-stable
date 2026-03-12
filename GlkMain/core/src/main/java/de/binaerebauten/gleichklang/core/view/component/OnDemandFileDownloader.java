package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.server.*;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Project: Import Export
 * Created by Domi on 23.06.2016.
 */
public class OnDemandFileDownloader extends FileDownloader {

    public interface OnDemandStreamResource extends StreamResource.StreamSource {
        String getFileName();
        String getMimeType();
    }

    private final OnDemandStreamResource onDemandStreamResource;

    /**
     * Creates a new file downloader for the given resource. To use the
     * downloader, you should also {@link #extend(AbstractClientConnector)} the
     * component.
     *
     * @param resource the resource to download when the user clicks the extended
     *                 component.
     */
    public OnDemandFileDownloader(OnDemandStreamResource resource) {
        super(new StreamResource(resource, ""));

        this.onDemandStreamResource = checkNotNull(resource, "Given on-demand stream resouce may not null!");
    }

    @Override
    public boolean handleConnectorRequest(VaadinRequest request, VaadinResponse response, String path) throws IOException {
        getResource().setFilename(onDemandStreamResource.getFileName());
        getResource().setMIMEType(onDemandStreamResource.getMimeType());
        return super.handleConnectorRequest(request, response, path);
    }

    private StreamResource getResource() {
        return (StreamResource)this.getResource("dl");
    }
}
