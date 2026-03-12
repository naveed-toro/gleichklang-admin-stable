package de.binaerebauten.gleichklang.memberweb.presenter;

import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.UI;
import de.binaerebauten.gleichklang.core.model.NaturalKeyEntity.NaturalKey;
import de.binaerebauten.gleichklang.core.model.media.Avatar;
import de.binaerebauten.gleichklang.core.model.media.FileEntity;
import de.binaerebauten.gleichklang.core.model.media.MediaGallery;
import de.binaerebauten.gleichklang.core.model.questionnaire.Answer;
import de.binaerebauten.gleichklang.core.model.questionnaire.QuestionnaireActivation;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.repository.AnswerRepository;
import de.binaerebauten.gleichklang.core.repository.AvatarRepository;
import de.binaerebauten.gleichklang.core.repository.MediaGalleryRepository;
import de.binaerebauten.gleichklang.core.service.AnswerService;
import de.binaerebauten.gleichklang.core.service.LocatableService;
import de.binaerebauten.gleichklang.core.service.UserDataService;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.service.file.*;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.UserProfileUtil.UserInfo;
import de.binaerebauten.gleichklang.core.view.component.UserProfile;
import de.binaerebauten.gleichklang.core.view.component.UserProfile.UserProfileListener;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.core.view.popup.AnswerPopup;
import de.binaerebauten.gleichklang.core.view.popup.ChangeValuePopup;
import de.binaerebauten.gleichklang.core.view.popup.GenericPopup;
import de.binaerebauten.gleichklang.core.view.popup.InfoPopup;
import de.binaerebauten.gleichklang.memberweb.view.ProfileView;
import de.binaerebauten.gleichklang.memberweb.view.ProfileView.MemberProfileViewListener;
import de.binaerebauten.gleichklang.memberweb.view.popup.AvatarPopup;
import de.binaerebauten.gleichklang.memberweb.view.popup.AvatarPopup.AvatarCallback;
import org.springframework.context.ApplicationContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ProfilePresenter extends NavigatePresenter implements MemberProfileViewListener, UserProfileListener, AvatarCallback
{
	private final ProfileView view;
	private final RecommendationCategory category;
	private final QuestionnaireActivation activation;
	
	private final UserService userService;
	private final UserDataService userDataService;
	private final AvatarService avatarService;
	private final AnswerService answerService;
	private final AnswerRepository answerRepository;
	private final LocatableService locatableService;
	private final MediaGalleryRepository mediaGalleryRepository;
	private final MediaService mediaService;
	private final FileService fileService;
	private final AvatarRepository avatarRepository;

	public ProfilePresenter(ApplicationContext ctx, RecommendationCategory category, ProfileView view, QuestionnaireActivation activation)
	{
		super(view);
		
		this.view = view;
		this.category = category;
		this.activation = activation;
		
		userService = ctx.getBean(UserService.class);
		userDataService = ctx.getBean(UserDataService.class);
		avatarService = ctx.getBean(AvatarService.class);
		answerService = ctx.getBean(AnswerService.class);
		answerRepository = ctx.getBean(AnswerRepository.class);
		locatableService = ctx.getBean(LocatableService.class);
		mediaGalleryRepository = ctx.getBean(MediaGalleryRepository.class);
		mediaService = ctx.getBean(MediaService.class);
		fileService = ctx.getBean(FileService.class);
		avatarRepository = ctx.getBean(AvatarRepository.class);

		view.setListener(this);
		
	}
	
	@Override
	public void enter(String parameters)
	{
		refresh();
	}
	
	private void refresh()
	{
		final User currentUser = userService.getCurrentUser();
		this.view.setUserData(userDataService.createUserProfileData(currentUser, category), this);
	}
	
	@Override
	public void leave()
	{
		this.view.setUserData(null, this);
		
		super.leave();
	}
	
	@Override
	public void editStatus(User user)
	{
		final ChangeValuePopup popup = new ChangeValuePopup
				(
						I18N.PROFILEPRESENTER_CAPTION_PROFILE.msg(),
						I18N.PROFILEPRESENTER_CAPTION_STATUSTITLE.msg(),
						I18N.PROFILEPRESENTER_CAPTION_STATUSDESCRIPTION.msg(),
						user.getStatusMessage(),
						this::saveStatus
				);
		popup.setIcon(new ThemeResource("img/icon_profile_white.svg"));
		popup.addCloseListener(e -> refresh());
		
		tryOpenPopup(popup);
	}
	
	private void saveStatus(String value) throws ValidationException
	{
		// get current user because other values could be changed and should not overwritten
		final User currentUser = userService.getCurrentUser();
		currentUser.setStatusMessage(value);
		try
		{
			userService.save(currentUser);
		}
		catch (UniqueValidationException e)
		{
			throw new ValidationException(e.getMessage());
		}
	}

	private void saveAvatarsInAvatarGallery(AvatarUploadFile avatar)
	{
		MediaGallery avatarGallery = mediaGalleryRepository.findAvatarGalleryByAuthor(userService.getCurrentUser());

		if (avatarGallery == null)
		{
			avatarGallery = new MediaGallery();
			avatarGallery.setAsAvatarGallery();
			avatarGallery.setAuthor(userService.getCurrentUser());
			avatarGallery.setName(I18N.PROFILEPRESENTER_AVATAR_GALLERY.msg());
			avatarGallery.setSecret(true);
			mediaGalleryRepository.save(avatarGallery);
		}

		try
		{
			BufferedImage inputImage = ImageIO.read(new File(avatar.getPath().toString()));

			File file = new File(avatar.getPath().toString());
			FileEntity persistedFile = fileService.saveAvatar(file, inputImage);

			MediaUploadFile media = new MediaUploadFile(avatarGallery, fileService, persistedFile);

			mediaService.saveWithoutValidation(media);

			avatar.getAvatar().setMediafile(persistedFile);

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void editAvatar(User user)
	{
		final AvatarPopup avatarPopup = new AvatarPopup(avatarService.createAvatars(user), this,
				(file, cat) -> saveAvatarFromMediaGallery(file, cat, user), user.getOrderedCategories(), this::saveAvatarsInAvatarGallery);
		avatarPopup.selectCategory(category);
		avatarPopup.addCloseListener(e -> refresh());
		tryOpenPopup(avatarPopup);
	}
	
	@Override
	public void saveAvatars(Collection<AvatarUploadFile> avatarUploadFiles)
	{
		avatarUploadFiles.forEach(AbstractUploadFile::save);
	}
	
	@Override
	public void editFreeText(User user)
	{
		final NaturalKey naturalKey = AnswerService.getFreeTextNaturalKey(category);
		
		final List<Answer> answers = answerService.getAnswers(user, naturalKey, true);
		final AnswerPopup popup = new AnswerPopup(answers, I18N.PROFILEPRESENTER_CAPTION_PROFILE.msg(), locatableService, activation, this::saveAnswers);
		popup.addStyleName(CssStyle.POPUP_TYPE_GREEN.getStyleName());
		popup.setIcon(new ThemeResource("img/question-icon.svg"));
		popup.setBoxSize(GenericPopup.BoxSize.WIDE);
		popup.setHeight(80, Sizeable.Unit.PERCENTAGE);
		popup.addCloseListener(e -> refresh());
		tryOpenPopup(popup);
	}

	@Override
	public List<MediaUploadFile> getMedias(MediaGallery mediaGallery)
	{
		return mediaService.getMedias(mediaGallery);
	}

	@Override
	public Map<MediaGallery, MediaUploadFile> getMediaGalleries()
	{
		final User currentUser = userService.getCurrentUser();
		return mediaService.getMediaGalleryWithPreview(currentUser);
	}

	private void saveAvatarFromMediaGallery(MediaUploadFile mediaUploadFile, RecommendationCategory recommendationCategory, User currentUser)
	{
		BufferedImage inputImage = null;
		try
		{
			inputImage = ImageIO.read(new File(mediaUploadFile.getPath().toString()));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		File file = new File(mediaUploadFile.getPath().toString());
		FileEntity persistedFile = fileService.saveAvatar(file, inputImage);
		FileEntity persistedThumbnail = fileService.saveAvatarThumbnail(file, inputImage);

		Avatar oldAvatar = avatarRepository.findByUserAndCategory(currentUser, recommendationCategory);
		if (oldAvatar != null)
		{
			oldAvatar.setFile(persistedFile);
			oldAvatar.setThumbnail(persistedThumbnail);
			oldAvatar.setMediafile(mediaUploadFile.getFileEntity());
			avatarRepository.save(oldAvatar);
		}
		else
		{
			Avatar avatar = new Avatar();
			avatar.setCategory(recommendationCategory);
			avatar.setUser(currentUser);

			avatar.setFile(persistedFile);
			avatar.setThumbnail(persistedThumbnail);
			avatar.setMediafile(mediaUploadFile.getFileEntity());


			avatarRepository.save(avatar);
		}

	}
	
	private void saveAnswers(List<Answer> answers)
	{
		this.answerRepository.save(answers);
	}
	
	@Override
	public void openInfo(UserInfo userInfo, List<Answer> answers)
	{
		final InfoPopup infoPopup = new InfoPopup(userInfo.toString(), answers);
		infoPopup.setEditAnswerListener(answer ->
		{
			infoPopup.close();
			openEditAnswer(answer, userInfo, answers);
		});
		tryOpenPopup(infoPopup);
	}
	
	@Override
	public void showMediaGalleries(UserProfile sender, User user, RecommendationCategory category)
	{
		UI.getCurrent().getNavigator().navigateTo("MEDIA");
	}
	
	private void openEditAnswer(Answer answer, UserInfo userInfo, List<Answer> answers)
	{
		final AnswerPopup popup = new AnswerPopup(answer, I18N.PROFILEPRESENTER_CAPTION_PROFILE.msg(), locatableService, activation, this::saveAnswers);
		popup.addStyleName(CssStyle.POPUP_TYPE_GREEN.getStyleName());
		popup.setIcon(new ThemeResource("img/question-icon.svg"));
		popup.setBoxSize(GenericPopup.BoxSize.WIDE);
		popup.setHeight(80, Sizeable.Unit.PERCENTAGE);
		popup.addCloseListener(e -> openInfo(userInfo, answers));
		tryOpenPopup(popup);
	}
}