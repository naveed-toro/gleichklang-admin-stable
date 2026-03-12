package de.binaerebauten.gleichklang.adminweb.view;


import de.binaerebauten.gleichklang.adminweb.view.NewsView.NewsViewListener;
import de.binaerebauten.gleichklang.core.model.news.News;
import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer;

/**
 * Created by rgoerner on 10.12.15.
 */
public interface NewsView extends NavigateView<NewsViewListener>
{
    interface NewsViewListener extends NavigateView.NavigateViewListener
    {
        void editNews(News news);
        void createNews();
        void deleteNews(News news);
        void sendNewsViaEmail(News news);
    }

    void setNewsHandler(LazyBeanItemContainer.LazyBeanFilteredItemsHandler<News> handler);

    void disableSendEmailButton();
}
