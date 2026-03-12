package de.binaerebauten.gleichklang.memberweb.presenter.handler;

import com.vaadin.server.BrowserWindowOpener;
import com.vaadin.server.Sizeable;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Window.CloseListener;
import de.binaerebauten.gleichklang.core.initializer.AppUI;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.matching.Relationship.Affiliation;
import de.binaerebauten.gleichklang.core.model.media.MediaGallery;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.questionnaire.Answer;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.presenter.PopupOpener;
import de.binaerebauten.gleichklang.core.presenter.media.MediaGalleryHandler;
import de.binaerebauten.gleichklang.core.repository.RelationshipRepository;
import de.binaerebauten.gleichklang.core.service.AnswerService;
import de.binaerebauten.gleichklang.core.service.MessageService;
import de.binaerebauten.gleichklang.core.service.RelationshipService;
import de.binaerebauten.gleichklang.core.service.UserDataService;
import de.binaerebauten.gleichklang.core.service.file.MediaService;
import de.binaerebauten.gleichklang.core.service.file.MediaUploadFile;
import de.binaerebauten.gleichklang.core.service.file.MessageUploadFile;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.UserProfileUtil;
import de.binaerebauten.gleichklang.core.utils.UserProfileUtil.UserInfo;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanItemsHandler;
import de.binaerebauten.gleichklang.core.view.component.OnDemandFileDownloader;
import de.binaerebauten.gleichklang.core.view.component.UserProfile.UserProfileData;
import de.binaerebauten.gleichklang.core.view.component.message.NewMessagePopup;
import de.binaerebauten.gleichklang.core.view.component.message.ShowMessagePopup;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.core.view.popup.I18N;
import de.binaerebauten.gleichklang.core.view.popup.InfoPopup;
import de.binaerebauten.gleichklang.core.view.popup.MediaGalleryPopup;
import de.binaerebauten.gleichklang.memberweb.service.print.PrintService;
import de.binaerebauten.gleichklang.memberweb.view.popup.MessageListPopup;
import de.binaerebauten.gleichklang.memberweb.view.popup.RelationshipPopup;
import de.binaerebauten.gleichklang.memberweb.view.popup.RelationshipPopup.RelationshipData;
import de.binaerebauten.gleichklang.memberweb.view.popup.RelationshipPopup.RelationshipPopupListener;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

public class RelationshipHandler implements RelationshipPopupListener
{
	
	private final AnswerService answerService;
	private final UserDataService userDataService;
	private final MediaService mediaService;
	private final MessageService messageService;
	private final RelationshipService relationshipService;
	private final RelationshipRepository relationshipRepository;
	private final PrintService printService;
	
	private final PopupOpener popupOpener;
	private final MediaGalleryHandler showMediaGalleryHandler;
	
	public RelationshipHandler(ApplicationContext ctx, PopupOpener popupOpener)
	{
		this.popupOpener = Objects.requireNonNull(popupOpener);
		
		userDataService = ctx.getBean(UserDataService.class);
		answerService = ctx.getBean(AnswerService.class);
		mediaService = ctx.getBean(MediaService.class);
		messageService = ctx.getBean(MessageService.class);
		relationshipService = ctx.getBean(RelationshipService.class);
		printService = ctx.getBean(PrintService.class);
		relationshipRepository = ctx.getBean(RelationshipRepository.class);
		
		showMediaGalleryHandler = new MediaGalleryHandler(ctx, popupOpener);
	}
	
	@Override
	public void openUserInfo(UserInfo userInfo, List<Answer> answers)
	{
		final InfoPopup infoPopup = new InfoPopup(userInfo.toString(), answers);
		popupOpener.tryOpenPopup(infoPopup);
	}
	
