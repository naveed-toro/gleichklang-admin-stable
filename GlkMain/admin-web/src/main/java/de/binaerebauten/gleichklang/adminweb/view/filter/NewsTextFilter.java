package de.binaerebauten.gleichklang.adminweb.view.filter;

import de.binaerebauten.gleichklang.core.model.news.News;
import de.binaerebauten.gleichklang.core.model.news.News_;
import de.binaerebauten.gleichklang.core.view.filter.SimpleStringFilter;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.util.Arrays;
import java.util.Collection;

/**
 * Filters {@link News} entities over the title of the news.
 *
 */
public class NewsTextFilter extends SimpleStringFilter<News>
{
	@Override
	protected Collection<Path<String>> getSimplePath(Root<News> root)
	{
		final Path<String> titlePath = root.get(News_.title);
		final Path<String> textPath = root.get(News_.text);
		final Path<String> teaserPath = root.get(News_.teaserText);
		
		return Arrays.asList(titlePath, textPath, teaserPath);
	}
}
