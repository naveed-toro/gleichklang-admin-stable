package de.binaerebauten.gleichklang.memberweb.view;

import com.vaadin.ui.Field;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.component.Savable;
import de.binaerebauten.gleichklang.memberweb.view.PersonalDataView.PersonalDataViewListener;
import de.binaerebauten.gleichklang.memberweb.view.component.PersonalDataComponent.PersonalDataHandler;

import java.util.Collection;

public interface PersonalDataView extends NavigateView<PersonalDataViewListener>, Savable
{
	interface PersonalDataViewListener extends NavigateView.NavigateViewListener
	{
	}
	
	void setPersonalDataComponent(PersonalDataHandler personalDataHandler);
	
	Collection<Field<?>> getFields();
}
