package de.binaerebauten.gleichklang.memberweb.view;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.service.RelationshipService;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.FormPanel;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.navigation.DefaultNavigatorFactory;
import de.binaerebauten.gleichklang.memberweb.view.StatisticView.StatisticViewListener;
import de.binaerebauten.gleichklang.memberweb.view.component.GenericViewHeader;

import java.util.Map;

@SuppressWarnings("serial")
public class StatisticViewImpl extends AbstractNavigateView<StatisticViewListener> implements StatisticView
{
	private final FormPanel statisticsFormPanel;
	private final FormPanel matchFormPanel;
	private final FormPanel receivedFormPanel;
	private final FormPanel sentFormPanel;
    private  int numberOfSuggestion;
	public StatisticViewImpl()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setStyleName(CssStyle.STATISTICS_WRAPPER.getStyleName());

		statisticsFormPanel = new FormPanel(I18N.STATISTIC_CAPTION_USER_STATISTICS.msg());
		statisticsFormPanel.setStyleName(CssStyle.STATISTICS_PANEL.getStyleName());
		statisticsFormPanel.addStyleName(CssStyle.GK_PANEL.getStyleName());

		matchFormPanel = new FormPanel(I18N.STATISTIC_CAPTION_MATCHES.msg());
		matchFormPanel.setStyleName(CssStyle.STATISTICS_PANEL.getStyleName());
		matchFormPanel.addStyleName(CssStyle.GK_PANEL.getStyleName());

		receivedFormPanel = new FormPanel(I18N.STATISTIC_CAPTION_RECEIVED.msg());
		receivedFormPanel.setStyleName(CssStyle.STATISTICS_PANEL.getStyleName());
		receivedFormPanel.addStyleName(CssStyle.GK_PANEL.getStyleName());

		sentFormPanel = new FormPanel(I18N.STATISTIC_CAPTION_SENT.msg());
		sentFormPanel.setStyleName(CssStyle.STATISTICS_PANEL.getStyleName());
		sentFormPanel.addStyleName(CssStyle.GK_PANEL.getStyleName());

		final GenericViewHeader header = new GenericViewHeader();
		header.addStyleName(CssStyle.GENERIC_HEADER_BLUE.getStyleName());
		header.setCaption(DefaultNavigatorFactory.MemberMenuItem.STATISTIC.toString());
		header.setDescription(I18N.USERDATA_STATISTICS_DESCRIPTION.msg());
		header.setIcon(new ThemeResource("img/statistics-outline.svg"));

		layout.addComponents(header,statisticsFormPanel, matchFormPanel, receivedFormPanel, sentFormPanel);

		setCompositionRoot(layout);
	}

	@Override
	public void setNoOfMatches(Map<RecommendationCategory, Long> values)
	{
		setDataToFormPanel(values, matchFormPanel,true);
	}

	@Override
	public void setNoOfReceivedMessages(Map<RecommendationCategory, Long> values)
	{
		setDataToFormPanel(values, receivedFormPanel,false);
	}

	@Override
	public void setNoOfSentMessages(Map<RecommendationCategory, Long> values)
	{
		setDataToFormPanel(values, sentFormPanel,false);
	}

	@Override
	public void setStatistics(Map<RelationshipService.StatisticData, String> statistics)
	{
		setDataToFormPanelStatisticData(statistics, statisticsFormPanel);

		if(numberOfSuggestion<20)
		{
			statisticsFormPanel.setVisible(false);
		}
	}

	private void setDataToFormPanel(Map<RecommendationCategory, Long> values, FormPanel formPanel,boolean isSuggestions)
	{
		formPanel.removeAllComponents();

		if(values != null)
		{
			for (Map.Entry<RecommendationCategory, Long> value : values.entrySet()) {
				final Long longValue = value.getValue();
				if(isSuggestions)
				{
					numberOfSuggestion = longValue.intValue() + numberOfSuggestion;
				}
				final RecommendationCategory category = value.getKey();
				final Label labelField = new Label();
				labelField.setCaption(category.getName());
				labelField.setValue(longValue.toString());
				formPanel.addFormElement(labelField);
			}
		}

	}
	private void setDataToFormPanelStatisticData(Map<RelationshipService.StatisticData, String> statistics, FormPanel formPanel)
	{
		if(statistics != null)
		{

			for (Map.Entry<RelationshipService.StatisticData, String> value : statistics.entrySet()) {
				final String valueStatistic = value.getValue();
				final RelationshipService.StatisticData statisticData = value.getKey();

				if("%-als ERSTER angeschriebener Vorschläge".equalsIgnoreCase(value.getKey().getName())) {
					final Label labelField = new Label();
					labelField.setCaption(statisticData.toString()+":");
					labelField.setValue(valueStatistic.toString().substring(0,valueStatistic.toString().lastIndexOf(" "))+" (empfohlener Prozentsatz: 30%)");
					formPanel.addFormElement(labelField);
				}
				if("%-Anteil, dass auf als ERSTER versendete Nachricht geantwortet wurde".equalsIgnoreCase(value.getKey().getName())){
					final Label labelField = new Label();
					labelField.setCaption(statisticData.toString()+":");
					labelField.setValue(valueStatistic.toString().substring(0,valueStatistic.toString().lastIndexOf(" "))+" (durchschnittlicher Prozentsatz: 60%)");
					formPanel.addFormElement(labelField);
				}
				if("%-überhaupt einen erhaltenen Vorschlag anschauen".equalsIgnoreCase(value.getKey().getName())) {
					final Label labelField = new Label();
					labelField.setCaption(statisticData.toString()+":");
					labelField.setValue(valueStatistic.toString().substring(0,valueStatistic.toString().lastIndexOf(" "))+" (empfohlener Prozentsatz: 90%)");
					formPanel.addFormElement(labelField);
				}

			}

		}
	}

}