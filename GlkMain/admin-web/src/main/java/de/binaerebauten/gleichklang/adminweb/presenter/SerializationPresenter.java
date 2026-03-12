package de.binaerebauten.gleichklang.adminweb.presenter;

import com.vaadin.ui.Upload;
import de.binaerebauten.gleichklang.adminweb.service.serialization.SerializationContainer;
import de.binaerebauten.gleichklang.adminweb.service.serialization.SerializationService;
import de.binaerebauten.gleichklang.adminweb.view.SerializationView;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import de.binaerebauten.gleichklang.core.view.component.OnDemandFileDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.xml.bind.JAXBException;
import java.io.*;


/**
 * Project: Import Export
 * Created by Domi on 03.06.2016.
 */
public class SerializationPresenter extends NavigatePresenter implements SerializationView.SerializationListener, Upload.SucceededListener, Upload.Receiver {

    private static final Logger LOG = LoggerFactory.getLogger(SerializationPresenter.class);

    private final SerializationView view;
    private final SerializationService serializationService;

    private SerializationService.SerializationGroup serializationGroup;
    private OnDemandFileDownloader fileDownloader;
    private ByteArrayOutputStream byteArrayOutputStream = null;

    public SerializationPresenter(ApplicationContext ctx, SerializationView view) {
        super(view);
        
        this.view = view;
        this.serializationService = ctx.getBean(SerializationService.class);

        setupView();

        view.setListener(this);
    }


    private void handleImportData(SerializationContainer container) {
        if (serializationService.checkVersion(container)) {
            importData(container);
        } else {
            MessageBox.show("Abweichende Version(different version)", "Möchten Sie die Daten trotzdem importieren?(Proceed?)", MessageBox.MessageBoxButtons.YES_NO, MessageBox.MessageBoxStyle.QUESTION, dialogResult -> {
                if (dialogResult.equals(MessageBox.DialogResult.YES)) {
                    importData(container);
                }
            });
        }
    }

    private void importData(SerializationContainer container) {
        String errors;
        try {
            errors = serializationService.importData(container);
            if (errors!=null && !errors.isEmpty())
                MessageBox.show(errors);
            else
                MessageBox.show("Erledigt(Done)");
        } catch (UniqueValidationException e) {
            LOG.error("SerializationPresenter: the imported data was not valid: ", e);
            MessageBox.show("Fehlerhafte Daten(Corrupt data)", "Prüfen Sie die die zu importierenden Daten auf Korrektheit(validate data)", MessageBox.MessageBoxStyle.ATTENTION, null);
        }  catch (Exception e ){
            MessageBox.show("Der Import ist fehlgeschlagen (Import failed)");
            LOG.error("SerializationPresenter.importData:import failed ", e);
        }
    }


    @Override
    public void enter(String parameters) {

    }

    private void setupView() {
        this.fileDownloader = setupDownloader();

        view.setupDownloadButton(this.fileDownloader);
        view.setUploadReceiver(this);
        view.setSucceededListener(this);
    }


    private OnDemandFileDownloader setupDownloader() {
        final OnDemandFileDownloader fileDownloader = new OnDemandFileDownloader(getFileDownloaderResource(SerializationService.SerializationGroup.QUESTIONNAIRE));

        return fileDownloader;
    }


    // Listener

    @Override
    public void serializationGroupDidChange(SerializationService.SerializationGroup type) {
        this.serializationGroup = type;
    }



    private OnDemandFileDownloader.OnDemandStreamResource getFileDownloaderResource(SerializationService.SerializationGroup group) {
        OnDemandFileDownloader.OnDemandStreamResource streamResource = new OnDemandFileDownloader.OnDemandStreamResource() {
            @Override
            public String getFileName() {
                return serializationGroup.toString() + ".xml";
            }

            @Override
            public String getMimeType() {
                return "text/xml";
            }

            @Override
            public InputStream getStream() {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                serializationService.exportData(serializationGroup, outputStream);

                return new ByteArrayInputStream(outputStream.toByteArray());
            }
        };

        return streamResource;
    }


    @Override
    public OutputStream receiveUpload(String filename, String mimeType) {
        byteArrayOutputStream = new ByteArrayOutputStream();

        return byteArrayOutputStream;
    }

    @Override
    public void uploadSucceeded(Upload.SucceededEvent event) {
        if (this.byteArrayOutputStream.size() == 0) {
            MessageBox.show("Es wurden keine Daten hochgeladen.(No data uploaded)", "Prüfen Sie, ob eine Datei angegeben wurde.(check file)", MessageBox.MessageBoxStyle.ATTENTION, null);
            return;
        }

        try {
            SerializationContainer container = serializationService.deserialize(new ByteArrayInputStream(this.byteArrayOutputStream.toByteArray()));
            handleImportData(container);
            byteArrayOutputStream.close();
            byteArrayOutputStream = null;
        } catch (JAXBException e) {
            LOG.error(e.getMessage(), e);
            MessageBox.show("Feherhafte XML Datei", "Prüfen Sie, ob die importierte Datei ein valides Gleichklang-XML Dokument ist.", MessageBox.MessageBoxStyle.ATTENTION, null);
        } catch (IOException e) {
            LOG.error(e.getMessage(),e);
        }
    }
}
