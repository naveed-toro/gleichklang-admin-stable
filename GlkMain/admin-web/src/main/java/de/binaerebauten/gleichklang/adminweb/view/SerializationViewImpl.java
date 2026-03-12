package de.binaerebauten.gleichklang.adminweb.view;

import com.vaadin.server.FileDownloader;
import com.vaadin.ui.*;
import com.vaadin.ui.Component;
import de.binaerebauten.gleichklang.adminweb.presenter.SerializationPresenter;
import de.binaerebauten.gleichklang.adminweb.service.serialization.SerializationService;
import de.binaerebauten.gleichklang.core.model.legacy.persistence.*;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;

/**
 * Project: Import Export
 * Created by Domi on 03.06.2016.
 */
public class SerializationViewImpl extends AbstractNavigateView<SerializationView.SerializationListener> implements SerializationView {

    private final AbstractOrderedLayout layout;

    private Upload uploadComponent;
    private Button exportButton;
    private ComboBox groupChooser;

    public SerializationViewImpl() {
        layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setMargin(true);

        layout.addComponent(createExportPanel());
        layout.addComponent(createImportPanel());

        setCompositionRoot(layout);
    }

    private Component createExportPanel() {
        final Panel panel = new Panel("Export");

        final VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.addComponent(createChooser());
        layout.addComponent(createExportButton());

        panel.setContent(layout);
        return panel;
    }

    private Component createImportPanel() {
        final Panel panel = new Panel("Import");

        final VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.addComponentAsFirst(createImportButton());

        panel.setContent(layout);
        return panel;
    }

    private ComboBox createChooser() {
        groupChooser = new ComboBox();
        groupChooser.addItem(SerializationService.SerializationGroup.QUESTIONNAIRE);
        groupChooser.addItem(SerializationService.SerializationGroup.TRANSLATION);
        groupChooser.addItem(SerializationService.SerializationGroup.ACTIVATOR);
        groupChooser.addItem(SerializationService.SerializationGroup.FILTER);
        groupChooser.addItem(SerializationService.SerializationGroup.MAPPER);
        groupChooser.addItem(SerializationService.SerializationGroup.MATRIX);
        groupChooser.addItem(SerializationService.SerializationGroup.PRODUCT);
        groupChooser.addItem(SerializationService.SerializationGroup.ALL);

        groupChooser.setTextInputAllowed(false);

        groupChooser.addValueChangeListener(e -> fireEvent(eventAction -> {
            final boolean valueChoosed = groupChooser.getValue() != null;
            exportButton.setEnabled(valueChoosed);

            eventAction.serializationGroupDidChange((SerializationService.SerializationGroup)groupChooser.getValue());
        }));

        return groupChooser;
    }


    private Component createImportButton() {
        uploadComponent = new Upload();
        uploadComponent.setButtonCaption("Import");

        return uploadComponent;
    }

    private Component createExportButton() {
        exportButton = new Button("Export");
        exportButton.setEnabled(false);

        return exportButton;
    }

    @Override
    public void setupDownloadButton(FileDownloader fileDownloader) {
        fileDownloader.extend(exportButton);
    }

    @Override
    public void setUploadReceiver(Upload.Receiver receiver) {
        uploadComponent.setReceiver(receiver);
    }

    @Override
    public void setSucceededListener(Upload.SucceededListener succededListener) {
        uploadComponent.addSucceededListener(succededListener);
    }

//    @Override
//    public void setupUploadButton(SerializationPresenter.ImportFileUploader receiver) {
//        this.uploadComponent.setReceiver(receiver);
//        this.uploadComponent.addSucceededListener(receiver);
//    }
}
