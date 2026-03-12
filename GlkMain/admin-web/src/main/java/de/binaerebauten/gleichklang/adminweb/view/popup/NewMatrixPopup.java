package de.binaerebauten.gleichklang.adminweb.view.popup;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.core.model.matching.MatchingMatrix;
import de.binaerebauten.gleichklang.core.model.matching.MatchingMatrix_;
import de.binaerebauten.gleichklang.core.model.questionnaire.ChoiceGroup;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.Popup;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;

import java.util.Collection;
import java.util.Objects;

public class NewMatrixPopup extends Popup
{
	public interface NewMatrixCallback
	{
		void saveMatrix(MatchingMatrix matchingMatrix) throws ValidationException;
	}
	
	private final NewMatrixCallback newMatrixCallback;
	private final ComponentGroup<MatchingMatrix> componentGroup;
	
	public NewMatrixPopup(MatchingMatrix matchingMatrix, Collection<ChoiceGroup> choiceGroups, NewMatrixCallback newMatrixCallback)
	{
		Objects.requireNonNull(newMatrixCallback);
		this.newMatrixCallback = newMatrixCallback;
		
		componentGroup = new ComponentGroup<>(MatchingMatrix.class, matchingMatrix);
		
		setCaption(I18N.NEWMATRIXPOPUP_CAPTION_TITLE.msg());
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);
		
		final Component nameTextField = componentGroup.buildAndBind(true, I18N.NEWMATRIXPOPUP_CAPTION_NAME.msg(), MatchingMatrix_.name);
		final Component sourceChoiceValueGroup = createChoiceValueGroupComboBox(choiceGroups, I18N.NEWMATRIXPOPUP_CAPTION_SOURCE.msg(), MatchingMatrix_.sourceChoiceGroup);
		final Component targetChoiceValueGroup = createChoiceValueGroupComboBox(choiceGroups, I18N.NEWMATRIXPOPUP_CAPTION_TARGET.msg(), MatchingMatrix_.targetChoiceGroup);
		
		final SaveHelper saveHelper = new SaveHelper(this::save);
		saveHelper.setShowUnsavedNotification(false);
		saveHelper.addFields(componentGroup);
		
		layout.addComponents(saveHelper.getValidationComponent(), nameTextField, sourceChoiceValueGroup, targetChoiceValueGroup, saveHelper.getSaveButton());
		setContent(layout);
	}
	
	private void save() throws ValidationException
	{
		newMatrixCallback.saveMatrix(componentGroup.getItemDataSource().getBean());
		close();
	}
	
	private ComboBox createChoiceValueGroupComboBox(Collection<ChoiceGroup> choiceGroups, String caption, javax.persistence.metamodel.Attribute<?, ?> attribute)
	{
		final BeanItemContainer<ChoiceGroup> choiceGroupContainer = new BeanItemContainer<>(ChoiceGroup.class, choiceGroups);
		final ComboBox choiceValueGroup = componentGroup.buildAndBind(true, caption, ComboBox.class, attribute);
		
		choiceValueGroup.setContainerDataSource(choiceGroupContainer);
		choiceValueGroup.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		choiceValueGroup.setItemCaptionPropertyId(ChoiceGroup.NAME);
		
		return choiceValueGroup;
	}
}
