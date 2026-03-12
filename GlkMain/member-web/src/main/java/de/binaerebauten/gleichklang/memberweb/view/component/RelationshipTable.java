package de.binaerebauten.gleichklang.memberweb.view.component;

import com.vaadin.server.*;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;
import de.binaerebauten.gleichklang.core.initializer.AppUI;
import de.binaerebauten.gleichklang.core.model.NaturalKeyEntity.NaturalKey;
import de.binaerebauten.gleichklang.core.model.audio.UserAudio;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.matching.Relationship.Affiliation;
import de.binaerebauten.gleichklang.core.model.media.Footprint;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.AudioRepository;
import de.binaerebauten.gleichklang.core.repository.LocatableRepository;
import de.binaerebauten.gleichklang.core.service.file.AvatarUploadFile;
import de.binaerebauten.gleichklang.core.utils.StringUtils;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanPagingComponent;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.view.I18N;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import javax.persistence.metamodel.SingularAttribute;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static de.binaerebauten.gleichklang.core.model.user.I18N.CANCELED_USER_WITH_ALIAS;
import static de.binaerebauten.gleichklang.core.model.user.I18N.DELETED_USER_WITH_ALIAS;

public class RelationshipTable extends CustomComponent
{

	@Autowired
	@Lazy
	AudioRepository audioRepository;
	LocatableRepository locatableRepository;

	public interface RelationshipTableHandler
	{
		void showRelationship(Relationship relationship);

		File getThumbnailAvatarImage(Relationship relationship);

		String getZipRegionsCountries(User user,List<Object>  savedRegionSearchRequest);
		Boolean checkUserAccess(User user);
	}
	private static final Logger log = LoggerFactory.getLogger(RelationshipTable.class);
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy   HH:mm");
	private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

	private final LazyBeanPagingComponent<Relationship> pagingComponent;

	private RelationshipTableHandler relationshipTableHandler;

	public boolean isCancelCheckBoxTrue() {
		return cancelCheckBoxTrue;
	}

	public void setCancelCheckBoxTrue(boolean cancelCheckBoxTrue) {
		this.cancelCheckBoxTrue = cancelCheckBoxTrue;
	}

	private boolean cancelCheckBoxTrue;

	private String showStringPopUp="";

	public RelationshipTable(Device device)
	{
		pagingComponent = createPagingComponent(device);

		locatableRepository = AppUI.getApplicationContext().getBean(LocatableRepository.class);

		setCompositionRoot(pagingComponent);
	}

	private LazyBeanPagingComponent<Relationship> createPagingComponent(Device device)
	{
		final LazyBeanPagingComponent<Relationship> pagingComponent = new LazyBeanPagingComponent<>();
		pagingComponent.initView(device);
		pagingComponent.setSizeFull();
		pagingComponent.setStyleName(CssStyle.RELATIONSHIP_TABLE.getStyleName());

		pagingComponent.addGeneratedColumn(itemId -> createRelationshipRow(device, itemId));

		return pagingComponent;
	}

	public void setMaxResults(int maxResults)
	{
		pagingComponent.setMaxResults(maxResults);
	}

	public void setSortPropertyId(boolean ascending, SingularAttribute<? super Relationship, ?> propertyId)
	{
		pagingComponent.setSortPropertyId(ascending, propertyId);
	}

