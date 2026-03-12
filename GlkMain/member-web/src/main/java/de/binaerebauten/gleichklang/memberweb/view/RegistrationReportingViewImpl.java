package de.binaerebauten.gleichklang.memberweb.view;

import com.vaadin.server.StreamResource;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.HTMLLayout;
import de.binaerebauten.gleichklang.memberweb.view.RegistrationReportingView.RegistrationReportingListener;
import de.binaerebauten.gleichklang.core.view.component.FileViewerComponent;

import java.util.Map;

import static de.binaerebauten.gleichklang.memberweb.view.component.RegistrationWizard.PERSONAL_STEP;
import static de.binaerebauten.gleichklang.memberweb.view.component.RegistrationWizard.POST_TEMPLATE_SUFFIX;

public class RegistrationReportingViewImpl  extends AbstractNavigateView<RegistrationReportingListener> implements RegistrationReportingView
{
	private final VerticalLayout root;
	private final RecommendationCategory recommencationCategory;
	private final String templateName;
	private Panel reportingPanel;

	public RegistrationReportingViewImpl(RecommendationCategory recommendationCategory){
		this.root = new VerticalLayout();
		this.reportingPanel = new Panel();
		this.recommencationCategory = recommendationCategory;
		this.templateName = getTemplateName(recommendationCategory);
		final HTMLLayout templateLayout = new HTMLLayout(templateName + POST_TEMPLATE_SUFFIX);
		root.addComponents(templateLayout, reportingPanel);
		setCompositionRoot(root);
	}

	@Override
	public void setReportingLinks(Map<String, StreamResource> sourceMap){
		final Panel profileDownloaderComponent = new FileViewerComponent(sourceMap);
		profileDownloaderComponent.setCaption(I18N.HOMEVIEW_PROFILE_DOWNLOAD_TITLE.msg());
		profileDownloaderComponent.setDescription(I18N.HOMEVIEW_PROFILE_DOWNLOAD_DESCRIPTION.msg());

		root.replaceComponent(this.reportingPanel, profileDownloaderComponent);
		this.reportingPanel = profileDownloaderComponent;
	}

	@Override
	public RecommendationCategory getRecommendationCategory()
	{
		return this.recommencationCategory;
	}

	private String getTemplateName(RecommendationCategory category)
	{
		String prefix;
		if(category == null) {
			prefix = PERSONAL_STEP.toLowerCase();
		}
		else{
			prefix = category.name().toLowerCase();
		}

		return prefix;
	}

	@Override
	public String getTemplateName(){
		return templateName;
	}


}
