package de.binaerebauten.gleichklang.memberweb.presenter;

import com.vaadin.server.StreamResource;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.service.report.UserProfileReportService;
import de.binaerebauten.gleichklang.core.utils.pdf.StyledDocument;
import de.binaerebauten.gleichklang.core.view.component.NavigationEnum;
import de.binaerebauten.gleichklang.core.view.component.OnDemandStreamSource;
import de.binaerebauten.gleichklang.memberweb.view.RegistrationReportingView;
import org.springframework.context.ApplicationContext;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class RegistrationReportingPresenter extends NavigatePresenter implements RegistrationReportingView.RegistrationReportingListener
{
	private final RegistrationReportingView view;
	private final UserProfileReportService userProfileReportService;
	private final User currentUser;

	public RegistrationReportingPresenter(ApplicationContext ctx, RegistrationReportingView registrationReportingView){
		super(registrationReportingView);
		this.view = registrationReportingView;
		this.userProfileReportService = ctx.getBean(UserProfileReportService.class);
		this.currentUser = ctx.getBean(UserService.class).getCurrentUser();
		registrationReportingView.setListener(this);
	}
	
	@Override
	public void enter(String parameters)
	{
		final RecommendationCategory recommendationCategory = this.view.getRecommendationCategory();
		this.view.setReportingLinks(getSourceMap(recommendationCategory));
	}

	private Map<String, StreamResource> getSourceMap(RecommendationCategory recommendationCategory)
	{
        Map<String, StreamResource> sourceMap = new LinkedHashMap<>();
        final Set<RecommendationCategory> categories = currentUser.getCategories();


        if (recommendationCategory != null) {
            if (recommendationCategory.equals(RecommendationCategory.FRIENDSHIP) && categories.contains(RecommendationCategory.PARTNERSHIP)) {
                sourceMap.put(RecommendationCategory.PARTNERSHIP.msg(), getStreamSource(RecommendationCategory.PARTNERSHIP, currentUser));
            }
            sourceMap.put(recommendationCategory.msg(), getStreamSource(recommendationCategory, currentUser));
        } else {
            for (RecommendationCategory category : categories) {
                sourceMap.put(category.msg(), getStreamSource(category, currentUser));
            }
            sourceMap.put(I18N.HOMEPRESENTER_SOCIAL_PROFILE.msg(), new StreamResource(new OnDemandStreamSource() {
                @Override
                public String getFileName() {
                    return I18N.HOMEPRESENTER_SOCIAL_PROFILE_FILENAME.msg(".pdf");
                }

                @Override
                public String getMimeType() {
                    return "application/pdf";
                }

                @Override
                public InputStream getStream() {
                    return new ByteArrayInputStream(userProfileReportService.getGesellschaftProfileForUser(currentUser).getOutputStream().toByteArray());
                }
            }, I18N.HOMEPRESENTER_SOCIAL_PROFILE_FILENAME.msg(".pdf")));
            sourceMap.put(I18N.HOMEPRESENTER_PERSONAL_PROFILE.msg(), new StreamResource(new OnDemandStreamSource() {
                @Override
                public String getFileName() {
                    return I18N.HOMEPRESENTER_PERSONAL_PROFILE_FILENAME.msg(".pdf");
                }

                @Override
                public String getMimeType() {
                    return "application/pdf";
                }

                @Override
                public InputStream getStream() {
                    return new ByteArrayInputStream(userProfileReportService.getPersoenlichkeitProfileForUser(currentUser).getOutputStream().toByteArray());
                }
            }, I18N.HOMEPRESENTER_PERSONAL_PROFILE_FILENAME.msg(".pdf")));
        }

		return sourceMap;
	}

	private StreamResource getStreamSource(final RecommendationCategory category, final User user)
	{
        final String filename = getTranslatedReportFileName(category);
        return new StreamResource(new OnDemandStreamSource() {
            @Override
            public String getFileName() {
                return filename;
            }

            @Override
            public String getMimeType() {
                return "application/pdf";
            }

            @Override
            public InputStream getStream() {
                return new ByteArrayInputStream(getReportingComponentForCategory(category, user).getOutputStream().toByteArray());
            }
        }, filename);
	}

	private StyledDocument getReportingComponentForCategory(RecommendationCategory category, User user)
	{
		switch (category){
			case FRIENDSHIP:
				return  userProfileReportService.getFreundschaftProfileForUser(user);
			case PARTNERSHIP:
				return userProfileReportService.getPartnerschaftProfileForUser(user);
		}
		return null;
	}

	private String getTranslatedReportFileName(RecommendationCategory category) {
        switch (category) {
            case FRIENDSHIP:
                return I18N.HOMEPRESENTER_FRIENDSHIP_PROFILE_FILENAME.msg(".pdf");
            case PARTNERSHIP:
                return I18N.HOMEPRESENTER_PARTNER_PROFILE_FILENAME.msg(".pdf");
        }

        return "report.pdf";
    }

	@Override
	public void leave()
	{

	}
	
	@Override
	public void navigateTo(NavigationEnum... navigationEnums)
	{
		//TODO not necessary here... should be refactored with big registration refactoring
	}
	
	@Override
	public RegistrationReportingView getView()
	{
		return view;
	}
}