	private CssLayout createRelationshipRow(Device device, Relationship relation)
	{
		final CssLayout wrapper = new CssLayout();
		wrapper.setWidth(100, Unit.PERCENTAGE);
		wrapper.setHeight(170, Unit.PIXELS);
		if(relationshipTableHandler.checkUserAccess(relation.getTargetUser()))
			wrapper.setStyleName(CssStyle.RELATIONSHIP_NOTMATCH_WRAPPER.getStyleName());
		else wrapper.setStyleName(CssStyle.RELATIONSHIP_MATCH_WRAPPER.getStyleName());
		wrapper.addLayoutClickListener(event -> relationshipTableHandler.showRelationship(relation));

		final Button buttonForScreenreader = new Button(relation.getTargetUser().getAlias());
		buttonForScreenreader.addClickListener(event -> relationshipTableHandler.showRelationship(relation));
		final CssLayout screenreaderWrapper = new CssLayout(buttonForScreenreader);
		screenreaderWrapper.addStyleName(CssStyle.HIDDEN.getStyleName());
		wrapper.addComponent(screenreaderWrapper);

		// NEW Label
		final CssLayout newLabelLayout = new CssLayout();
		newLabelLayout.addStyleName(CssStyle.RELATIONSHIP_NEW_WRAPPER.getStyleName());
		final Label newLabel = new Label();
		newLabel.setWidthUndefined();
		if (!relation.isViewed())
		{
			newLabel.addStyleName(CssStyle.RELATIONSHIP_NEW.getStyleName());
			newLabel.setValue(I18N.RELATIONSHIP_NEW.msg());
		}
		else
		{
			newLabel.addStyleName(CssStyle.RELATIONSHIP_OLD.getStyleName());
		}
		newLabelLayout.addComponent(newLabel);
		wrapper.addComponent(newLabelLayout);

		// Avatar image
		final Component avatar = createAvatarComponent(relationshipTableHandler.getThumbnailAvatarImage(relation), relation);
		wrapper.addComponent(avatar);

		// User Info
		final Component userInfo = createrUserInfoComponent(device, relation);
		wrapper.addComponent(userInfo);
		if(!isAllowMessage(relation)){
			wrapper.setStyleName(CssStyle.RELATIONSHIP_NOTMATCH_WRAPPER.getStyleName());
		}

		// Memo
		final Component memos = createMemoComponent(relation.getMemo());
		wrapper.addComponent(memos);

		if(Collections.disjoint(relation.getCategories(), relation.getTargetUser().getCategories()) && cancelCheckBoxTrue){
			wrapper.setVisible(false);
		}

//		if(relation.getTargetUser().isBlocked()){
//			wrapper.setVisible(false);
//		}

		return wrapper;
	}

