package de.binaerebauten.gleichklang.adminweb.view.popup;

import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.core.model.matching.AreaMatchStatisticEntry;
import de.binaerebauten.gleichklang.core.model.matching.MatchStatistic;
import de.binaerebauten.gleichklang.core.model.matching.MatchStatisticEntry;
import de.binaerebauten.gleichklang.core.utils.StringUtils;
import de.binaerebauten.gleichklang.core.view.component.FormPanel;
import de.binaerebauten.gleichklang.core.view.component.LabelField;
import de.binaerebauten.gleichklang.core.view.component.Popup;

import java.time.Duration;

public class MatchStatisticPopup extends Popup
{
	public MatchStatisticPopup(MatchStatistic matchStatistic)
	{
		setCaption(I18N.MATCHSTATISTICPOPUP_CAPTION_TITLE.msg(matchStatistic.getMatchingScope().toString()));
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);
		
		final Component areaMatchComponent = createAreaMatchComponent(matchStatistic);
		final Component matchComponent = createMatchComponent(matchStatistic);
		
		layout.addComponent(createGeneralInformationComponent(matchStatistic));
		if(areaMatchComponent != null) layout.addComponent(areaMatchComponent);
		if(matchComponent != null) layout.addComponent(matchComponent);
		
		setContent(layout);
	}
	
	private FormPanel createGeneralInformationComponent(MatchStatistic matchStatistic)
	{
		final FormPanel formPanel = new FormPanel(I18N.MATCHSTATISTICPOPUP_CAPTION_GENERALSTATISTIC.msg());
		formPanel.addFormElement(new LabelField(I18N.MATCHSTATISTICPOPUP_CAPTION_STARTDATE.msg(), StringUtils.timeToString(matchStatistic.getStartDate())));
		formPanel.addFormElement(new LabelField(I18N.MATCHSTATISTICPOPUP_CAPTION_ENDDATE.msg(), StringUtils.timeToString(matchStatistic.getEndDate())));
		formPanel.addFormElement(new LabelField(I18N.MATCHSTATISTICPOPUP_CAPTION_DURATION.msg(), StringUtils.durationToString(matchStatistic.getDuration())));
		
		return formPanel;
	}
	
	private FormPanel createMatchComponent(MatchStatistic matchStatistic)
	{
		if(matchStatistic.getMatchStatisticEntryMap().isEmpty()) return null;
		
		final FormPanel matchPanel = new FormPanel(I18N.MATCHSTATISTICPOPUP_CAPTION_MATCHSTATISTIC.msg());
		
		for (MatchStatisticEntry matchStatisticEntry : matchStatistic.getMatchStatisticEntryMap().values())
		{
			matchPanel.addFormElement(createLabel(matchStatisticEntry.getLogArea().toString(), matchStatisticEntry.getNumber(), matchStatisticEntry.getDuration()));
		}
		return matchPanel;
	}
	
	private FormPanel createAreaMatchComponent(MatchStatistic matchStatistic)
	{
		if(matchStatistic.getAreaMatchStatisticEntryMap().isEmpty()) return null;
		
		final FormPanel areaMatchPanel = new FormPanel(I18N.MATCHSTATISTICPOPUP_CAPTION_AREAMATCHSTATISTIC.msg());
		
		for (AreaMatchStatisticEntry areaMatchStatisticEntry : matchStatistic.getAreaMatchStatisticEntryMap().values())
		{
			areaMatchPanel.addFormElement(createLabel(areaMatchStatisticEntry.getLogMatchArea().toString(), areaMatchStatisticEntry.getNumber(), areaMatchStatisticEntry.getDuration()));
		}
		return areaMatchPanel;
	}
	
	private Component createLabel(String label, Long number, Duration duration)
	{
		final long s = duration.getSeconds();
		final long average = (long) (((double) s / (double) number) * 100000);
		
		return new LabelField(label, I18N.MATCHSTATISTICPOPUP_CAPTION_STATISTICVALUE.msg(number, StringUtils.durationToString(s), StringUtils.durationToString(average)));
	}
}