	@Override
	public void showMediaGalleries(RelationshipPopup sender, Relationship relationship, RelationshipData relationshipData, float popupWidthAsFloat, float popupHeightAsFloat)
	{
		final User targetUser = relationship.getTargetUser();
		final User sourceUser = relationship.getSourceUser();
		final Set<RecommendationCategory> recommendationCategories = relationshipData.getUserProfileDataMap().keySet();
		final Affiliation rating = relationshipData.getRating();
		final Map<MediaGallery, MediaUploadFile> mediaGalleryWithPreview = mediaService.getMediaGalleryWithPreview(targetUser, sourceUser, recommendationCategories, rating);
		
		final MediaGalleryPopup popup = new MediaGalleryPopup(showMediaGalleryHandler,	I18N.MEDIAGALLERYPOPUP_CAPTION_TITLE.msg(targetUser.getAlias()), mediaGalleryWithPreview);
		popup.setWidth(popupWidthAsFloat, Unit.PERCENTAGE);
		popup.setHeight(popupHeightAsFloat, Unit.PERCENTAGE);
		popupOpener.tryOpenPopup(popup);
	}
	
	@Override
	public void showNewMessage(RelationshipPopup sender, Relationship relationship)
	{
		final NewMessagePopup popup = new NewMessagePopup(messageService.createNewMessage(relationship.getSourceUser(), relationship.getTargetUser()));
		popup.setNewUploadCallback(this::newUploadFile);
		popup.setSendCallback(messageService::sendMessage);
		popup.setReceiver(relationship.getTargetUser());
		popup.setWidth(sender.getWidth(), Sizeable.Unit.PERCENTAGE);
		popup.setHeight(sender.getHeight(), Sizeable.Unit.PERCENTAGE);
		popup.center();
		popup.addStyleName(CssStyle.GENERIC_POPUP.getStyleName());
		popup.setDraggable(false);
		
		popupOpener.tryOpenPopup(popup);
	}
	
	@Override
	public void showListMessages(RelationshipPopup sender, Relationship relationship, Device device)
	{
		final LazyBeanItemsHandler<Message> messageHandler = messageService.createListMessagesHandler(relationship.getSourceUser(), relationship.getTargetUser());
		final MessageListPopup popup = new MessageListPopup(this::showMessage, messageHandler, device);
		popup.setWidth(sender.getWidth(), Sizeable.Unit.PERCENTAGE);
		popup.setHeight(sender.getHeight(), Sizeable.Unit.PERCENTAGE);
		popup.center();
		
		popupOpener.tryOpenPopup(popup);
	}
	
	@Override
	public void showReportAbuse(RelationshipPopup sender, Relationship relationship, float popupWidthAsFloat, float popupHeightAsFloat)
	{
		final NewMessagePopup popup = new NewMessagePopup(true, messageService.createAbuseMessage(relationship.getSourceUser(), relationship.getTargetUser()));
		popup.setSubjectEnabled(false);
		popup.setNewUploadCallback(this::newUploadFile);
		popup.setSendCallback(messageService::sendMessageToAdmin);
		popup.setWidth(popupWidthAsFloat, Sizeable.Unit.PERCENTAGE);
		popup.setHeight(popupHeightAsFloat, Sizeable.Unit.PERCENTAGE);
		popup.center();
		popup.addStyleName(CssStyle.GENERIC_POPUP.getStyleName());
		popup.setDraggable(false);
		
		popupOpener.tryOpenPopup(popup);
	}
	
	private void showMessage(Message message, float popupWidthAsFloat, float popupHeightAsFloat)
	{
		final ShowMessagePopup popup = new ShowMessagePopup(message, message.getReceiverEnvelope().getUser(), messageService.getMessageAttachments(message));
		
		popup.setWidth(popupWidthAsFloat, Sizeable.Unit.PERCENTAGE);
		popup.setHeight(popupHeightAsFloat, Sizeable.Unit.PERCENTAGE);
		
		popupOpener.tryOpenPopup(popup);
	}
	
	@Override
	public void setupPrintContent(Button button, Relationship relationship, RelationshipData data)
	{
		StreamResource streamResource = new StreamResource(new OnDemandFileDownloader.OnDemandStreamResource()
		{
			@Override
			public String getFileName()
			{
				return "Profil.pdf";
			}
			
			@Override
			public String getMimeType()
			{
				return "application/pdf";
			}
			
			@Override
			public InputStream getStream()
			{
				return new ByteArrayInputStream(printService.getRelationshipPrintout(relationship, data).getOutputStream().toByteArray());
			}
		}, "Profil_" + data.getTargetUser().getAlias() + ".pdf");
		
		BrowserWindowOpener browserWindowOpener = new BrowserWindowOpener(streamResource);
		browserWindowOpener.extend(button);
	}
	
