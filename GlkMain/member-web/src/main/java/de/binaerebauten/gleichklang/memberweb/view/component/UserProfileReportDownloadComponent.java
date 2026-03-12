package de.binaerebauten.gleichklang.memberweb.view.component;

import com.vaadin.server.StreamResource;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.view.component.FileViewerComponent;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.view.I18N;

import java.util.Map;

/**
 * Created by rgoerner on 20.05.16.
 */
public class UserProfileReportDownloadComponent extends CustomComponent
{


    private final VerticalLayout wrapper;
    private Panel userProfileReportDownloadComponent;


    public UserProfileReportDownloadComponent()
    {

        wrapper = new VerticalLayout();
        wrapper.setSizeFull();
        wrapper.setSpacing(true);
        wrapper.addStyleName(CssStyle.PANEL_WRAPPER.getStyleName());
        setCompositionRoot(wrapper);


        final HorizontalLayout picWrapper = new HorizontalLayout();
        picWrapper.setSizeFull();
        picWrapper.setStyleName(CssStyle.USER_PROFILE_REPORT_PLACEHOLDER.getStyleName());

        HorizontalLayout header = new HorizontalLayout();
        header.setSizeFull();
        header.addComponent(new Label(I18N.USERPROFILE_REPORT_LABEL.msg()));
        header.addStyleName(CssStyle.PANEL_HEADER.getStyleName());
        header.addStyleName(CssStyle.REPORT_RED.getStyleName());

        wrapper.addComponent(picWrapper);
        wrapper.addComponent(header);

        userProfileReportDownloadComponent = new Panel();//"Profil Download");

    }


    public VerticalLayout createUserReportDownloader(Map<String, StreamResource> sourceList)
    {
//        final Panel profileDownloaderComponent = new FileLinkComponent(sourceList);
        final Panel profileDownloaderComponent = new FileViewerComponent(sourceList);

        profileDownloaderComponent.setSizeFull();
        profileDownloaderComponent.setStyleName(CssStyle.USER_PROFILE_REPORT_DOWNLOAD_WRAPPER.getStyleName());
        //profileDownloaderComponent.setCaption(I18N.HOMEVIEW_PROFILE_DOWNLOAD_TITLE.msg());
        profileDownloaderComponent.setDescription(I18N.HOMEVIEW_PROFILE_DOWNLOAD_DESCRIPTION.msg());


        wrapper.replaceComponent(this.userProfileReportDownloadComponent, profileDownloaderComponent);

        this.userProfileReportDownloadComponent = profileDownloaderComponent;

        return wrapper;
    }



}
