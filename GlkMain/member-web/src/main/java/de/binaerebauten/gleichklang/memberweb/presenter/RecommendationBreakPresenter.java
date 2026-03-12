package de.binaerebauten.gleichklang.memberweb.presenter;

import com.vaadin.ui.Notification;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.memberweb.view.RecommendationBreakView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by rgoerner on 20.09.16.
 */
public class RecommendationBreakPresenter extends NavigatePresenter implements RecommendationBreakView.RecommendationBreakViewListener
{
    private static final Logger LOG = LoggerFactory.getLogger(RecommendationBreakPresenter.class);

    private final RecommendationBreakView view;
    private final UserService userService;

    public RecommendationBreakPresenter(ApplicationContext ctx, RecommendationBreakView view)
    {
        super(view);

        this.view = view;
        userService = ctx.getBean(UserService.class);

        this.view.setListener(this);
    }

    private void refreshRecommendationBreak(User currentUser)
    {
        final Map<RecommendationCategory, LocalDate> recommendationBreaks = userService.getRecommendationBreaks(currentUser);

        final Set<RecommendationCategory> visibleCategories = currentUser.getCategories();
        final SortedMap<RecommendationCategory, Boolean> breakCategories = new TreeMap<>();

        for(RecommendationCategory category : visibleCategories)
        {
            breakCategories.put(category, recommendationBreaks.containsKey(category));
        }
        this.view.setRecommendationBreaks(breakCategories, recommendationBreaks);
    }

    @Override
    public void enter(String parameters)
    {
        final User currentUser = userService.getCurrentUser();

        refreshRecommendationBreak(currentUser);
        this.view.reset();
    }

    @Override
    public void saveRecommendationBreaks(Map<RecommendationCategory, LocalDate> recommendationBreaks)
    {
        final User currentUser = userService.getCurrentUser();
        userService.saveRecommendationBreaks(currentUser, recommendationBreaks);
        Notification.show(de.binaerebauten.gleichklang.core.view.I18N.DATA_SAVED.msg(), Notification.Type.TRAY_NOTIFICATION);
    }
}