	private MessageUploadFile newUploadFile(Message message, Collection<MessageUploadFile> currentUploadFiles) throws ValidationException
	{
		return messageService.createMessageAttachment(message, currentUploadFiles.size());
	}
	
	public RelationshipPopup createRelationshipPopup(Relationship relationship, RecommendationCategory selectedCategory, CloseListener closeListener, ClientInformation.Device device)
	{
		RelationshipPopup relationshipPopup = null;

		final Relationship inverseRelationship = relationshipService.getInverseRelationship(relationship);
		if (inverseRelationship != null) {
			final SortedMap<RecommendationCategory, UserProfileData> userProfileDataMap = new TreeMap<>();
			for (RecommendationCategory category : relationship.getCategories()) {
				userProfileDataMap.put(category, userDataService.createUserProfileData(relationship.getTargetUser(), category));
			}

			final Collection<RecommendationCategory> categories = selectedCategory != null ? Collections.singleton(selectedCategory) : relationship.getCategories();
			final LinkedHashMap<UserInfo, List<Answer>> userAnswers = new LinkedHashMap<>();
			for (UserInfo userInfo : UserInfo.values()) {
				final List<Answer> answers = UserProfileUtil.getNaturalKeys(userInfo, categories, relationship.getTargetUser(), answerService::getAnswersVisibleToOtherUsers);
				if (answers != null && !answers.isEmpty()) {
					userAnswers.put(userInfo, answers);
				}
			}

			final RelationshipData relationshipData = new RelationshipData(
					relationship.getTargetUser(),
					inverseRelationship.getAffiliation(),
					inverseRelationship.getFootprint(),
					inverseRelationship.getFootprintDate(),
					isAllowMessage(relationship),
					messageService.isExistsMessages(relationship),
					mediaService.isExistsMediaGallery(relationship, inverseRelationship),
					userProfileDataMap,
					userAnswers);

			final Affiliation oldAffiliation = relationship.getAffiliation();
				relationshipPopup = new RelationshipPopup(relationship, relationshipData, selectedCategory, this, device);
				relationshipPopup.addCloseListener(e ->
				{
					relationshipService.saveViewedRelationship(relationship, oldAffiliation);

					//first save data, then refresh view. otherwise old data will be loaded
					if (closeListener != null) {
						closeListener.windowClose(e);
					}
				});
			}

		return relationshipPopup;
	}
	
	private boolean isAllowMessage(Relationship relationship)
	{
		return !relationship.getTargetUser().isDataDeleted() && relationship.getTargetUser().getOrderedCategories().size()!=0 && !relationship.getTargetUser().isCanceled() && !relationship.getTargetUser().isAdminCanceled() && !Collections.disjoint(relationship.getCategories(), relationship.getTargetUser().getCategories());
	}
	
	@Override
	public void cancelRelationship(RelationshipPopup sender, Relationship relationship)
	{
		final NewMessagePopup popup = new NewMessagePopup(messageService.createCancelMessage(relationship.getSourceUser(), relationship.getTargetUser()));
		popup.setSendCallback((message, messageUploadFiles) ->
		{
			messageService.sendMessage(message, messageUploadFiles, false);
			relationship.setDeleted(true);
			relationship.setDeleteDate(LocalDateTime.now());
			relationship.setDeletedBy("S");
            updateDeletedBy(relationship);
			sender.close();
		});
		popup.setReceiver(relationship.getTargetUser());
		popupOpener.tryOpenPopup(popup);
	}

	public void updateDeletedBy(Relationship relationshipS)
    {
        RelationshipRepository relationshipRepository = AppUI.getApplicationContext().getBean(RelationshipRepository.class);

        Relationship relationshipO = relationshipRepository.findRelationshipBySourceUserIdAndTargetUserId(relationshipS.getTargetUserId(),relationshipS.getSourceUserId());
		relationshipO.setDeletedBy("O");
		relationshipO.setDeleteDate(LocalDateTime.now());
		relationshipRepository.save(relationshipO);
    }
}
