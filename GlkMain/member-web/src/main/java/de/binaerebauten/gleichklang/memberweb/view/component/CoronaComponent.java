//package de.binaerebauten.gleichklang.memberweb.view.component;
//
//import com.vaadin.server.*;
//import com.vaadin.shared.ui.label.ContentMode;
//import com.vaadin.ui.*;
//import com.vaadin.ui.themes.BaseTheme;
//import com.vaadin.ui.themes.Reindeer;
//import de.binaerebauten.gleichklang.core.initializer.AppUI;
//import de.binaerebauten.gleichklang.core.launcher.H2SchemaGenerator;
//import de.binaerebauten.gleichklang.core.model.user.ClientInformation;
//import de.binaerebauten.gleichklang.core.service.UserService;
//import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;
//import de.binaerebauten.gleichklang.core.utils.PropertiesLoader;
//import de.binaerebauten.gleichklang.core.view.component.NavigationEnum;
//import de.binaerebauten.gleichklang.core.view.css.CssStyle;
//import de.binaerebauten.gleichklang.memberweb.view.I18N;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//
//import java.io.*;
//import java.sql.Connection;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Properties;
//import java.util.stream.Collectors;
//
//public class CoronaComponent extends CustomComponent {
//
//    private static final Logger LOG = LoggerFactory.getLogger(CoronaComponent.class);
//
//    UserService userService = AppUI.getApplicationContext().getBean(UserService.class);
//    private VerticalLayout wrapper = null;
//
//    public CoronaComponent(ClientInformation.Device device, String coronaFilePath) {
//        wrapper = new VerticalLayout();
//        wrapper.setSizeFull();
//
//        wrapper.addStyleName(CssStyle.PANEL_WRAPPER.getStyleName());
//        setCompositionRoot(wrapper);
//
//        Label label1 = new Label();
//        label1.setValue(I18N.COROVIRUS_HEADER_MESSAGE.msg());
//        label1.setSizeFull();
//        label1.setStyleName(CssStyle.PANEL_HEADER.getStyleName());
//        label1.addStyleName(CssStyle.BLUE.getStyleName());
//        label1.setContentMode(ContentMode.HTML);
//        Label label2 = new Label();
//        label2.setValue(I18N.COROVIRUS_MESSAGE.msg());
//        label2.setStyleName(CssStyle.PANEL_CONTENT.getStyleName());
//        label2.setContentMode(ContentMode.HTML);
//
//
//        Label label3 = new Label();
//        label3.setValue(I18N.COROVIRUS_LINK_HEADER_MESSAGE.msg());
//
//        label3.setStyleName(CssStyle.PANEL_CONTENT.getStyleName());
//        label3.setContentMode(ContentMode.HTML);
//
//
//        FileResource resource = new FileResource(new File(
//                "/mnt/webapps/files/_misc/directlist.pdf"));
//
//        Label label5 = new Label();
//        label5.setValue(I18N.COROVIRUS_LINK_MESSAGE.msg());
//        label5.setContentMode(ContentMode.HTML);
//        label5.setStyleName(CssStyle.PANEL_CONTENT.getStyleName());
//
//        BrowserWindowOpener opener = new BrowserWindowOpener(resource);
//        opener.setWindowName("_blank");
//        opener.setParameter("Corona", "corona");
//
//        opener.extend(label5);
//
//        Label headerNext = new Label();
//        headerNext.setValue(I18N.COROVIRUS_LINK_HEADER_MESSAGE_NEXT.msg());
//
//        headerNext.setStyleName(CssStyle.PANEL_CONTENT.getStyleName());
//        headerNext.setContentMode(ContentMode.HTML);
//
//        Button coronaFormLink = new Button(I18N.COROVIRUS_FORM_LINK_MESSAGE.msg());
//        coronaFormLink.setCaptionAsHtml(true);
//        coronaFormLink.addStyleName(BaseTheme.BUTTON_LINK);
//        coronaFormLink.addClickListener(clickEvent -> {
//            VaadinRequest vaadinRequest = VaadinService.getCurrentRequest();
//            LOG.info("Vaadin Request========", vaadinRequest);
//            vaadinRequest.setAttribute("CoronaComponent", "CoronaComponent");
//            navigateTo(CoronaDataTab.REGISTERATION, CoronaDataTab.CONTACT_LIST);
//        });
//
//        wrapper.addComponent(label1);
//        wrapper.addComponent(label2);
//        wrapper.addComponent(label3);
//        wrapper.addComponent(coronaFormLink);
//        wrapper.addComponent(headerNext);
//        wrapper.addComponent(label5);
//        wrapper.addComponent(new Label());
//
//
//    }
//
//    public void navigateTo(NavigationEnum... navigationEnums) {
//        final String viewKey = Arrays.stream(navigationEnums)
//                .map(NavigationEnum::getPath)
//                .collect(Collectors.joining("/"));
//
//        try {
//            UI.getCurrent().getNavigator().navigateTo(viewKey);
//        } catch (IllegalArgumentException ex) {
//        }
//    }
//
//    public enum CoronaDataTab implements DefaultEnumI18N, NavigationEnum {
//        REGISTERATION("registration"),
//        CONTACT_LIST("kontaktliste");
//
//        private final String path;
//
//        CoronaDataTab(String path) {
//            this.path = path;
//        }
//
//        @Override
//        public String toString() {
//            return msg();
//        }
//
//        @Override
//        public String getPath() {
//            return path;
//        }
//    }
//
//}
