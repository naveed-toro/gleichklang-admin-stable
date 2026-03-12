package de.binaerebauten.gleichklang.memberweb.view.popup;

import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;
import de.binaerebauten.gleichklang.core.initializer.AppUI;
import de.binaerebauten.gleichklang.core.model.audio.UserAudio;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.matching.Relationship.Affiliation;
import de.binaerebauten.gleichklang.core.model.matching.Relationship_;
import de.binaerebauten.gleichklang.core.model.media.Footprint;
import de.binaerebauten.gleichklang.core.model.questionnaire.Answer;
import de.binaerebauten.gleichklang.core.model.user.Address;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.User_;
import de.binaerebauten.gleichklang.core.repository.AudioRepository;
import de.binaerebauten.gleichklang.core.service.file.AvatarUploadFile;
import de.binaerebauten.gleichklang.core.utils.ComboBoxUtils;
import de.binaerebauten.gleichklang.core.utils.StringUtils;
import de.binaerebauten.gleichklang.core.utils.UserProfileUtil.UserInfo;
import de.binaerebauten.gleichklang.core.view.component.ButtonDropdownComponent;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.StickyFooterPopup;
import de.binaerebauten.gleichklang.core.view.component.UserProfile.UserProfileData;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.view.AudioUploader;
import de.binaerebauten.gleichklang.memberweb.view.component.FootprintComponent;
import de.binaerebauten.gleichklang.core.view.component.FreeTextComponent;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;

import static de.binaerebauten.gleichklang.core.model.user.I18N.CANCELED_USER_WITH_ALIAS;
import static de.binaerebauten.gleichklang.core.model.user.I18N.DELETED_USER_WITH_ALIAS;

public class RelationshipPopup extends StickyFooterPopup
{

	@Value("${local.file.path}")
	private String filePath;
	public interface RelationshipPopupListener
	{
		void showNewMessage(RelationshipPopup sender, Relationship relationship);

		void showListMessages(RelationshipPopup sender, Relationship relationship, Device device);

		void showReportAbuse(RelationshipPopup sender, Relationship relationship, float popupWidthAsFloat, float popupHeightAsFloat);

		void openUserInfo(UserInfo userInfo, List<Answer> answers);

		void showMediaGalleries(RelationshipPopup sender, Relationship relationship, RelationshipData relationshipData, float popupWidthAsFloat, float popupHeightAsFloat);

		void setupPrintContent(Button button, Relationship relationship, RelationshipData data);

		void cancelRelationship(RelationshipPopup sender, Relationship relationship);
	}

	public static class RelationshipData
	{
		private final User targetUser;
		private final Footprint footprint;
		private final Affiliation rating;
		private final boolean allowMessage;
		private final boolean existsMessages;
		private final boolean existsGalleries;
		private final SortedMap<RecommendationCategory, UserProfileData> userProfileDataMap;
		private final LocalDateTime footprintDate;
		private final LinkedHashMap<UserInfo, List<Answer>> userAnswers;

		public RelationshipData(User targetUser, Affiliation rating, Footprint footprint, LocalDateTime footprintDate, boolean allowMessage, boolean existsMessages, boolean existsGalleries, SortedMap<RecommendationCategory, UserProfileData> userProfileDataMap, LinkedHashMap<UserInfo, List<Answer>> userAnswers)
		{
			this.userProfileDataMap = Objects.requireNonNull(userProfileDataMap);
			this.targetUser = Objects.requireNonNull(targetUser);
			this.userAnswers = Objects.requireNonNull(userAnswers);

			this.rating = rating;
			this.footprint = footprint;
			this.allowMessage = allowMessage;
			this.existsMessages = existsMessages;
			this.existsGalleries = existsGalleries;
			this.footprintDate = footprintDate;
		}

		public Affiliation getRating()
		{
			return rating;
		}

		public Footprint getFootprint()
		{
			return footprint;
		}

		public User getTargetUser()
		{
			return targetUser;
		}

		public boolean isAllowMessage()
		{
			return allowMessage;
		}

