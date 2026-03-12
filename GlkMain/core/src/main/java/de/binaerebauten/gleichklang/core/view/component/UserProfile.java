package de.binaerebauten.gleichklang.core.view.component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Audio;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import de.binaerebauten.gleichklang.core.initializer.AppUI;
import de.binaerebauten.gleichklang.core.model.FreeTextElement;
import de.binaerebauten.gleichklang.core.model.audio.UserAudio;
import de.binaerebauten.gleichklang.core.model.questionnaire.Answer;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.AudioRepository;
import de.binaerebauten.gleichklang.core.service.file.AvatarUploadFile;
import de.binaerebauten.gleichklang.core.utils.StringUtils;
import de.binaerebauten.gleichklang.core.utils.UserProfileUtil.UserInfo;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

public class UserProfile extends CustomComponent
{
	private static final Logger LOG = LoggerFactory.getLogger(UserProfile.class);
	@Autowired
	@Lazy
	AudioRepository audioRepository;

	public interface UserProfileListener
	{
		void openInfo(UserInfo userInfo, List<Answer> answers);

		void showMediaGalleries(UserProfile sender, User user, RecommendationCategory category);
	}

	public interface EditValueListener
	{
		void editValue(User user);
	}

	public static class UserProfileData
	{
		private final User user;
		private final List<FreeTextElement> freeText;
		private final File avatarFile;
		private final RecommendationCategory category;
		private final LinkedHashMap<UserInfo, List<Answer>> userAnswers;

		public UserProfileData(User user, List<FreeTextElement> freeText, File avatarFile, RecommendationCategory category, LinkedHashMap<UserInfo, List<Answer>> userAnswers)
		{
			this.user = Objects.requireNonNull(user);
			this.freeText = Objects.requireNonNull(freeText);
			this.category = Objects.requireNonNull(category);
			this.userAnswers = Objects.requireNonNull(userAnswers);

			this.avatarFile = avatarFile;
		}

		public User getUser()
		{
			return user;
		}

		public List<FreeTextElement> getFreeText()
		{
			return freeText;
		}

		public File getAvatarFile()
		{
			return avatarFile;
		}

		public RecommendationCategory getCategory()
		{
			return category;
		}

		public LinkedHashMap<UserInfo, List<Answer>> getUserAnswers()
		{
			return userAnswers;
		}
	}

	private final UserProfileData userProfileData;
	private final UserProfileListener userProfileListener;
	private final AbstractOrderedLayout mainLayout;

	private final ComponentReplacer<Button> editStatusButton = new ComponentReplacer<>();
	private final ComponentReplacer<Button> editFreeTextButton = new ComponentReplacer<>();
	private final ComponentReplacer<Button> editAvatarButton = new ComponentReplacer<>();
	private ClientInformation.Device device;

	public UserProfile(UserProfileData userProfileData, UserProfileListener userProfileListener)
	{
		this(userProfileData, userProfileListener, null);
	}

	public UserProfile(UserProfileData userProfileData, UserProfileListener userProfileListener, Device device)
	{
		Objects.requireNonNull(userProfileData);
		Objects.requireNonNull(userProfileListener);

		this.userProfileData = userProfileData;
		this.userProfileListener = userProfileListener;
		this.device = device;
		this.mainLayout = createProfileComponent();

		this.mainLayout.setStyleName(CssStyle.RELATIONSHIP_VIEW_RELATIONSHIP_POPUP.getStyleName());
		this.mainLayout.addStyleName(CssStyle.PREVIEW.getStyleName());

		setCompositionRoot(mainLayout);
	}

	public void refreshLayout(ClientInformation.Device device)
	{
		this.device = device;
		if (userProfileData != null && userProfileListener != null)
		{
			mainLayout.removeAllComponents();
			mainLayout.addComponent(createProfileComponent());
		}
	}

	public void setMargin(boolean enabled)
	{
		mainLayout.setMargin(enabled);
	}

	private Button createEditButton(EditValueListener editValueListener)
	{
		final Button editButton = new Button();

		editButton.setIcon(FontAwesome.PENCIL);
		editButton.addClickListener(event -> editValueListener.editValue(userProfileData.getUser()));
		editButton.setVisible(editValueListener != null);
		editButton.setStyleName(CssStyle.EDIT_BUTTON.getStyleName());

		return editButton;
	}