	private Component createrUserInfoComponent(Device device, Relationship relation)
	{
		final CssLayout wrapper = new CssLayout();
		wrapper.addStyleName(CssStyle.RELATIONSHIP_USERINFO_WRAPPER.getStyleName());

		// Symbols
		final CssLayout symbolRow = new CssLayout();
		symbolRow.addStyleName(CssStyle.RELATIONSHIP_SYMBOLS.getStyleName());

		// Category
		relation.getCategories().forEach(r ->
		{
			final Image image = new Image();
			image.setSource(r.getIcon());
			image.setHeight("35px");
			image.setDescription(r.msg());
			symbolRow.addComponent(image);
		});
		final Component affiliation = createAffiliationIcons(relation);
		symbolRow.addComponent(affiliation);

		final Component sex = createSexIcon(relation.getTargetUserSexKey().orElse(NaturalKey.SEX_W));
		symbolRow.addComponent(sex);

		final Component targetFootprint = createTargetFootprintComponent(relation);
		if (targetFootprint != null)
		{
			symbolRow.addComponent(targetFootprint);
		}

		final Component audio = createAudioIcon(relation);
		symbolRow.addComponent(audio);

		wrapper.addComponent(symbolRow);

		// creation date
		final Label created = new Label(relation.getCreateDate().format(FORMATTER));
		created.addStyleName(CssStyle.RELATIONSHIP_DATE_LABEL.getStyleName());
		wrapper.addComponent(created);

		// alias
		final Label alias;
		if (relation.getTargetUser().isDataDeleted())
		{
			final int maxSize = device == Device.MOBILE ? 8 : -1;
			alias = new Label(DELETED_USER_WITH_ALIAS.msg(StringUtils.cutString(relation.getTargetUser().getAlias(), maxSize)));
		}
		else if (relation.getTargetUser().isCanceled())
		{
			final int maxSize = device == Device.MOBILE ? 8 : -1;
			alias = new Label(CANCELED_USER_WITH_ALIAS.msg(StringUtils.cutString(relation.getTargetUser().getAlias(), maxSize)));
		}
		else
		{
			final int maxSize = device == Device.MOBILE ? 18 : -1;
			alias = new Label(I18N.RELATIONSHIP_VIEW_AGE.msg(StringUtils.cutString(relation.getTargetUser().getAlias(), maxSize), relation.getTargetUser().getAge()));
		}
		alias.addStyleName(CssStyle.RELATIONSHIP_USER_LABEL.getStyleName());
		alias.setContentMode(ContentMode.HTML);
		wrapper.addComponent(alias);

		String[] locationWithRegionSearchArray=relationshipTableHandler.getZipRegionsCountries(relation.getTargetUser(),locatableRepository.getSavedRegionSearchRequestBySourceUser(relation.getSourceUser())).split(";");

		String locationWithRegionSearch=relationshipTableHandler.getZipRegionsCountries(relation.getTargetUser(),locatableRepository.getSavedRegionSearchRequestBySourceUser(relation.getSourceUser()));

		final Label locationRS = new Label(locationWithRegionSearchArray.length>0?locationWithRegionSearchArray[0]:locationWithRegionSearch);
		locationRS.addStyleName(CssStyle.RELATIONSHIP_LOCATION_LABEL.getStyleName());
		locationRS.setVisible(!locationRS.getValue().isEmpty());

		//final Label locationFull = new Label(locationWithRegionSearch);
		//locationFull.addStyleName(CssStyle.RELATIONSHIP_LOCATION_LABEL.getStyleName());
		//locationFull.setVisible(false);

		if (device == Device.MOBILE && !locationRS.getValue().isEmpty())
		{
			locationRS.setValue(StringUtils.cutString(locationRS.getValue(), 45));
			//locationFull.setValue(StringUtils.cutString(locationFull.getValue(), 45));

		}

		final Button additionalLocations = new Button();
		additionalLocations.setCaption(I18N.SHOW_ADDITIONAL_LOCATIONS.msg()+System.lineSeparator());
		additionalLocations.setIcon(FontAwesome.CHEVRON_RIGHT);
		additionalLocations.addStyleName(BaseTheme.BUTTON_LINK);
		additionalLocations.addStyleName(CssStyle.SHOW_ADDITIONAL_LOCATION_BUTTON.getStyleName());
		additionalLocations.addClickListener((event) -> {

			if(locationWithRegionSearchArray[locationWithRegionSearchArray.length-1].equals("savedRegionSearch"))
			{
				MessageBox.show((locationWithRegionSearch.substring(locationWithRegionSearch.indexOf(";") + 1, locationWithRegionSearch.indexOf("savedRegionSearch")-1).replaceAll("; ",System.lineSeparator()).trim()));
			}
			else
			{
				MessageBox.show(locationWithRegionSearch.replaceAll("; ",System.lineSeparator()).trim());
			}

		});

		wrapper.addComponent(locationRS);
		boolean additionalLocation=false;

		if(locationWithRegionSearchArray[locationWithRegionSearchArray.length-1].equals("savedRegionSearch"))
		{
			showStringPopUp=" "+(locationWithRegionSearch.substring(0, locationWithRegionSearch.indexOf("savedRegionSearch")-1));
		}
		else
		{
			showStringPopUp=" "+locationWithRegionSearch;
		}

		String[] parts = showStringPopUp.split(";");
		Set<String> set = new HashSet<>();
		for (String part : parts) {
			set.add(part);
		}
		if(showStringPopUp.split(";").length>1 && set.size()>1)
		{
			additionalLocation=true;
		}

		if(additionalLocation)
		{
			wrapper.addComponent(additionalLocations);
		}

		if (relation.isTargetRelationshipViewed() != null && relation.isTargetRelationshipViewed())
		{
			final Label relationShipViewed = new Label(I18N.RELATIONSHIP_VIEW_VIEWED.msg());
			relationShipViewed.addStyleName(CssStyle.RELATIONSHIP_VIEWED_LABEL.getStyleName());
			wrapper.addComponent(relationShipViewed);
		}
		if(!isAllowMessage(relation))
		{
			final Label userNotAvailable = new Label(I18N.RELATIONSHIP_USER_NOT_AVAILABLE.msg());
			userNotAvailable.addStyleName(CssStyle.RELATIONSHIP_USER_UNAVAILABLE.getStyleName());
			wrapper.addComponent(userNotAvailable);
			//wrapper.setStyleName(CssStyle.RELATIONSHIP_NOTMATCH_WRAPPER.getStyleName());
		}

		return wrapper;
	}

