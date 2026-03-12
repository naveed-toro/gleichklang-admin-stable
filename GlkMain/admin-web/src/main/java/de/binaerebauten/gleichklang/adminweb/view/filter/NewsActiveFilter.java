package de.binaerebauten.gleichklang.adminweb.view.filter;

import de.binaerebauten.gleichklang.core.model.news.News;
import de.binaerebauten.gleichklang.core.model.news.News_;
import de.binaerebauten.gleichklang.core.view.filter.SimpleAttributeFilter;

/**
 * Filters {@link News} entities over the state "active" of the news.
 * Created by rgoerner on 16.12.15.
 */
public class NewsActiveFilter extends SimpleAttributeFilter<News, Boolean>
{
    public NewsActiveFilter()
    {
        super(News_.active);
    }
    
    public void setValue(boolean active, boolean inactive)
    {
        final Boolean value;
        value = active && inactive || !active && !inactive ? null : active;
        super.setValue(value);
    }
}