		public boolean isExistsMessages()
		{
			return existsMessages;
		}

		public boolean isExistsGalleries()
		{
			return existsGalleries;
		}

		public Map<RecommendationCategory, UserProfileData> getUserProfileDataMap()
		{
			return userProfileDataMap;
		}

		public LocalDateTime getFootprintDate()
		{
			return footprintDate;
		}

		public LinkedHashMap<UserInfo, List<Answer>> getUserAnswers()
		{
			return userAnswers;
		}
	}

	private final ComponentGroup<Relationship> relationshipComponentGroup;
	private final RelationshipData relationshipData;
	private final Relationship relationship;
	private final RelationshipPopupListener listener;
	private final VerticalLayout mainComponent;
	private final TextArea memoTextArea;
	private final FootprintComponent footprintComponent;
	private final RecommendationCategory initialSelectedCategory;
	private final User targetUser;
	private Device device;
	private AudioRepository audioRepository;
	public RelationshipPopup(Relationship relationship, RelationshipData relationshipData, RecommendationCategory selectedCategory, RelationshipPopupListener listener, Device device)
	{
		Objects.requireNonNull(listener);
		Objects.requireNonNull(relationship);
		Objects.requireNonNull(relationshipData);

		this.device = device;
		this.targetUser = relationshipData.getTargetUser();

		this.relationshipComponentGroup = new ComponentGroup<>(Relationship.class, relationship);
		this.relationship = relationship;
		this.relationshipData = relationshipData;
		this.initialSelectedCategory = selectedCategory;
		this.listener = listener;
		this.footprintComponent = new FootprintComponent(relationship, relationshipData, device);
		this.memoTextArea = createMemoTextArea();

		addCloseListener(e -> footprintComponent.close());

		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(false);
		layout.setMargin(false);

		setPopupContent(layout);

		this.addStyleName(CssStyle.RELATIONSHIP_VIEW_RELATIONSHIP_POPUP.getStyleName());
		mainComponent = layout;

		createResponsiveLayout();
	}

	private void setPopupHeader()
	{
		final String alias = targetUser.getAlias();
		final String additionalInfos = createAdditionalInfos(targetUser);
		final String canceledCaption = CANCELED_USER_WITH_ALIAS.msg(alias);
		final String deletedCaption = DELETED_USER_WITH_ALIAS.msg(alias);
		final String name = !targetUser.isDataDeleted() ? !targetUser.isCanceled() ? alias : canceledCaption : deletedCaption;

		setCaption(name + additionalInfos);
		setCaptionAsHtml(true);

		setIcon(new ThemeResource("img/icon_profile_white.svg"));
	}

	private String createAdditionalInfos(User targetUser)
	{
		String additionalInfos = ", " + I18N.RELATIONSHIPPOPUP_YEARS.msg(targetUser.getAge());

		if (relationship.getTargetUserSex() != null)
			additionalInfos = additionalInfos.concat(", " + relationship.getTargetUserSex());
		if (targetUser.getAddresses() != null)
			additionalInfos = additionalInfos.concat(getAddress(targetUser));

		return additionalInfos;
	}

	private Button createRemoveRelationshipButton()
	{
		final Button button = new Button(I18N.RELATIONSHIPPOPUP_CAPTION_DELETE.msg());
		button.setStyleName(CssStyle.NEW_MESSAGE_BUTTON.getStyleName());

		button.setIcon(FontAwesome.TRASH_O);
		if (device == Device.DESKTOP)
		{
			button.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		}
		button.addClickListener(event -> listener.cancelRelationship(this, relationship));

		return button;
	}