	private Component createStatusComponent()
	{
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setCaption(I18N.USERPROFILE_CAPTION_STATUS.msg());
		layout.setStyleName(CssStyle.MOBILE_BOTTOM_HEADER.getStyleName());

		final Label statusLabel = new Label(StringUtils.deSanitizeSpecialCharacters(userProfileData.getUser().getStatusMessage()));

		layout.addComponents(statusLabel, editStatusButton);

		return layout;
	}

	private AbstractOrderedLayout createProfileComponent()
	{
		final VerticalLayout layout = new VerticalLayout();

		final HorizontalLayout avatarFreeText = new HorizontalLayout();
		avatarFreeText.setSpacing(true);
		avatarFreeText.setStyleName(CssStyle.USER_PROFILE.getStyleName());

		final Component avatarComponent = createAvatarComponent(userProfileData);
		final Component freetextComponent = createFreeTextComponent(userProfileData);
		avatarFreeText.addComponents(avatarComponent, freetextComponent);

		avatarFreeText.setWidth(100, Unit.PERCENTAGE);
		avatarFreeText.setExpandRatio(avatarComponent, 0.0f);
		avatarFreeText.setExpandRatio(freetextComponent, 1.0f);

		if (device == null || device == ClientInformation.Device.DESKTOP)
		{
			layout.addComponents(
					createProfileHeader(),
					createInfoButtons(),
					createStatusComponent(),
					avatarFreeText
			);
		}
		else
		{
			layout.addComponents(
					createMobileHeader(),
					createInfoButtons(),
					createStatusComponent(),
					createMobileAvatarFreetext()
			);

		}

		return layout;
	}

	private Component createMobileAvatarFreetext()
	{
		final VerticalLayout wrapper = new VerticalLayout();

		wrapper.setStyleName(CssStyle.USER_PROFILE.getStyleName());
		wrapper.addComponents(createAvatarComponent(userProfileData), createFreeTextComponent(userProfileData));

		return wrapper;
	}

	private Component createProfileHeader()
	{
		final HorizontalLayout headerWrapper = new HorizontalLayout();
		headerWrapper.setStyleName(CssStyle.PROFILE_HEADER.getStyleName());
		headerWrapper.setWidth(100, Unit.PERCENTAGE);

		final HorizontalLayout wrapper = new HorizontalLayout();
		wrapper.setSpacing(true);
		headerWrapper.addComponent(wrapper);
		headerWrapper.setComponentAlignment(wrapper, Alignment.MIDDLE_LEFT);

		final Image headerImage = new Image();
		headerImage.setSource(new ThemeResource("img/icon_profile_white.svg"));
		headerImage.setWidth(45, Unit.PIXELS);
		wrapper.addComponent(headerImage);
		wrapper.setComponentAlignment(headerImage, Alignment.MIDDLE_LEFT);
		wrapper.setExpandRatio(headerImage, 0.0f);

		String userName = userProfileData.getUser().getAlias();
		if (device == ClientInformation.Device.MOBILE)
		{
			if (userName.length() > 19)
				userName = userName.substring(0, 19).concat("...");
		}
		final Label userNameLabel = new Label(userName, ContentMode.HTML);

		wrapper.addComponent(userNameLabel);
		wrapper.setComponentAlignment(userNameLabel, Alignment.MIDDLE_LEFT);

		return headerWrapper;
	}

	private Component createFreeTextComponent(UserProfileData userProfileData)
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setWidth(100, Unit.PERCENTAGE);

