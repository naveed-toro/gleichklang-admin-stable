package de.binaerebauten.gleichklang.adminweb.view;

import com.vaadin.server.FileDownloader;
import com.vaadin.ui.Upload;
import de.binaerebauten.gleichklang.adminweb.presenter.SerializationPresenter;
import de.binaerebauten.gleichklang.adminweb.service.serialization.SerializationService;
import de.binaerebauten.gleichklang.core.view.NavigateView;

import java.io.InputStream;

/**
 * Project: Import Export
 * Created by Domi on 03.06.2016.
 */
public interface SerializationView extends NavigateView<SerializationView.SerializationListener> {

    void setupDownloadButton(FileDownloader fileDownloader);
//    void setupUploadButton(SerializationPresenter.ImportFileUploader receiver);
    void setUploadReceiver(Upload.Receiver receiver);
    void setSucceededListener(Upload.SucceededListener succededListener);

    interface SerializationListener extends NavigateView.NavigateViewListener {
        void serializationGroupDidChange(SerializationService.SerializationGroup type);
    }
}
