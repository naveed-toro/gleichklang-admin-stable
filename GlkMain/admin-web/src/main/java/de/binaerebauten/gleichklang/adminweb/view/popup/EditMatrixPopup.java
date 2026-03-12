package de.binaerebauten.gleichklang.adminweb.view.popup;

//import com.sun.prism.Texture;
import com.vaadin.ui.*;
import com.vaadin.ui.Notification.Type;
import de.binaerebauten.gleichklang.core.model.matching.MatchingMatrix;
import de.binaerebauten.gleichklang.core.model.matching.MatrixValue;
import de.binaerebauten.gleichklang.core.model.matching.MatrixValue.Strictness;
import de.binaerebauten.gleichklang.core.model.questionnaire.Choice;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.Popup;

import java.util.HashMap;
import java.util.Map;

public class EditMatrixPopup extends Popup
{
	public interface EditMatrixCallback
	{
		void saveMatrix(MatchingMatrix matchingMatrix) throws ValidationException;
	}

	private final MatchingMatrix matchingMatrix;
	private final Map<Choice, Map<Choice, ComboBox>> strictnessComboBoxes;

	public EditMatrixPopup(MatchingMatrix matchingMatrix, EditMatrixCallback editMatrixCallback)
	{
		this.matchingMatrix = matchingMatrix;
		this.strictnessComboBoxes = createStrictnessComboBoxes();

		setCaption(I18N.EDITMATRIXPOPUP_CAPTION_TITLE.msg(matchingMatrix.getName()));

		TextArea matrixName = new TextArea();
		matrixName.setWidth("500px");
		matrixName.setHeight("50px");
		matrixName.setValue(matchingMatrix.getName());

		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);

		layout.addComponent(matrixName);

		final Component valueTable = createTable();

		final Button saveButton = createSaveButton(editMatrixCallback,matrixName);
		layout.addComponents(saveButton, valueTable);
		setContent(layout);
	}

	private Map<Choice, Map<Choice, ComboBox>> createStrictnessComboBoxes()
	{
		final Map<Choice, Map<Choice, ComboBox>> strictnessComboBoxes = new HashMap<>();
		for (MatrixValue matrixValue : matchingMatrix.getMatrixValues())
		{
			Map<Choice, ComboBox> row = strictnessComboBoxes.get(matrixValue.getTargetChoice());
			if (row == null)
			{
				row = new HashMap<>();
				strictnessComboBoxes.put(matrixValue.getTargetChoice(), row);
			}

			final ComboBox strictnessComboBox = ComponentFactory.getInstance().createField(Strictness.class, ComboBox.class);
			strictnessComboBox.setValue(matrixValue.getStrictness());

			row.put(matrixValue.getSourceChoice(), strictnessComboBox);
		}
		return strictnessComboBoxes;
	}

	private Table createTable()
	{
		final Table table = new Table();
		table.setSizeFull();

		table.addContainerProperty(matchingMatrix.getTargetChoiceGroup().getName() + " \\ " + matchingMatrix.getSourceChoiceGroup().getName(), String.class, "");
		for (Choice sourceChoice : matchingMatrix.getSourceChoiceGroup().getChoices())
		{
			table.addContainerProperty(sourceChoice.getName(), ComboBox.class, null);
		}

		for (Choice targetChoice : matchingMatrix.getTargetChoiceGroup().getChoices())
		{
			final Object[] objectRow = new Object[matchingMatrix.getSourceChoiceGroup().getChoices().size() + 1];
			int i = 0;
			objectRow[i++] = targetChoice.getName();
			for (Choice sourceChoice : matchingMatrix.getSourceChoiceGroup().getChoices())
			{
				objectRow[i++] = strictnessComboBoxes.get(targetChoice).get(sourceChoice);
			}

			table.addItem(objectRow, null);
		}
		table.setPageLength(matchingMatrix.getTargetChoiceGroup().getChoices().size());

		return table;
	}

	private Button createSaveButton(EditMatrixCallback editMatrixCallback, TextArea matrixName)
	{
		final Button button = new Button(I18N.EDITMATRIXPOPUP_ACTION_SAVE.msg());
		button.addClickListener(event ->
		{
			for (MatrixValue matrixValue : matchingMatrix.getMatrixValues())
			{
				Map<Choice, ComboBox> row = strictnessComboBoxes.get(matrixValue.getTargetChoice());
				ComboBox strictnessComboBox = row.get(matrixValue.getSourceChoice());
				matrixValue.setStrictness((Strictness) strictnessComboBox.getValue());
			}

			try
			{
				matchingMatrix.setName(matrixName.getValue());
				editMatrixCallback.saveMatrix(matchingMatrix);
				close();
			}
			catch (UniqueValidationException e)
			{
				Notification.show(I18N.EDITMATRIXPOPUP_VALIDATION_UNQIUENAME.msg(), Type.ERROR_MESSAGE);
			}
			catch (ValidationException e)
			{
				Notification.show(e.getLocalizedMessage(), Type.ERROR_MESSAGE);
			}
		});
		return button;
	}
}