	private Component createMemoComponent(String memo)
	{
		final CssLayout layout = new CssLayout();
		layout.addStyleName(CssStyle.RELATIONSHIP_MEMO_WRAPPER.getStyleName());

		if (memo == null || memo.isEmpty())
		{
			return layout;
		}

		final Label memoText = new Label();
		memoText.setValue(memo);
		memoText.addStyleName(CssStyle.RELATIONSHIP_MEMO_TEXT.getStyleName());
		layout.addComponent(memoText);

		return layout;
	}

	private Component createAudioIcon(Relationship relationship){
		final Image image = new Image();
		audioRepository = AppUI.getApplicationContext().getBean(AudioRepository.class);

		List<UserAudio> userAudio =  audioRepository.findByAuthorId(relationship.getTargetUser().getId());
		Set<RecommendationCategory> categories =  relationship.getCategories();
		if(userAudio!=null && userAudio.size()>0) {
			if (userAudio.size() >1) {
				setAudioIcon(image);
				return image;
			}

			if(userAudio.size()==1) {

				if (userAudio.get(0).getFriendship() && userAudio.get(0).getPartnership()) {
					setAudioIcon(image);
					return image;
				}
				if (categories.size() >= 1 && categories.contains(RecommendationCategory.PARTNERSHIP)) {
					UserAudio audioModel = audioRepository.findByAuthorIdAndForPartnerShip(relationship.getTargetUser().getId());
					if (audioModel != null) {
						setAudioIcon(image);
						return image;
					}
				}
				if (categories.size() >= 1 && categories.contains(RecommendationCategory.FRIENDSHIP)) {
					UserAudio audioModel = audioRepository.findByAuthorIdAndForFriendship(relationship.getTargetUser().getId());
					if (audioModel != null) {
						setAudioIcon(image);
						return image;
					}
				}
			}
		}

		return  image;
	}

	private void setAudioIcon(Image image){
		Resource resource = new ThemeResource("img/audios_icon.png");
		image.setDescription(I18N.AUDIO_DESCRIPTION.msg());
		image.setSource(resource);
		image.setHeight("36px");
	}

	private Component createSexIcon(NaturalKey sexKey)
	{
		final Image image = new Image();

		final Resource source;

		switch (sexKey)
		{
			case SEX_W:
				source = new ThemeResource("img/female.svg");
				image.setDescription(I18N.RELATIONSHIP_VIEW_FEMALE.msg());
				break;
			case SEX_M:
				source = new ThemeResource("img/male.svg");
				image.setDescription(I18N.RELATIONSHIP_VIEW_MALE.msg());
				break;
			case SEX_I:
				source = new ThemeResource("img/intersexual.svg");
				image.setDescription(I18N.RELATIONSHIP_VIEW_INTERSEXUAL.msg());
				break;
			case SEX_WI:
				source = new ThemeResource("img/intersexual.svg");
				image.setDescription(I18N.RELATIONSHIP_VIEW_INTERSEXUAL_FEMALE.msg());
				break;
			case SEX_MI:
				source = new ThemeResource("img/intersexual.svg");
				image.setDescription(I18N.RELATIONSHIP_VIEW_INTERSEXUAL_MALE.msg());
				break;
			default:
				source = new ThemeResource("img/female.svg");
				image.setDescription(I18N.RELATIONSHIP_VIEW_FEMALE.msg());
				break;
		}

		image.setSource(source);
		image.setHeight("35px");

		return image;
	}

