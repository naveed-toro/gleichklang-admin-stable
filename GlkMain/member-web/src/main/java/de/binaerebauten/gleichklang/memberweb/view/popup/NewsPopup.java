package de.binaerebauten.gleichklang.memberweb.view.popup;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.news.UserNews;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.core.view.popup.GenericPopup;
import de.binaerebauten.gleichklang.memberweb.view.I18N;

/**
 * Created by rgoerner on 29.08.16.
 */
public class NewsPopup extends GenericPopup
{

    private VerticalLayout wrapper;

    public NewsPopup(UserNews userNews) {
        setIcon(new ThemeResource("img/icon_news.svg"));

        setPopupContent(createNewsLayout(userNews));
        addStyleName(CssStyle.NEWS_POPUP.getStyleName());
    }


    private Component createNewsLayout(UserNews userNews) {
        wrapper = new VerticalLayout();
        wrapper.setHeight(100, Unit.PERCENTAGE);

        final Label title = new Label(userNews.getNews().getTitle());
        title.setStyleName(CssStyle.TITLE.getStyleName());
        title.setWidth(100, Unit.PERCENTAGE);

        final Label teaser = new Label(userNews.getNews().getTeaserText());
        teaser.setWidth(100, Unit.PERCENTAGE);
        teaser.setStyleName(CssStyle.TEASER.getStyleName());

        final Label text = new Label();
        text.setContentMode(ContentMode.HTML);
        text.setValue(userNews.getNews().getText());
        text.setStyleName(CssStyle.TEXT.getStyleName());
        text.setWidth(100, Unit.PERCENTAGE);

        wrapper.addComponents(title, teaser, text);

        return wrapper;
    }

    public void addHideButton(Button.ClickListener listener)
    {
        final Button btnHideNews = new Button(FontAwesome.EYE_SLASH);
        btnHideNews.setCaption(I18N.NEWS_POPUP_HIDENEWS.msg());
        btnHideNews.setDescription(I18N.NEWS_POPUP_HIDENEWS.msg());
        btnHideNews.addClickListener(listener);

        addFooterComponent(btnHideNews, FooterPosition.MIDDLE);
    }
}
