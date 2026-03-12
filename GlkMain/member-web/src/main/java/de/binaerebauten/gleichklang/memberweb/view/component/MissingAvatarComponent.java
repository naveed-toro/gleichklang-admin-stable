package de.binaerebauten.gleichklang.memberweb.view.component;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.view.I18N;

import java.util.List;
import java.util.Objects;

public class MissingAvatarComponent extends CustomComponent
{
	public interface MissingAvatarHandler
	{
		void onAvatarClicked(RecommendationCategory recommendationCategory);
	}

	private final VerticalLayout wrapper;
	private final MissingAvatarHandler missingAvatarHandler;

	public MissingAvatarComponent(MissingAvatarHandler missingAvatarHandler)
	{
		Objects.requireNonNull(missingAvatarHandler);

		this.missingAvatarHandler = missingAvatarHandler;

		wrapper = new VerticalLayout();
		wrapper.setStyleName(CssStyle.MISSING_AVATAR_COMPONENT.getStyleName());
		setCompositionRoot(wrapper);
	}


	public void setMissingRecommendationCategories(List<RecommendationCategory> recommendationCategories)
	{
		wrapper.removeAllComponents();
		if(recommendationCategories.isEmpty())
		{
			wrapper.setVisible(false);
			return;
		}
		wrapper.addComponent(new Label(I18N.MISSING_AVATAR_COMPONENT.msg()));
		for(RecommendationCategory category : recommendationCategories)
		{
			final Button incompleteButton = new Button(I18N.MISSING_AVATAR_LABEL.msg() +" " +category.getName());
			incompleteButton.setIcon(new ThemeResource("img/default_avatar.svg"));
			incompleteButton.setStyleName(CssStyle.TEXT_BUTTON.getStyleName());
			incompleteButton.addClickListener(event -> missingAvatarHandler.onAvatarClicked(category));
			wrapper.addComponent(incompleteButton);
		}
	}
}