	private Component createMobileRelationshipProfileComponent()
	{
		final VerticalLayout layout = new VerticalLayout();

		final Component header = createMobileHeader();
		final Component userRating = createUserRatingDropdown();
		userRating.setStyleName(CssStyle.MOBILE_USER_RATING_WRAPPER.getStyleName());

		final VerticalLayout memoTextAreaWrapper = new VerticalLayout();
		memoTextAreaWrapper.setSizeUndefined();
		memoTextAreaWrapper.addComponent(memoTextArea);
		memoTextAreaWrapper.setStyleName(CssStyle.RELATIONSHIPPOPUP_MEMO_WRAPPER.getStyleName());

		final VerticalLayout memoWrapper = new VerticalLayout();
		memoWrapper.setWidth(100, Unit.PERCENTAGE);
		memoWrapper.addComponent(memoTextAreaWrapper);
		memoWrapper.setComponentAlignment(memoTextAreaWrapper, Alignment.TOP_LEFT);
		memoWrapper.addStyleName(CssStyle.RELATIONSHIPPOPUP_MEMO_LAYOUT.getStyleName());

		final Component profile = createUserProfileComponent();

		if (targetUser.isCanceled())
			memoWrapper.addStyleName("not-available");

		layout.addComponents(header);

		if (!targetUser.isCanceled())
			layout.addComponent(footprintComponent);

		if(!targetUser.isCanceled() && !targetUser.isDataDeleted() && !targetUser.isAdminCanceled() && !targetUser.isBlocked() && targetUser.getOrderedCategories().size()!=0) {
			layout.addComponents(profile, memoWrapper, userRating);
		}
		else{
			layout.addComponents(profile, memoWrapper);

		}

		final HorizontalLayout footerWrapper = new HorizontalLayout();
		footerWrapper.setWidth(100, Unit.PERCENTAGE);

		final Component footer = createFooterButtons();
		footerWrapper.addComponent(footer);
		footerWrapper.setComponentAlignment(footer, Alignment.MIDDLE_RIGHT);

		this.setFooter(footerWrapper);

		setPopupHeader();

		return layout;
	}

	private Component createMobileHeader()
	{
		final VerticalLayout headerWrapper = new VerticalLayout();
		headerWrapper.setSizeFull();

		final HorizontalLayout middleHeader = new HorizontalLayout();
		middleHeader.setStyleName(CssStyle.MOBILE_MIDDLE_HEADER.getStyleName());

		final Component userInfoButtons = createUserInfoButtons();

		if (!targetUser.isCanceled() && !targetUser.isDataDeleted() && !targetUser.isAdminCanceled() && targetUser.getOrderedCategories().size()!=0)
			middleHeader.addComponent(userInfoButtons);

		final VerticalLayout bottomHeader = new VerticalLayout();
		bottomHeader.setSizeFull();
		bottomHeader.setStyleName(CssStyle.MOBILE_BOTTOM_HEADER.getStyleName());

		final Label statusLabel = new Label(StringUtils.deSanitizeSpecialCharacters(relationshipData.getTargetUser().getStatusMessage()));
		statusLabel.setCaption(I18N.RELATIONSHIPPOPUP_CAPTION_STATUS.msg());

		bottomHeader.addComponents(statusLabel);
		if (statusLabel.getValue() == null || statusLabel.getValue().isEmpty())
			bottomHeader.setVisible(false);

		headerWrapper.addComponents(middleHeader, bottomHeader);
		headerWrapper.setExpandRatio(middleHeader, 0.5f);
		headerWrapper.setExpandRatio(bottomHeader, 0.5f);

		return headerWrapper;
	}

	private Component createDesktopRelationshipProfileComponent()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setSizeFull();
		layout.setHeight("100%");

		final VerticalLayout headerWrapper = new VerticalLayout();
		headerWrapper.setWidth(100, Unit.PERCENTAGE);
		headerWrapper.setStyleName(CssStyle.RELATIONSHIPPOPUP_HEADER.getStyleName());

		final VerticalLayout popupHeader = new VerticalLayout();
		popupHeader.setWidth(100, Unit.PERCENTAGE);
		popupHeader.addStyleName(CssStyle.RELATIONSHIPPOPUP_HEADER.getStyleName());

		final Component userInfoButtons = createUserInfoButtons();

		if (!targetUser.isCanceled() && !targetUser.isDataDeleted() && !targetUser.isAdminCanceled() && targetUser.getOrderedCategories().size()!=0)
			popupHeader.addComponent(userInfoButtons);

