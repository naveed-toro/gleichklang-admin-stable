package de.binaerebauten.gleichklang.adminweb.presenter;

import de.binaerebauten.gleichklang.adminweb.service.TranslationService;
import de.binaerebauten.gleichklang.adminweb.view.TranslationView;
import de.binaerebauten.gleichklang.adminweb.view.TranslationView.TranslationViewListener;
import de.binaerebauten.gleichklang.adminweb.view.popup.TranslationPopup;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import org.springframework.context.ApplicationContext;

import java.util.Collection;

public class TranslationPresenter extends NavigatePresenter implements TranslationViewListener
{
	private final TranslationView view;
	private final TranslationService translationService;

	public TranslationPresenter(ApplicationContext ctx, TranslationView view)
	{
		super(view);
		
		this.view = view;
		
		translationService = ctx.getBean(TranslationService.class);

		view.setListener(this);
	}

	@Override
	public void enter(String parameters)
	{
		refreshView();
	}

	@Override
	public void editI18NEntity(I18NEntity i18NEntity)
	{
		final TranslationPopup popup = new TranslationPopup(translationService.getI18NEntitiesForAllLanguages(
						i18NEntity.getBaseName(), i18NEntity.getKey()), this::saveEntities);
		tryOpenPopup(popup);
	}
	
	private void saveEntities(Collection<I18NEntity> i18NEntities)
	{
		translationService.saveAndFlush(i18NEntities);
		refreshView();
	}
	
	private void refreshView()
	{
		view.setTranslationHandler(translationService::getI18NEntities);
	}
	
}
