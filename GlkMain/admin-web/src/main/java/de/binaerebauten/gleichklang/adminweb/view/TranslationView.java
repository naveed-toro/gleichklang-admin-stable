package de.binaerebauten.gleichklang.adminweb.view;

import de.binaerebauten.gleichklang.adminweb.view.TranslationView.TranslationViewListener;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;

public interface TranslationView extends NavigateView<TranslationViewListener>
{
	interface TranslationViewListener extends NavigateView.NavigateViewListener
	{
		void editI18NEntity(I18NEntity i18NEntity);
	}

	void setTranslationHandler(LazyBeanFilteredItemsHandler<I18NEntity> handler);
}