		setHeader(popupHeader);
		setPopupHeader();

		final VerticalLayout statusLayout = new VerticalLayout();
		statusLayout.setSizeFull();
		statusLayout.setStyleName(CssStyle.RELATIONSHIPPOPUP_STATUS_WRAPPER.getStyleName());

		final Label statusLabel = new Label(relationshipData.getTargetUser().getStatusMessage());
		statusLabel.setCaption(I18N.RELATIONSHIPPOPUP_CAPTION_STATUS.msg());
		statusLayout.addComponent(statusLabel);
		statusLayout.setVisible(!Strings.isNullOrEmpty(statusLabel.getValue()));

		final VerticalLayout memoTextAreaWrapper = new VerticalLayout();
		memoTextAreaWrapper.setSizeUndefined();
		memoTextAreaWrapper.addComponent(memoTextArea);
		memoTextAreaWrapper.setStyleName(CssStyle.RELATIONSHIPPOPUP_MEMO_WRAPPER.getStyleName());

		final VerticalLayout memoWrapper = new VerticalLayout();
		memoWrapper.setWidth(100, Unit.PERCENTAGE);
		memoWrapper.addComponent(memoTextAreaWrapper);
		memoWrapper.setComponentAlignment(memoTextAreaWrapper, Alignment.TOP_LEFT);
		memoWrapper.addStyleName(CssStyle.RELATIONSHIPPOPUP_MEMO_LAYOUT.getStyleName());

		if (targetUser.isCanceled())
			memoWrapper.addStyleName("not-available");

		final Component userProfile = createUserProfileComponent();

		// add components together
		layout.addComponents(statusLayout);

		if (!targetUser.isCanceled() && !targetUser.isDataDeleted() && !targetUser.isAdminCanceled() && targetUser.getOrderedCategories().size()!=0)
			layout.addComponent(footprintComponent);

		if (!targetUser.isCanceled() && !targetUser.isDataDeleted()){
			layout.addComponents(statusLayout, userProfile,  memoWrapper);
		}
		else{
			layout.addComponents(statusLayout, userProfile,  memoWrapper);
		}

		// == set footer components == //
		final HorizontalLayout footerWrapper = new HorizontalLayout();
		footerWrapper.setWidth(100, Unit.PERCENTAGE);

		// left side
		final HorizontalLayout leftSideComponents = new HorizontalLayout();
		leftSideComponents.setHeight(100, Unit.PERCENTAGE);

		// user rating combo box
		if(!targetUser.isCanceled() && !targetUser.isDataDeleted() && !targetUser.isAdminCanceled() && !targetUser.isBlocked() && targetUser.getOrderedCategories().size()!=0) {
			final Component userRating = createUserRatingDropdown();
			leftSideComponents.addComponent(userRating);
			leftSideComponents.setComponentAlignment(userRating, Alignment.MIDDLE_LEFT);
		}

		// right side
		final HorizontalLayout rightSideComponents = new HorizontalLayout();
		rightSideComponents.setHeight(100, Unit.PERCENTAGE);

		// actions buttons
		rightSideComponents.addComponent(createFooterButtons());

		footerWrapper.addComponents(leftSideComponents, rightSideComponents);
		footerWrapper.setExpandRatio(leftSideComponents, 0.4f);
		footerWrapper.setExpandRatio(rightSideComponents, 0.6f);
		footerWrapper.setComponentAlignment(leftSideComponents, Alignment.MIDDLE_LEFT);
		footerWrapper.setComponentAlignment(rightSideComponents, Alignment.MIDDLE_RIGHT);

		this.setFooter(footerWrapper);