	private Image createAffiliationIcons(Relationship relation)
	{

		final Image image = new Image();
		image.setSource(relation.getAffiliation().getIcon());
		image.setHeight("35px");
		if (relation.getAffiliation() == Affiliation.POSITIVE)
			image.setDescription(I18N.RELATIONSHIP_VIEW_AFFILLIATION_POSITIVE.msg());
		else if (relation.getAffiliation() == Affiliation.NEGATIVE)
			image.setDescription(I18N.RELATIONSHIP_VIEW_AFFILLIATION_NEGATIVE.msg());
		else
			image.setDescription(I18N.RELATIONSHIP_VIEW_AFFILLIATION_NEUTRAL.msg());

		return image;
	}

	private Embedded createTargetFootprintComponent(Relationship relationship)
	{
		if (relationship.getTargetUserFootprint() == null) return null;

		return createFootprintComponent(relationship.getTargetUserFootprint(), relationship.getTargetUserFootprintDate());
	}

	private Component createAvatarComponent(File file, Relationship relationship)
	{
		final CssLayout wrapper = new CssLayout();
		wrapper.addStyleName(CssStyle.RELATIONSHIP_AVATAR_WRAPPER.getStyleName());

		final Image image = new Image();
		image.setStyleName(CssStyle.RELATIONSHIP_AVATAR.getStyleName());

		if (relationship.getTargetUser().isCanceled() || relationship.getTargetUser().isDataDeleted() || relationship.getTargetUser().isAdminCanceled() || relationship.getTargetUser().getOrderedCategories().size()==0)
			image.setSource(new ThemeResource(AvatarUploadFile.canceledDummyAvatar));
		else
		{
			if (file == null)
				image.setSource(new ThemeResource(AvatarUploadFile.dummyAvatar));
			else
				image.setSource(new FileResource(file));
		}

		wrapper.addComponent(image);
		return wrapper;
	}

	private Embedded createFootprintComponent(Footprint footprint, LocalDateTime footprintDate)
	{

		final Embedded embedded = new Embedded();
		final String dateString = footprintDate == null ? I18N.RELATIONSHIP_VIEW_FOOTPRINT_NODATE.msg() : footprintDate.format(DAY_FORMATTER);
		embedded.setDescription(I18N.RELATIONSHIP_VIEW_FOOTPRINT_RECEIVED.msg(dateString, footprint.getName()));
		embedded.setWidth(35, Unit.PIXELS);

		embedded.setSource(new ThemeResource(footprint.getPath()));
		return embedded;
	}

	public void setRelationshipHandler(LazyBeanFilteredItemsHandler<Relationship> handler, RelationshipTableHandler relationshipTableHandler)
	{
		if (handler != null) Objects.requireNonNull(relationshipTableHandler);

		this.relationshipTableHandler = relationshipTableHandler;
		pagingComponent.setHandler(handler);
	}

	public void onDeviceChanged(Device device)
	{
		Objects.requireNonNull(device);

		pagingComponent.initView(device);
	}

	public void refreshItem(Relationship relationship)
	{
		pagingComponent.refreshItem(relationship);
	}

	public LazyBeanPagingComponent<Relationship> getPagingComponent()
	{
		return pagingComponent;
	}

	private boolean isAllowMessage(Relationship relationship)
	{
		return !relationship.getTargetUser().isDataDeleted() && !relationship.getTargetUser().isCanceled()&& relationship.getTargetUser().getOrderedCategories().size()!=0 &&!relationship.getTargetUser().isAdminCanceled() && !Collections.disjoint(relationship.getCategories(), relationship.getTargetUser().getCategories());
	}
}