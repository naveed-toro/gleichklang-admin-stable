package de.binaerebauten.gleichklang.adminweb.view.popup.usermanage;

import com.vaadin.ui.Button;
import de.binaerebauten.gleichklang.adminweb.view.popup.I18N;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.view.component.user.RecommendationBreakComponent;
import de.binaerebauten.gleichklang.core.view.popup.GenericPopup;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

public class RecommendationBreakPopup extends GenericPopup
{
	public interface SaveListener
	{
		void save(Map<RecommendationCategory, LocalDate> recommendationBreak);
	}
	
	public RecommendationBreakPopup(RecommendationCategory category, SaveListener saveListener)
	{
		final RecommendationBreakComponent component = RecommendationBreakComponent.createRecommendationBreakComponents(Collections.singletonMap(category, false), Collections.emptyMap()).iterator().next();
		
		final Button saveButton = new Button(I18N.USERMANAGEPOPUP_ACTION_SAVERECOMMENDATIONBREAK.msg());
		saveButton.addClickListener(event ->
		{
			saveListener.save(RecommendationBreakComponent.getRecommendationBreaks(Collections.singleton(component)));
			close();
		});
		
		setPopupContent(component);
		setFooter(saveButton);
	}
}
