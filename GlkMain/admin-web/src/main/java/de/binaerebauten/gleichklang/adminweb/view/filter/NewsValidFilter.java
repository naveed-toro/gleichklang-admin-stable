package de.binaerebauten.gleichklang.adminweb.view.filter;

import de.binaerebauten.gleichklang.core.model.news.News;
import de.binaerebauten.gleichklang.core.model.news.News_;
import de.binaerebauten.gleichklang.core.view.filter.AbstractFilter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;

/**
 * Filters {@link News} entities over a Date from which the news is valid.
 * Created by rgoerner on 16.12.15.
 */
public class NewsValidFilter extends AbstractFilter<News, LocalDateTime, LocalDateTime>
{
    public NewsValidFilter()
    {
        super(News_.validFrom);
    }
    
    @Override
    protected Predicate toPredicate(Path<LocalDateTime> path, CriteriaQuery<?> query, CriteriaBuilder cb)
    {
        return cb.greaterThanOrEqualTo(path, getValue());
    }
}
