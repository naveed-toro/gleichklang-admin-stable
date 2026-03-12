package de.binaerebauten.gleichklang.memberweb.view;

import com.vaadin.ui.Field;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.commit_strategy.HideUnsavedNotificationValidationStrategy;
import de.binaerebauten.gleichklang.memberweb.view.component.PersonalDataComponent;
import de.binaerebauten.gleichklang.memberweb.view.component.PersonalDataComponent.PersonalDataHandler;
import de.binaerebauten.gleichklang.memberweb.view.PersonalDataView.PersonalDataViewListener;

import java.util.Collection;
import java.util.Collections;

public class PersonalDataViewImpl extends AbstractNavigateView<PersonalDataViewListener> implements PersonalDataView
{
	private PersonalDataComponent personalDataComponent = null;
	
	@Override
	public void setPersonalDataComponent(PersonalDataHandler personalDataHandler)
	{
		personalDataComponent = new PersonalDataComponent(new HideUnsavedNotificationValidationStrategy(), personalDataHandler);
		setCompositionRoot(personalDataComponent);
	}
	
	@Override
	public Collection<Field<?>> getFields()
	{
		if(personalDataComponent == null) return Collections.emptyList();
		return personalDataComponent.getFields();
	}
	
	@Override
	public void saveComplete(SaveResultListener saveResultListener)
	{
		personalDataComponent.saveComplete(saveResultListener);
	}
}
