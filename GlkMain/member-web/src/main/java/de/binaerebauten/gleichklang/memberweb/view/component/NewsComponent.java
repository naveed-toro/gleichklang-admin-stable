package de.binaerebauten.gleichklang.memberweb.view.component;

import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import de.binaerebauten.gleichklang.core.model.news.News;
import de.binaerebauten.gleichklang.core.model.news.UserNews;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.view.I18N;

import java.util.List;

/**
 * Created by rgoerner on 20.05.16.
 */
public class NewsComponent  extends CustomComponent
{

    public interface ExpandNewsHandler
    {
        void onNewsClicked(UserNews userNews);
    }

    private final VerticalLayout wrapper;
    private final Label newsTeaser;
    private final Label newsTitle;
    private final Label newsText;
    private final Button btnNewsForward;
    private final Button btnNewsBackward;
    private final Button btnExpandNewsText;

    private List<UserNews> userNewsList;
    private Integer activeUserNews;

    private final ExpandNewsHandler expandNewsHandler;


    public NewsComponent(ExpandNewsHandler handler)
    {
        this.expandNewsHandler = handler;

        wrapper = new VerticalLayout();
        wrapper.setSizeFull();
        setCompositionRoot(wrapper);

        activeUserNews = 0;

        newsTitle = new Label();
        newsTeaser = new Label();
        newsText = new Label();

        btnNewsForward = new Button(FontAwesome.CHEVRON_RIGHT);
        btnNewsBackward = new Button(FontAwesome.CHEVRON_LEFT);

        btnExpandNewsText = new Button(I18N.NEWS_COMPONENT_EXPANDBUTTON.msg());
        btnExpandNewsText.addStyleName(CssStyle.TEXT_BUTTON.getStyleName());

        initListener();
    }

    private void createNewsLayout()
    {
        wrapper.removeAllComponents();

        HorizontalLayout picWrapper = new HorizontalLayout();
        picWrapper.setSizeFull();
        picWrapper.setStyleName(CssStyle.NEWS_PLACEHOLDER.getStyleName());

        wrapper.addComponent(picWrapper);
        wrapper.addComponent(createNewsCarousel());
    }

    private void initNews()
    {
        final News news = this.userNewsList.get(activeUserNews).getNews();

        newsTitle.setValue(news.getTitle());
        newsTeaser.setValue(news.getTeaserText());
        newsText.setValue(news.getText());

        btnExpandNewsText.setEnabled(true);
        btnNewsForward.setVisible(userNewsList.size() > 1);
        btnNewsForward.setEnabled(userNewsList.size() > 1);
        btnNewsBackward.setVisible(activeUserNews >= 1);
        btnNewsBackward.setEnabled(activeUserNews >= 1);
    }

    private void moveNewsForward(Button.ClickEvent event)
    {
        activeUserNews++;
        toggleNews();
    }

    private void moveNewsBackward(Button.ClickEvent event)
    {
        activeUserNews--;
        toggleNews();
    }

    private void toggleNews()
    {
        btnNewsForward.setVisible((userNewsList.size() > 1 && activeUserNews < userNewsList.size() - 1));
        btnNewsForward.setEnabled((userNewsList.size() > 1 && activeUserNews < userNewsList.size() - 1));
        btnNewsBackward.setVisible(activeUserNews >= 1);
        btnNewsBackward.setEnabled(activeUserNews >= 1);

        final News news = userNewsList.get(activeUserNews).getNews();

        newsTitle.setValue(news.getTitle());
        newsTeaser.setValue(news.getTeaserText());
        newsText.setValue(news.getText());
    }

    private void initListener()
    {
        btnNewsBackward.addClickListener(this::moveNewsBackward);
        btnNewsForward.addClickListener(this::moveNewsForward);

        btnExpandNewsText.addClickListener(event ->
        {
            final UserNews userNews = userNewsList.get(activeUserNews);
            userNews.setViewed(true);
            expandNewsHandler.onNewsClicked(userNews);
        });
    }

    private VerticalLayout createNewsCarousel()
    {
        final VerticalLayout newsWrapper = new VerticalLayout();
        newsWrapper.setSizeFull();
        newsWrapper.setStyleName(CssStyle.NEWS_WRAPPER.getStyleName());

        final HorizontalLayout header = new HorizontalLayout();
        header.setSizeFull();

        final VerticalLayout panelTitle = new VerticalLayout();
        panelTitle.setSizeFull();
        panelTitle.addComponent(new Label(I18N.NEWS_COMPONENT_PANELTITLE.msg()));
        panelTitle.addStyleName(CssStyle.PANEL_HEADER.getStyleName());
        panelTitle.addStyleName(CssStyle.NEWS_BLUE.getStyleName());

        btnNewsForward.addStyleName(CssStyle.CAROUSEL_BUTTONS.getStyleName());
        btnNewsBackward.addStyleName(CssStyle.CAROUSEL_BUTTONS.getStyleName());

        header.addComponents(panelTitle, btnNewsBackward, btnNewsForward);
        header.setExpandRatio(panelTitle, 0.5f);
        header.setComponentAlignment(btnNewsForward, Alignment.BOTTOM_CENTER);
        header.setComponentAlignment(btnNewsBackward, Alignment.BOTTOM_CENTER);


        newsTitle.setStyleName(CssStyle.TITLE.getStyleName());
        newsTeaser.setStyleName(CssStyle.TEASER.getStyleName());
        newsText.setStyleName(CssStyle.TEXT.getStyleName());

        VerticalLayout newsLayout = new VerticalLayout();
        newsLayout.addStyleName(CssStyle.PANEL_CONTENT.getStyleName());

        newsText.setContentMode(ContentMode.HTML);

        newsLayout.addComponent(newsTitle);
        newsLayout.addComponent(newsTeaser);

        newsWrapper.addComponents(header, newsLayout);

        final HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSizeFull();
        buttons.addStyleName(CssStyle.NEWS_WRAPEPR_BUTTONS.getStyleName());
        buttons.addComponents(btnExpandNewsText);
        buttons.setComponentAlignment(btnExpandNewsText, Alignment.MIDDLE_LEFT);

        newsWrapper.addComponent(buttons);
        newsWrapper.setComponentAlignment(buttons, Alignment.BOTTOM_CENTER);

        return newsWrapper;
    }


    public void setUserNewsList(List<UserNews> userNewsList)
    {
        this.userNewsList = userNewsList;
        if (userNewsList != null && !userNewsList.isEmpty())
        {
            initNews();
            createNewsLayout();
        }
        else
            wrapper.removeAllComponents();

    }
}
