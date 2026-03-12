package de.binaerebauten.gleichklang.core.view.component.user;

import com.vaadin.data.Validator;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;
import de.binaerebauten.gleichklang.core.model.user.RecommendationBreak;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class RecommendationBreakComponent extends CssLayout
{
	private final RecommendationCategory category;
	private final CheckBox checkBox;
	private final DateField dateField;
	
	private RecommendationBreakComponent(RecommendationCategory category, boolean enabled, LocalDate recommendationBreak)
	{
		this.category = category;
		this.checkBox = ComponentFactory.getInstance().createField(CheckBox.class);
		this.dateField = ComponentFactory.getInstance().createField(LocalDate.class, DateField.class);
		
		this.setStyleName(CssStyle.RECOMMENDATIONBREAK_COMPONENT.getStyleName());
		this.setSizeFull();
		
		final CssLayout firstColumn = new CssLayout();
		final CssLayout secondColumn = new CssLayout();
		final Label categoryLabel = new Label(category.getName());
		
		firstColumn.addComponents(categoryLabel, checkBox);
		final Label dateFieldLabel = new Label(I18N.RECOMMENDATIONBREAK_CAPTION_DATEFIELD.msg());
		secondColumn.addComponents(dateFieldLabel, dateField);
		
		this.addComponents(firstColumn, secondColumn);
		
		checkBox.addValueChangeListener((event) -> dateField.setEnabled(checkBox.getValue()));
		dateField.addValidator(value ->
		{
			if (value != null && LocalDate.now().isAfter((LocalDate) value))
			{
				throw new Validator.InvalidValueException(I18N.RECOMMENDATIONBREAK_VALIDATION_DATEFIELD.msg());
			}
		});
		dateField.addValueChangeListener((event) ->
		{
			dateField.removeStyleName(CssStyle.EMPTY_ANSWER.getStyleName());
			dateField.addStyleName(CssStyle.ANSWERED.getStyleName());
		});
		dateField.setEnabled(false);
		
		checkBox.setValue(enabled);
		dateField.setConvertedValue(recommendationBreak);
		
		if (checkBox.getValue())
			dateField.addStyleName(CssStyle.ANSWERED.getStyleName());
		else
			dateField.addStyleName(CssStyle.EMPTY_ANSWER.getStyleName());
	}
	
	private RecommendationCategory getCategory()
	{
		return category;
	}
	
	private LocalDate getEndDate()
	{
		return (LocalDate) dateField.getConvertedValue();
	}
	
	private boolean isRecommendationBreakActive()
	{
		return checkBox.getValue();
	}
	
	public static Map<RecommendationCategory, LocalDate> getRecommendationBreaks(Collection<RecommendationBreakComponent> recommendationBreakComponents)
	{
		final Map<RecommendationCategory, LocalDate> recommendationBreaks = new HashMap<>();
		for (RecommendationBreakComponent recommendationBreakComponent : recommendationBreakComponents)
		{
			if (recommendationBreakComponent.isRecommendationBreakActive())
			{
				recommendationBreaks.put(recommendationBreakComponent.getCategory(), recommendationBreakComponent.getEndDate());
			}
		}
		return recommendationBreaks;
	}
	
	public static List<RecommendationBreakComponent> createRecommendationBreakComponents(Map<RecommendationCategory, Boolean> categories, Map<RecommendationCategory, LocalDate> recommendationBreaks)
	{
		return categories.keySet().stream()
				.map(c -> new RecommendationBreakComponent(c, categories.get(c), recommendationBreaks.get(c)))
				.collect(Collectors.toList());
	}
	
	public static List<RecommendationBreakComponent> createRecommendationBreakComponents(Collection<RecommendationBreak> recommendationBreaks)
	{
		return recommendationBreaks.stream()
				.map(rb -> new RecommendationBreakComponent(rb.getCategory(), true, rb.getEndDate()))
				.collect(Collectors.toList());
	}
}
