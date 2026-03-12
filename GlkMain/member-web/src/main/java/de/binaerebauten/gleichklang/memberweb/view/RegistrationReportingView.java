package de.binaerebauten.gleichklang.memberweb.view;

import com.vaadin.server.StreamResource;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.component.OnDemandLink.OnDemandStreamSource;

import java.util.Map;

public interface RegistrationReportingView extends NavigateView<RegistrationReportingView.RegistrationReportingListener>
{
	interface RegistrationReportingListener
			extends NavigateView.NavigateViewListener
	{
		
		RegistrationReportingView getView();
	}

	void setReportingLinks(Map<String, StreamResource> streamSourceList);

	RecommendationCategory getRecommendationCategory();

	String getTemplateName();
}
