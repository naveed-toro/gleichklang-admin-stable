package de.binaerebauten.gleichklang.core.view.component.question;

import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.locatable.ProximitySearchRequest;
import de.binaerebauten.gleichklang.core.model.locatable.Region;
import de.binaerebauten.gleichklang.core.model.locatable.RegionSearchRequest;
import de.binaerebauten.gleichklang.core.model.questionnaire.RegionAnswer;
import de.binaerebauten.gleichklang.core.model.questionnaire.RegionAnswer_;
import de.binaerebauten.gleichklang.core.model.questionnaire.RegionQuestion;
import de.binaerebauten.gleichklang.core.service.LocatableHandler;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.FormPanel;
import de.binaerebauten.gleichklang.core.view.component.LabelField;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import org.springframework.security.access.method.P;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class RegionAnswerComponent extends CustomComponent
{
	private static final int MAX_REGIONS = 10;
	private static final int MAX_PROXIMITIES = 10;
	
	private final ComponentContainer regionSearchRequestContainer = new VerticalLayout();
	private final ComponentContainer proximitySearchRequestContainer = new VerticalLayout();
	private final Label regionSearchInfo = new Label();
	private final Label proximitySearchInfo = new Label();
	private final AbstractField<?> regionSearchRequestCountField;
	private final AbstractField<?> proximitySearchRequestCountField;
	
	private final ComponentGroup<RegionAnswer> regionAnswerComponentGroup;
	private final LocatableHandler locatableHandler;
	
	private final Component relocationComponent;

	private final Label proximitySearchRequestContainerHeader = new Label();
	private final Label regionSearchRequestContainerHeader = new Label();


	public RegionAnswerComponent(RegionQuestion regionQuestion, ComponentGroup<RegionAnswer> componentGroup, LocatableHandler locatableHandler)
	{
		Objects.requireNonNull(regionQuestion);
		Objects.requireNonNull(componentGroup);
		Objects.requireNonNull(locatableHandler);
		
		this.locatableHandler = locatableHandler;
		this.regionAnswerComponentGroup = componentGroup;
		
		this.regionSearchRequestCountField = createSearchRequestCountField(I18N.REGIONANSWER_CAPTION_REGIONCOUNT, I18N.REGIONANSWER_ERROR_REGIONCOUNT, MAX_REGIONS);
		this.proximitySearchRequestCountField = createSearchRequestCountField(I18N.REGIONANSWER_CAPTION_PROXIMITYCOUNT, I18N.REGIONANSWER_ERROR_PROXIMITYCOUNT, MAX_PROXIMITIES);
		this.regionSearchRequestCountField.setStyleName("max-info-label");
		this.proximitySearchRequestCountField.setStyleName("max-info-label");

		relocationComponent = createRelocationComponent();
		relocationComponent.setStyleName(CssStyle.RELOCATION_COMPONENT.getStyleName());
		
		regionSearchRequestContainer.addStyleName(CssStyle.REGION_ANSWER_COMPONENT.getStyleName());
		proximitySearchRequestContainer.addStyleName(CssStyle.PROXIMITY_ANSWER_COMPONENT.getStyleName());
		
		regionSearchInfo.setStyleName(CssStyle.SEARCH_INFO_LABEL.getStyleName());
		proximitySearchInfo.setStyleName(CssStyle.SEARCH_INFO_LABEL.getStyleName());
		
		this.regionAnswerComponentGroup.addAdditionalFields(proximitySearchRequestCountField, regionSearchRequestCountField);

		setCompositionRoot(createLayout());
		
		relocationComponent.setVisible(regionQuestion.isWithRelocation());
	}
	
	private AbstractField<String> createSearchRequestCountField(I18N caption, I18N errorMsg, int maxValue)
	{
		final LabelField field = new LabelField(caption.msg());
		field.setConverter(Integer.class);
		field.addValidator(new IntegerRangeValidator(errorMsg.msg(maxValue), 0, maxValue));

		return field;
	}
	
	private Component createLayout()
	{
		final VerticalLayout layout = new VerticalLayout();
		
		final VerticalLayout regionQuestionComponentWrapper = new VerticalLayout();
		regionQuestionComponentWrapper.setStyleName(CssStyle.REGION_QUESTION_WRAPPER.getStyleName());
		
		final VerticalLayout proximityQuestionComponentWrapper = new VerticalLayout();
		proximityQuestionComponentWrapper.setStyleName(CssStyle.PROXIMITY_QUESTION_WRAPPER.getStyleName());
		
		regionQuestionComponentWrapper.addComponent(new RegionSearchRequestPanel(locatableHandler, this::addRegionSearchRequest));
		regionQuestionComponentWrapper.addComponents(regionSearchInfo, regionSearchRequestCountField, regionSearchRequestContainer);

		proximityQuestionComponentWrapper.addComponent(new ProximitySearchRequestPanel(locatableHandler, this::addProximitySearchRequest));
		proximityQuestionComponentWrapper.addComponents(proximitySearchInfo, proximitySearchRequestCountField, proximitySearchRequestContainer);

		layout.addComponent(relocationComponent);
		layout.addComponents(regionQuestionComponentWrapper, proximityQuestionComponentWrapper);
		
		return layout;
	}
	
	private Component createRelocationComponent()
	{
		final FormPanel relocatableComponents = new FormPanel(I18N.REGIONANSWER_CAPTION_RELOCATABLETITLE.msg());
		relocatableComponents.setDescription(I18N.REGIONANSWER_CAPTION_RELOCATABLEDESCRIPTION.msg());
		relocatableComponents.addStyleName("relocation-component");

		relocatableComponents.addFormElement(regionAnswerComponentGroup.buildAndBind(I18N.REGIONANSWER_CAPTION_ISRELOCATABLE.msg(), RegionAnswer_.relocatable));
		relocatableComponents.addFormElement(regionAnswerComponentGroup.buildAndBind(I18N.REGIONANSWER_CAPTION_SEARCHRELOCATABLE.msg(), RegionAnswer_.searchRelocatable));
		
		return relocatableComponents;
	}
	
	private void addRegionSearchRequest(RegionSearchRequest regionSearchRequest)
	{
		final RegionAnswer regionAnswer = regionAnswerComponentGroup.getItemDataSource().getBean();
        boolean duplicate=false;

		Map<String,String> regionSearchReqMap= new ConcurrentHashMap<>();

		List<RegionSearchRequest>  regionSearchRequests = new CopyOnWriteArrayList(regionAnswer.getRegionSearchRequests());

		//Process Previously added Region search request select continent,country,restriction stored in "regionAnswer" to store in "regionSearchReqMap".
		// So that with count so that if count >1 it means we have dulicate addresses.
		String regionString="";
		for (Region reg : regionSearchRequest.getRestrictions()) {

			regionString=regionSearchRequest.getContinent().getName().toLowerCase() + "-" +
					regionSearchRequest.getCountry().getName().toLowerCase() + "-" +
					reg.getName();

			regionSearchReqMap.put(regionString,regionString);
		}

		if(regionSearchRequests.size()>0) {

			//Process Last and Latest Region search request select continent,country,restriction stored in "regionSearchRequest" to store in "regionSearchReqMap".
			// So that with count so that if count >1 it means we have dulicate addresses.

			for (RegionSearchRequest regSearchReq : regionSearchRequests) {
				for (Region reg : regSearchReq.getRestrictions()) {

					regionString=regSearchReq.getContinent().getName().toLowerCase() + "-" +
							regSearchReq.getCountry().getName().toLowerCase() + "-" +
							reg.getName();

					if (!regionSearchReqMap.containsKey(regionString)) {

						regionSearchReqMap.put(regionString,regionString);
					}
					else
					{
						duplicate=true;
						MessageBox.show(I18N.DUPLICATE_ADDRESS_FOR_SELECTED_REGION_ERROR_MESSAGE.msg());
					}
				}
			}
		}

		if(!duplicate){
			regionAnswer.addRegionSearchRequest(regionSearchRequest);
			addRegionSearchRequestEntry(regionSearchRequest);
			refreshInfoLabels();
			refreshSearchRequestCountFields();
		}


	}
	
	private void addProximitySearchRequest(ProximitySearchRequest proximitySearchRequest)
	{
		final RegionAnswer regionAnswer = regionAnswerComponentGroup.getItemDataSource().getBean();
		regionAnswer.addProximitySearchRequest(proximitySearchRequest);

		addProximitySearchRequestEntry(proximitySearchRequest);
		refreshInfoLabels();
		refreshSearchRequestCountFields();

	}
	
	public void initComponent()
	{
		proximitySearchRequestContainer.removeAllComponents();
		regionSearchRequestContainer.removeAllComponents();
		
		proximitySearchRequestContainerHeader.setValue(I18N.PROXIMITYANSWER_HEADER_CAPTION.msg());
		proximitySearchRequestContainerHeader.addStyleName(CssStyle.HEADER_LABEL.getStyleName());
		
		regionSearchRequestContainerHeader.setValue(I18N.REGIONANSWER_HEADER_CAPTION.msg());
		regionSearchRequestContainerHeader.addStyleName(CssStyle.HEADER_LABEL.getStyleName());
		
		proximitySearchRequestContainer.addComponent(proximitySearchRequestContainerHeader);
		regionSearchRequestContainer.addComponent(regionSearchRequestContainerHeader);
		
		final RegionAnswer regionAnswer = regionAnswerComponentGroup.getItemDataSource().getBean();
		
		for (RegionSearchRequest regionSearchRequest : regionAnswer.getRegionSearchRequests())
		{
			addRegionSearchRequestEntry(regionSearchRequest);
		}
		
		for (ProximitySearchRequest proximitySearchRequest : regionAnswer.getProximitySearchRequests())
		{
			addProximitySearchRequestEntry(proximitySearchRequest);
		}
		
		refreshInfoLabels();
		refreshSearchRequestCountFields();
	}
	
	private void addProximitySearchRequestEntry(ProximitySearchRequest proximitySearchRequest)
	{
		final VerticalLayout proximitySearchRequestEntry = new VerticalLayout();
		proximitySearchRequestEntry.setStyleName(CssStyle.PROXIMITY_SEARCH_REQUEST_ENTRY.getStyleName());
		
		String value = proximitySearchRequest.getName();
		
		if (value.contains("="))
		{
			Label label = new Label();
			label.setValue(value.substring(0, value.indexOf("=") - 2).concat(I18N.PROXIMITY.msg()));
			proximitySearchRequestEntry.addComponent(label);
			value = value.substring(value.indexOf("=") + 2, value.length());
			
			final Label spacer = new Label();
			spacer.setIcon(FontAwesome.DOT_CIRCLE_O);
			proximitySearchRequestEntry.addComponent(spacer);
		}
		
		proximitySearchRequestEntry.addComponent(new Label(value));
		
		final Button delete = new Button(I18N.REGIONANSWER_ACTION_REMOVEPROXIMITY.msg());
		delete.setIcon(FontAwesome.TRASH_O);
		delete.setStyleName(CssStyle.DELETE_BUTTON.getStyleName());
		delete.addClickListener(event ->
		{
			regionAnswerComponentGroup.getItemDataSource().getBean().getProximitySearchRequests().remove(proximitySearchRequest);
			proximitySearchRequestContainer.removeComponent(proximitySearchRequestEntry);
			refreshInfoLabels();
			refreshSearchRequestCountFields();
		});
		
		proximitySearchRequestEntry.addComponent(delete);
		proximitySearchRequestContainer.addComponent(proximitySearchRequestEntry);
	}
	
	private void addRegionSearchRequestEntry(RegionSearchRequest regionSearchRequest)
	{
		final VerticalLayout regionSearchRequestEntry = new VerticalLayout();
		regionSearchRequestEntry.setStyleName(CssStyle.REGION_SEARCH_REQUEST_ENTRY.getStyleName());
		
		String value = regionSearchRequest.getName();
		
		while (value.contains(">"))
		{
			Label label = new Label();
			label.setValue(value.substring(0, value.indexOf(">") - 1));
			regionSearchRequestEntry.addComponent(label);
			value = value.substring(value.indexOf(">") + 2, value.length());
			
			final Label spacer = new Label();
			spacer.setIcon(FontAwesome.CHEVRON_RIGHT);
			spacer.setStyleName(CssStyle.REGION_SEARCH_REQUEST_ENTRY_SPACER.getStyleName());
			regionSearchRequestEntry.addComponent(spacer);
		}
		
		regionSearchRequestEntry.addComponent(new Label(value));
		
		final Button delete = new Button(I18N.REGIONANSWER_ACTION_REMOVEREGION.msg());
		delete.setIcon(FontAwesome.TRASH_O);
		delete.setStyleName(CssStyle.DELETE_BUTTON.getStyleName());
		delete.addClickListener(event ->
		{
			regionAnswerComponentGroup.getItemDataSource().getBean().getRegionSearchRequests().remove(regionSearchRequest);
			regionSearchRequestContainer.removeComponent(regionSearchRequestEntry);
			refreshInfoLabels();
			refreshSearchRequestCountFields();
		});
		
		regionSearchRequestEntry.addComponent(delete);
		
		regionSearchRequestContainer.addComponent(regionSearchRequestEntry);
	}
	
	private void refreshInfoLabels()
	{
		proximitySearchInfo.setValue(null);
		regionSearchInfo.setValue(null);
		
		final RegionAnswer regionAnswer = regionAnswerComponentGroup.getItemDataSource().getBean();
		
		final boolean emptyRegionSearch = regionAnswer.getRegionSearchRequests().isEmpty();
		final boolean emptyProximitySearch = regionAnswer.getProximitySearchRequests().isEmpty();
		
		if (emptyRegionSearch)
		{
			regionSearchInfo.setValue(emptyProximitySearch ? I18N.REGIONANSWER_CAPTION_WORLDWIDEINFO.msg() : I18N.REGIONANSWER_CAPTION_REGIONINFO.msg());
			regionSearchRequestContainer.setVisible(false);
		}
		else
		{
			regionSearchRequestContainer.setVisible(true);
		}
		
		if (emptyProximitySearch)
		{
			proximitySearchInfo.setValue(emptyRegionSearch ? I18N.REGIONANSWER_CAPTION_WORLDWIDEINFO.msg() : I18N.REGIONANSWER_CAPTION_PROXIMITYINFO.msg());
			proximitySearchRequestContainer.setVisible(false);
		}
		else
		{
			proximitySearchRequestContainer.setVisible(true);
		}
	}
	
	private void refreshSearchRequestCountFields()
	{
		final RegionAnswer regionAnswer = regionAnswerComponentGroup.getItemDataSource().getBean();
		
		proximitySearchRequestCountField.setConvertedValue(regionAnswer.getProximitySearchRequests().size());
		regionSearchRequestCountField.setConvertedValue(regionAnswer.getRegionSearchRequests().size());

		proximitySearchRequestCountField.setVisible(regionAnswer.getProximitySearchRequests().size() > MAX_PROXIMITIES);
		regionSearchRequestCountField.setVisible(regionAnswer.getRegionSearchRequests().size() > MAX_REGIONS);

		regionSearchRequestContainerHeader.setValue(I18N.REGIONANSWER_HEADER_CAPTION.msg().concat(": " +regionAnswer.getRegionSearchRequests().size()+" / " +MAX_REGIONS));
		proximitySearchRequestContainerHeader.setValue(I18N.PROXIMITYANSWER_HEADER_CAPTION.msg().concat(": " +regionAnswer.getProximitySearchRequests().size()+" / " +MAX_REGIONS));
	}
}