		final FreeTextComponent freeText = new FreeTextComponent(userProfileData);
		layout.addComponents(freeText, editFreeTextButton);
		return layout;
	}

	private Component createMobileHeader()
	{
		final VerticalLayout wrapper = new VerticalLayout();
		wrapper.setStyleName(CssStyle.PREVIEW_HEADER.getStyleName());
		wrapper.addComponents(createProfileHeader());

		return wrapper;
	}

	private Component createInfoButtons()
	{
		final ButtonDropdownComponent buttonDropdownComponent = new ButtonDropdownComponent(device);
		buttonDropdownComponent.setStyleName(CssStyle.USER_INFO_BUTTONS, CssStyle.TABSHEET_DROPDOWN_BLUE);
		buttonDropdownComponent.addRefreshListener(() -> refreshLayout(device));

		for (UserInfo userInfo : userProfileData.getUserAnswers().keySet())
		{
			final List<Answer> answers = userProfileData.getUserAnswers().get(userInfo);
			final ClickListener clickListener = event -> userProfileListener.openInfo(userInfo, answers);
			buttonDropdownComponent.addItem(userInfo.toString(), clickListener);
		}

		buttonDropdownComponent.addItem(I18N.USERPROFILE_MEDIA_BUTTON.msg(), event -> userProfileListener.showMediaGalleries(this, userProfileData.getUser(), userProfileData.getCategory()));

		return buttonDropdownComponent;
	}

	private Component createAvatarComponent(UserProfileData userProfileData)
	{

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		VerticalLayout verticalLayout = new VerticalLayout();

		final HorizontalLayout layout = new HorizontalLayout();
		final File file = userProfileData.getAvatarFile();

		final Image image = new Image();

		image.setSource(file == null ? new ThemeResource(AvatarUploadFile.dummyAvatar) : new FileResource(file));

		layout.addComponent(image);
		layout.addComponent(editAvatarButton);
		layout.setExpandRatio(image, 1.0f);
		layout.setExpandRatio(editAvatarButton, 0.0f);
		layout.setSizeUndefined();
		verticalLayout.addComponent(layout);
		audioRepository = AppUI.getApplicationContext().getBean(AudioRepository.class);
		Audio audio = new Audio();
		HorizontalLayout horizontalLayout1 = new HorizontalLayout();
		UserAudio userAudio=null;
		if(userProfileData.getCategory()==RecommendationCategory.FRIENDSHIP){
			userAudio =	audioRepository.findByAuthorIdAndForFriendship(userProfileData.getUser().getId());
		}
		else if(userProfileData.getCategory()==RecommendationCategory.PARTNERSHIP){
			userAudio = audioRepository.findByAuthorIdAndForPartnerShip(userProfileData.getUser().getId());
		}
		if(userAudio!=null) {
			String path = userAudio.getPath();
			if (device == Device.DESKTOP) {
				audio.setVisible(true);
				File mediaFile = new File(path);
				audio.setSource(new FileResource(mediaFile));
				horizontalLayout1.addComponent(audio);
			}
			else {
				try {
					Label audioLabel = new Label();
					byte[] audioBytes = Files.readAllBytes(new File(path).toPath());
					audioLabel.setContentMode(ContentMode.HTML);
					audioLabel.setValue("<audio controls src=\"data:audio/mp3;base64,"+new String(Base64.getEncoder().encode(audioBytes))+"\" type=\"audio/mpeg\"></audio>");
					horizontalLayout1.addComponent(audioLabel);
				} catch (IOException e) {
					LOG.error("createAvatarComponent", e);
				}
			}
			verticalLayout.addComponent(horizontalLayout1);
		}

		horizontalLayout.addComponent(verticalLayout);
		return horizontalLayout;
	}

	public void setEditStatusListener(EditValueListener editValueListener)
	{
		final Button editButton = createEditButton(editValueListener);
		editButton.setStyleName(CssStyle.EDIT_STATUS_BUTTON.getStyleName());
		editButton.addStyleName(CssStyle.EDIT_BUTTON.getStyleName());

		editStatusButton.setComponent(editButton);
	}

	public void setEditFreeTextListener(EditValueListener editValueListener)
	{
		final Button editButton = createEditButton(editValueListener);
		editButton.setStyleName(CssStyle.EDIT_PROFILE_BUTTON.getStyleName());
		editButton.addStyleName(CssStyle.EDIT_BUTTON.getStyleName());

		editFreeTextButton.setComponent(editButton);
	}

	public void setEditAvatarListener(EditValueListener editValueListener)
	{
		final Button editButton = createEditButton(editValueListener);
		editButton.setStyleName(CssStyle.EDIT_AVATAR_BUTTON.getStyleName());
		editButton.addStyleName(CssStyle.EDIT_BUTTON.getStyleName());

		editAvatarButton.setComponent(editButton);
	}
}