		return layout;
	}

	private VerticalLayout createAudioComponent(UserProfileData audioModel){
		audioRepository = AppUI.getApplicationContext().getBean(AudioRepository.class);
		VerticalLayout audioLayout = new VerticalLayout();
		UserAudio userAudio=null;
		if(audioModel.getCategory()==RecommendationCategory.FRIENDSHIP){
			userAudio =	audioRepository.findByAuthorIdAndForFriendship(audioModel.getUser().getId());
		}
		else if(audioModel.getCategory()==RecommendationCategory.PARTNERSHIP){
			userAudio = audioRepository.findByAuthorIdAndForPartnerShip(audioModel.getUser().getId());
		}
		if (userAudio != null) {
			String aud = userAudio.getPath();
			audioLayout.setWidth(100, Unit.PERCENTAGE);
			if (device == Device.DESKTOP) {
				File mediaFile = null;
				try {
					mediaFile = new File(aud);
				} catch (Exception ex) {
				}
				Audio audio = new Audio(I18N.AUDIO_MESSAGE.msg());
				audio.setVisible(true);
				audio.setStyleName(CssStyle.AUDIO_STYLE.getStyleName());
				audio.setSource(new FileResource(mediaFile));
				audioLayout.addComponent(audio);
			} else {
				try {
					Label audioLabel = new Label(I18N.AUDIO_MESSAGE.msg());
					byte[] audioBytes = Files.readAllBytes(new File(aud).toPath());
					audioLabel.setContentMode(ContentMode.HTML);
					audioLabel.setValue("<audio controls src=\"data:audio/mp3;base64," + new String(Base64.getEncoder().encode(audioBytes)) + "\" type=\"audio/mpeg\"></audio>");
					audioLayout.addComponent(audioLabel);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
		return audioLayout;
	}

	private String getAddress(User user)
	{
		String result = "";

		final Set<Address> addresses = new LinkedHashSet<>(user.getAddresses());
		for (Address a : addresses)
		{
			if (a.isPayment())
			{
				if (a.getCountry() != null && a.getCountry().getCountryCode() != null)
				{
					result = result.concat(", " + a.getCountry().getCountryCode());
					if (a.getZip() != null && a.getZip().getZip() != null)
						result = result.concat(", PLZ " + a.getZip().getZip().substring(0, 2) + "...");
				}
			}
		}

		return result.isEmpty() ? "" : result;
	}

	private Component createUserProfileComponent()
	{
		if (relationshipData.getUserProfileDataMap().size() == 0)
			return new Label();
		if (relationshipData.getUserProfileDataMap().size() == 1)
		{
			if (device == Device.DESKTOP)
				return createDesktopUserProfileComponent(relationshipData.getUserProfileDataMap().values().iterator().next());
			else
				return createMobileUserProfileComponent(relationshipData.getUserProfileDataMap().values().iterator().next());
		}

		final VerticalLayout wrapper = new VerticalLayout();

		final HorizontalLayout dropdownWrapper = new HorizontalLayout();
		dropdownWrapper.addStyleName(CssStyle.RELATIONSHIP_POPUP_CATEGORY_FILTER_CONTROL.getStyleName());

		final ComboBox categoryDropdown = new ComboBox();
		categoryDropdown.setNullSelectionAllowed(false);
		categoryDropdown.setTextInputAllowed(false);
		categoryDropdown.setSizeUndefined();

		final TabSheet tabSheet = new TabSheet();
		tabSheet.setStyleName(CssStyle.USER_PROFILE.getStyleName());

		final Map<RecommendationCategory, Integer> categories = new HashMap<>();
		int position = 0;
		for (Map.Entry<RecommendationCategory, UserProfileData> entry : relationshipData.getUserProfileDataMap().entrySet())
		{
			if (device == Device.DESKTOP)
				tabSheet.addTab(createDesktopUserProfileComponent(entry.getValue()), entry.getKey().getName());
			else
				tabSheet.addTab(createMobileUserProfileComponent(entry.getValue()), entry.getKey().getName());

			categoryDropdown.addItem(entry.getKey());
			categoryDropdown.setItemCaption(entry.getKey(), entry.getKey().getName());
			categoryDropdown.setItemIcon(entry.getKey(), entry.getKey().getIcon());
			categories.put(entry.getKey(), position);
			position++;
		}

		dropdownWrapper.addComponent(categoryDropdown);

		if (relationship.getSourceUser().getOrderedCategories().size() == 1)
			dropdownWrapper.setVisible(false);
		else
		{
			categoryDropdown.select(0);
			categoryDropdown.setValue(relationshipData.getUserProfileDataMap().entrySet().iterator().next().getKey());
			categoryDropdown.addValueChangeListener((Property.ValueChangeListener) event -> tabSheet.setSelectedTab(categories.get(categoryDropdown.getValue())));

			if (this.initialSelectedCategory != null)
			{
				categoryDropdown.setValue(this.initialSelectedCategory);
			}
		}

		tabSheet.setTabsVisible(false);
		wrapper.addComponents(dropdownWrapper, tabSheet);

		return wrapper;
	}

	private Component createMobileUserProfileComponent(UserProfileData userProfileData)
	{
		final VerticalLayout wrapper = new VerticalLayout();
		wrapper.setStyleName(CssStyle.USER_PROFILE.getStyleName());

		final Component avatar = createAvatarComponent(userProfileData.getAvatarFile());
		avatar.setStyleName(CssStyle.AVATAR_WRAPPER.getStyleName());
		wrapper.addComponents(avatar);

		if(!userProfileData.getUser().isAdminCanceled() &&
		!userProfileData.getUser().isCanceled()) {
			wrapper.addComponent(createAudioComponent(userProfileData));
		}

		if (!targetUser.isCanceled())
		{
			wrapper.addComponent(createFreeTextComponent(userProfileData));
		}
		else
			wrapper.addStyleName("not-available");

		return wrapper;
	}

	private Component createDesktopUserProfileComponent(UserProfileData userProfileData)
	{
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);
		layout.setStyleName(CssStyle.USER_PROFILE.getStyleName());

		final VerticalLayout avatarLayout = new VerticalLayout();
		avatarLayout.setSizeFull();
		avatarLayout.addComponent(createAvatarComponent(userProfileData.getAvatarFile()));
		avatarLayout.addStyleName(CssStyle.USER_PROFILE_IMAGE_WRAPPER.getStyleName());
		if(!userProfileData.getUser().isAdminCanceled() &&
				!userProfileData.getUser().isCanceled()) {
			avatarLayout.addComponent(createAudioComponent(userProfileData));
		}
		layout.addComponents(avatarLayout);

		if (!targetUser.isCanceled() && !targetUser.isDataDeleted() && !targetUser.isAdminCanceled() && targetUser.getOrderedCategories().size()!=0)
		{
			final Component freeText = createFreeTextComponent(userProfileData);
			freeText.addStyleName(CssStyle.USER_PROFILE_FREETEXT_WRAPPER.getStyleName());
			layout.addComponent(freeText);
			layout.setExpandRatio(avatarLayout, 0.3f);
			layout.setExpandRatio(freeText, 0.7f);
		}
		else
		{
			layout.addStyleName("not-available");
		}
		return layout;
	}

	private TextArea createMemoTextArea()
	{
		final TextArea memoTextArea = relationshipComponentGroup.buildAndBind(I18N.RELATIONSHIPPOPUP_MEMOTEXT.msg(), TextArea.class, Relationship_.memo);
		memoTextArea.setStyleName(CssStyle.MEMO_TEXTFIELD.getStyleName());
		memoTextArea.setIcon(new ThemeResource("img/thumbtack_pushpin_2.svg"));
		memoTextArea.addValueChangeListener(event ->
		{
			try
			{
				relationshipComponentGroup.commit();
			}
			catch (CommitException ignored)
			{
			}
		});
		memoTextArea.setWidth(100, Unit.PERCENTAGE);

		return memoTextArea;
	}

	private Component createFreeTextComponent(UserProfileData userProfileData)
	{
		final FreeTextComponent freetext = new FreeTextComponent(userProfileData);
		return freetext;
	}

	private Component createUserInfoButtons()
	{
		final ButtonDropdownComponent buttonDropdownComponent = new ButtonDropdownComponent(device);
		buttonDropdownComponent.setStyleName(CssStyle.USER_INFO_BUTTONS, CssStyle.TABSHEET_DROPDOWN_WHITE);
		buttonDropdownComponent.addRefreshListener(this::createResponsiveLayout);
		buttonDropdownComponent.setHeightUndefined();

		for (UserInfo userInfo : relationshipData.getUserAnswers().keySet())
		{
			final List<Answer> answers = relationshipData.getUserAnswers().get(userInfo);
			final ClickListener clickListener = event -> listener.openUserInfo(userInfo, answers);
			buttonDropdownComponent.addItem(userInfo.toString(), clickListener);

			// [GF-159] existing gallery has to follow directly after LOOK
			if (userInfo.equals(UserInfo.LOOK) && relationshipData.isExistsGalleries()) {
				final ClickListener galleryClickListener = event -> listener.showMediaGalleries(this, relationship, relationshipData, getWidth(), getHeight());
				buttonDropdownComponent.addItem(I18N.RELATIONSHIPPOPUP_ACTION_MEDIA.msg(), galleryClickListener);


			}
		}



		return buttonDropdownComponent;
	}

	private HorizontalLayout createUserRatingDropdown()
	{
		final HorizontalLayout wrapper = new HorizontalLayout();
		wrapper.setHeight(100, Unit.PERCENTAGE);

		final Label ratingLabel = new Label();
		ratingLabel.setValue(I18N.RELATIONSHIPPOPUP_LABEL.msg());
		wrapper.addComponent(ratingLabel);
		wrapper.setComponentAlignment(ratingLabel, Alignment.MIDDLE_LEFT);

		final ComboBox userRating = ComponentFactory.getInstance().createField(ComboBox.class);
		userRating.setTextInputAllowed(false);
		userRating.setNullSelectionAllowed(false);

		ComboBoxUtils.addEntry(userRating, Affiliation.NEUTRAL, Affiliation.NEUTRAL.getIcon());
		ComboBoxUtils.addEntry(userRating, Affiliation.POSITIVE, Affiliation.POSITIVE.getIcon());
		ComboBoxUtils.addEntry(userRating, Affiliation.NEGATIVE, Affiliation.NEGATIVE.getIcon());

		final Affiliation affiliation = this.relationship.getAffiliation();
		switch (affiliation)
		{
			case NEUTRAL:
				userRating.select(affiliation);
				break;

			case POSITIVE:
				userRating.select(affiliation);
				break;

			case NEGATIVE:
				userRating.select(affiliation);
				break;
		}

		userRating.addValueChangeListener(event ->
		{
			relationship.setAffiliation((Affiliation) userRating.getValue());

			if (userRating.getValue() == Affiliation.NEGATIVE)
				close();
		});

		wrapper.addComponents(userRating);
		wrapper.setComponentAlignment(userRating, Alignment.MIDDLE_LEFT);

		return wrapper;
	}

	private Button createNewMessageButton()
	{
		final Button newMessageButton = new Button();
		newMessageButton.setIcon(FontAwesome.ENVELOPE);
		newMessageButton.setCaption(I18N.RELATIONSHIPPOPUP_NEW_MESSAGE.msg());
		newMessageButton.setDescription(I18N.RELATIONSHIPPOPUP_NEW_MESSAGE.msg());
		newMessageButton.setStyleName(CssStyle.NEW_MESSAGE_BUTTON.getStyleName());
		newMessageButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);

		newMessageButton.addClickListener(event -> listener.showNewMessage(this, relationship));

		if (!relationshipData.isAllowMessage()) {
			newMessageButton.setEnabled(false);
			newMessageButton.setDescription(I18N.RELATIONSHIPPOPUP_NEW_MESSAGE_NOT_ALLOWED.msg());
		}

		return newMessageButton;
	}

	private Component createFooterButtons()
	{
		final HorizontalLayout footer = new HorizontalLayout();

		footer.addComponent(createRemoveRelationshipButton());
		footer.addComponent(createNewMessageButton());

		if (device == Device.DESKTOP)
		{

			final Button listMessagesButton = new Button();
			listMessagesButton.setIcon(FontAwesome.COMMENTS);
			listMessagesButton.setDescription(I18N.RELATIONSHIPPOPUP_LIST_MESSAGES.msg());
			listMessagesButton.setCaption(I18N.RELATIONSHIPPOPUP_LIST_MESSAGES.msg());
			listMessagesButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
			listMessagesButton.addClickListener(event -> listener.showListMessages(this, relationship, device));
			listMessagesButton.setVisible(relationshipData.isExistsMessages());

			this.setModal(true);

			final Button printButton = new Button();
			printButton.setIcon(FontAwesome.PRINT);
			printButton.setDescription(I18N.RELATIONSHIPPOPUP_PRINT_BUTTON_CAPTION.msg());
			printButton.setCaption(I18N.RELATIONSHIPPOPUP_PRINT_BUTTON_CAPTION.msg());
			printButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
			printButton.setVisible(!relationshipData.targetUser.isDataDeleted());
			listener.setupPrintContent(printButton, relationship, relationshipData);

            if (relationshipData.targetUser.isDataDeleted()) {
                printButton.setEnabled(false);
            }
            if(relationshipData.targetUser.isCanceled()||
            relationshipData.targetUser.isAdminCanceled()){
                printButton.setEnabled(false);
                printButton.setVisible(false);
            }

			footer.addComponents(listMessagesButton, printButton);
		}

		final Button abuseButton = new Button();
		abuseButton.setIcon(FontAwesome.EXCLAMATION_CIRCLE);
		abuseButton.setDescription(I18N.RELATIONSHIPPOPUP_ABUSE_BUTTON.msg());

		if (device == Device.DESKTOP)
		{
			abuseButton.setCaption(I18N.RELATIONSHIPPOPUP_ABUSE_BUTTON.msg());
			abuseButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		}

		abuseButton.addClickListener(event -> listener.showReportAbuse(this, relationship, getWidth(), getHeight()));

		footer.addComponent(abuseButton);

		return footer;
	}

	private Component createAvatarComponent(File file)
	{
		final HorizontalLayout wrapper = new HorizontalLayout();
		wrapper.setWidth(100, Unit.PERCENTAGE);

		final Image image = new Image();
		image.setWidth(100, Unit.PERCENTAGE);

		if (targetUser.isCanceled() || targetUser.isDataDeleted() || targetUser.isAdminCanceled() || targetUser.getOrderedCategories().size()==0)
			image.setSource(new ThemeResource(AvatarUploadFile.canceledDummyAvatar));
		else
			image.setSource(file == null ? new ThemeResource(AvatarUploadFile.dummyAvatar) : new FileResource(file));

		wrapper.addComponent(image);
		return wrapper;
	}

	@Override
	public void onDeviceChanged(Device device)
	{
		this.device = device;
		createResponsiveLayout();
	}

	private void createResponsiveLayout()
	{
		mainComponent.removeAllComponents();
		footprintComponent.createFootprintComponent(relationship, relationshipData, device);

		if (device == Device.DESKTOP)
			createDesktopView();
		else if (device == Device.TABLET)
			createTabletView();
		else if (device == Device.MOBILE)
			createMobileView();
	}

	private void createMobileView()
	{
		this.setWidth("100%");
		this.setHeight("100%");
		this.setFooterVisible(true);
		mainComponent.addComponent(createMobileRelationshipProfileComponent());
		this.setHeaderVisible(false);
	}

	private void createTabletView()
	{
		this.setWidth("85%");
		this.setHeight("95%");
		this.setFooterVisible(true);
		this.setHeaderVisible(false);
		mainComponent.addComponent(createMobileRelationshipProfileComponent());
	}

	private void createDesktopView()
	{
		this.setWidth("70%");
		this.setHeight("90%");
		this.setFooterVisible(true);
		this.setHeaderVisible(true);
		mainComponent.addComponent(createDesktopRelationshipProfileComponent());
	}
}