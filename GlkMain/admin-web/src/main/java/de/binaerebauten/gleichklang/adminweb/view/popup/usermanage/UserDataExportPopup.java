package de.binaerebauten.gleichklang.adminweb.view.popup.usermanage;

import com.vaadin.server.*;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.adminweb.service.ExportService;
import de.binaerebauten.gleichklang.adminweb.service.UserDataExportService;
import de.binaerebauten.gleichklang.core.initializer.AppUI;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.locatable.Continent;
import de.binaerebauten.gleichklang.core.model.locatable.Country;
import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.model.user.*;
import de.binaerebauten.gleichklang.core.repository.*;
import de.binaerebauten.gleichklang.core.repository.message.MessageRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.service.LocatableService;
import de.binaerebauten.gleichklang.core.service.file.AudioService;
import de.binaerebauten.gleichklang.core.view.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.binaerebauten.gleichklang.core.service.RelationshipService;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.concurrent.ExecutionException;

import de.binaerebauten.gleichklang.adminweb.view.popup.I18N;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.concurrent.ListenableFuture;

import javax.transaction.Transactional;

public class UserDataExportPopup extends Popup
{
	private static final Logger LOG = LoggerFactory.getLogger(UserDataExportPopup.class);
	public static final String EXPORT_DATA_RESOURCE_KEY = "data";
	private  CheckBox exportCheckBoxUser,exportCheckBoxPrtnershipMessage,exportCheckBoxFriendshipMessage,exportCheckBoxSatisfaction,exportCheckBoxReasonForCancellation,exportCheckBoxAvtar,exportCheckBoxAnonymize;
	private final RelationshipService relationService;

	private final MessageRepository messageRepository;
	private final I18NRepository i18NRepository;
	public final QuestionnaireRepository questionnaireRepository;
	public final AnswerRepository answerRepository;

	private final AudioService audioService;
	private final ExportService exportService;
	private final UserDataExportService userDataExportService;
	private final LocatableService locatableService;
	private Specification<User> specification;
	private final AvatarRepository avatarRepository;
	public final LazyBeanPagingComponent<Questionnaire> questionnaireTable;
	public final Button export;
	private final String EXPORT_FILE_NAME="export.csv";
	public final Image waiting;
	public final Label waitingLabel;
	public final List<User> users;

	public UserDataExportPopup(List<User> users)
	{

		this.users=users;
		exportService = AppUI.getApplicationContext().getBean(ExportService.class);
		userDataExportService = AppUI.getApplicationContext().getBean(UserDataExportService.class);
		locatableService = AppUI.getApplicationContext().getBean(LocatableService.class);

		relationService = AppUI.getApplicationContext().getBean(RelationshipService.class);
		messageRepository = AppUI.getApplicationContext().getBean(MessageRepository.class);
		i18NRepository = AppUI.getApplicationContext().getBean(I18NRepository.class);
		audioService = AppUI.getApplicationContext().getBean(AudioService.class);
		avatarRepository = AppUI.getApplicationContext().getBean(AvatarRepository.class);
		questionnaireRepository = AppUI.getApplicationContext().getBean(QuestionnaireRepository.class);
		answerRepository = AppUI.getApplicationContext().getBean(AnswerRepository.class);

		this.exportCheckBoxUser=createExportCheckBox(I18N.USERMANAGEPOPUP_CAPTION_EXPORT_CHECKBOX_USER.msg());
		this.exportCheckBoxPrtnershipMessage=createExportCheckBox(I18N.USERMANAGEPOPUP_CAPTION__EXPORT_CHECKBOX_PARTNERSHIP.msg());
		this.exportCheckBoxFriendshipMessage=createExportCheckBox(I18N.USERMANAGEPOPUP_CAPTION__EXPORT_CHECKBOX_FRIENDSHIP.msg());
		this.exportCheckBoxSatisfaction=createExportCheckBox(I18N.USERMANAGEPOPUP_CAPTION__EXPORT_CHECKBOX_SATISFACTION.msg());
		this.exportCheckBoxReasonForCancellation=createExportCheckBox(I18N.USERMANAGEPOPUP_CAPTION__EXPORT_CHECKBOX_CANCELLATION.msg());
		this.exportCheckBoxAvtar=createExportCheckBox(I18N.USERMANAGEPOPUP_CAPTION__EXPORT_CHECKBOX_AVATAR.msg());
		this.exportCheckBoxAnonymize=createExportCheckBox(I18N.USERMANAGEPOPUP_CAPTION__EXPORT_CHECKBOX_ANONYMIZE.msg());

		waiting = new Image();
		waiting.setIcon(new ThemeResource("img/waiting.gif"));
		waiting.setHeight(50, Unit.PIXELS);
		waitingLabel = new Label(/*"Exporting User Data"*/);
		waiting.setVisible(false);
		waitingLabel.setVisible(false);

		final HorizontalLayout mianLayout = new HorizontalLayout();

		final VerticalLayout layoutAttributes = new VerticalLayout();

		layoutAttributes.setMargin(true);
		layoutAttributes.setSpacing(true);


		final VerticalLayout layoutQuestionnaire = new VerticalLayout();

		layoutQuestionnaire.setMargin(true);
		layoutQuestionnaire.setSpacing(true);
		layoutQuestionnaire.setWidth(100,Unit.PERCENTAGE);

		questionnaireTable=createQuestionnaireTable();
		questionnaireTable.setHandler(((LazyBeanItemContainer.LazyBeanFilteredItemsHandler<Questionnaire>)questionnaireRepository::findAll));

		export = new Button();
		export.setCaption(de.binaerebauten.gleichklang.adminweb.view.I18N.USERMANAGE_ACTION_START_EXPORT.msg());

		export.addClickListener((event) -> {

			//close();
			startExport();
		});


		layoutQuestionnaire.addComponent(new BoldLabel(de.binaerebauten.gleichklang.adminweb.view.I18N.USERMANAGE_ACTION_START_QUESTIONNARE_LABEL.msg()));
		layoutQuestionnaire.addComponent(questionnaireTable);



		layoutQuestionnaire.addComponent(waiting);
		layoutQuestionnaire.addComponent(waitingLabel);


		layoutQuestionnaire.addComponent(export);
		layoutAttributes.addComponent(exportCheckBoxUser);
		layoutAttributes.addComponent(exportCheckBoxPrtnershipMessage);
		layoutAttributes.addComponent(exportCheckBoxFriendshipMessage);
		layoutAttributes.addComponent(exportCheckBoxSatisfaction);
		layoutAttributes.addComponent(exportCheckBoxReasonForCancellation);
		layoutAttributes.addComponent(exportCheckBoxAvtar);
		layoutAttributes.addComponent(exportCheckBoxAnonymize);

		mianLayout.addComponent(layoutAttributes);
		mianLayout.addComponent(layoutQuestionnaire);

		setContent(mianLayout);


	}

	@Override
	public void close()
	{
		this.setVisible(false);
	}
	private LazyBeanPagingComponent<Questionnaire> createQuestionnaireTable()
	{
		final LazyBeanPagingComponent<Questionnaire> table = new LazyBeanPagingComponent<>();
		table.setSelectable(true);
		table.setMultiSelect(true);
		table.setSortPropertyId(false, Questionnaire_.changeDate);
		table.setSortPropertyId(true, Questionnaire_.createDate);
		table.setSizeFull();

		table.addGeneratedColumn(de.binaerebauten.gleichklang.adminweb.view.I18N.QUESTIONNAIRE_HEADER_KEY.msg(), (source, itemId, columnId) -> itemId.getI18nKey());
		table.addGeneratedColumn(de.binaerebauten.gleichklang.adminweb.view.I18N.QUESTIONNAIRE_HEADER_RECOMMENDATIONCATEGORY.msg(), (source, itemId, columnId) -> itemId.getRecommendationCategory());
		table.addGeneratedColumn(de.binaerebauten.gleichklang.adminweb.view.I18N.QUESTIONNAIRE_HEADER_NAME.msg(), (source, itemId, columnId) -> itemId.getName());

		return table;
	}

	@Transactional(value = Transactional.TxType.REQUIRED)
	public String[] getHeaders(){

		List<String> finalHeaders=new ArrayList<>();

		finalHeaders.add("user_id");

		if(!this.exportCheckBoxAnonymize.getValue())
		{
			finalHeaders.add("email");
			finalHeaders.add("lastname");
			finalHeaders.add("firstname");
			finalHeaders.add("alias");
			finalHeaders.add("subscription_state");
		}

		if(this.exportCheckBoxUser.getValue())
		{
			finalHeaders.add("Date_Of_registeration");
			finalHeaders.add("Legnth_of_Membership");
			finalHeaders.add("Number_of_partnership_suggestions");
			finalHeaders.add("Number_of_friendship_suggestions");
		}

		if(this.exportCheckBoxPrtnershipMessage.getValue())
		{
			finalHeaders.add("IncomingsByUser_p");
			finalHeaders.add("OutgoingsByUser_p");
			finalHeaders.add("NumberOfFirstWrote_p");
		}

		if(this.exportCheckBoxFriendshipMessage.getValue())
		{
			finalHeaders.add("IncomingsByUser_f");
			finalHeaders.add("OutgoingsByUser_f");
			finalHeaders.add("NumberOfFirstWrote_f");
		}

		if(this.exportCheckBoxSatisfaction.getValue())
		{
			finalHeaders.add("Satisfaction with Gleichklang");
			finalHeaders.add("Satisfaction with other platforms");
			finalHeaders.add("Success");
		}

		if(this.exportCheckBoxReasonForCancellation.getValue())
		{
			finalHeaders.add("Reasons For Cancellation");
		}

		if(this.exportCheckBoxAvtar.getValue())
		{
			finalHeaders.add("Has An Profile photo For Partnership");
			finalHeaders.add("Has An Profile photo Friendship");
			finalHeaders.add( "Has An Audio For Partnership");
			finalHeaders.add("Has An Audio For Friendship");
		}


		List<Object> headerList=null;

		List <Long> qids=new ArrayList<>();

		UserRepository userRepository= AppUI.getApplicationContext().getBean(UserRepository.class);
		List<User> users = userRepository.findAll(this.specification);

		this.questionnaireTable.getValues().forEach(q->qids.add(q.getId()));
		boolean regionQuestionare =false;

		if(qids.size()!=0)
		{
			headerList= userDataExportService.getHeadersForUserDataExportFromAdmin(qids);

			for (Object header : headerList) {
				if (header != null && ((String) header).contains("#")) {
					finalHeaders.add(((String) header));
				}

			}


			this.questionnaireTable.getValues().forEach(q->{
				if(q.getI18nKey().equals("partner.region_question")||q.getI18nKey().equals("friend.region_question"))
				{

						final String [] regionHeaders=userDataExportService.getRegionsHeaders();


						for (String regionHeader : regionHeaders)
						{
							finalHeaders.add(regionHeader);
						}

					users.forEach(user -> {


							final Map<String,String> proximityHeadersMap = userDataExportService.getProximityHeadersAndAnswersByUser(user,this.getlanguage());


							for (String proximityHeader : proximityHeadersMap.keySet())
							{
								finalHeaders.add(proximityHeader);
							}
					});
				}
			});

		}
		LinkedHashSet<String> finalHeadersHashSet = new LinkedHashSet<>(finalHeaders);
		List<String> listWithoutDuplicateHeaders = new ArrayList<>(finalHeadersHashSet);

		return Arrays.copyOf(listWithoutDuplicateHeaders.toArray(), listWithoutDuplicateHeaders.toArray().length, String[].class);
	}

	private boolean isAnyAttributeOrQuestionareSelected()
	{
		List <Long> qids=new ArrayList<>();
		this.questionnaireTable.getValues().forEach(q->qids.add(q.getId()));

		boolean isAnyAttributeOrQuestionareSelected=
				this.exportCheckBoxAnonymize.getValue() ||
						this.exportCheckBoxUser.getValue() ||
						this.exportCheckBoxPrtnershipMessage.getValue() ||
						this.exportCheckBoxFriendshipMessage.getValue() ||
						this.exportCheckBoxSatisfaction.getValue() ||
						this.exportCheckBoxReasonForCancellation.getValue() ||
						this.exportCheckBoxAvtar.getValue() ||
						qids.size()!=0;
		return  isAnyAttributeOrQuestionareSelected;

	}

	@Transactional(value = Transactional.TxType.REQUIRED)
	public List getRecords(User user){

		List records = new ArrayList();
		records.add(user.getId());
		if(!this.exportCheckBoxAnonymize.getValue())
		{


			records.add(user.getEmail());
			records.add(user.getLastName());
			records.add(user.getFirstName());
			records.add(user.getAlias());
			records.add(user.getMemberStatus());
		}

		if(this.exportCheckBoxUser.getValue())
		{
			records.add(user.getCreateDate());
			records.add(dateDiffInYearMonthDays(user.getCreateDate()));
			records.add(relationService.getRelationshipCountForUser(user,RecommendationCategory.PARTNERSHIP));
			records.add(relationService.getRelationshipCountForUser(user,RecommendationCategory.FRIENDSHIP));
		}

		if(this.exportCheckBoxPrtnershipMessage.getValue())
		{
			records.add(messageRepository.countIncomingsByUser(user,RecommendationCategory.PARTNERSHIP));
			records.add(messageRepository.countOutgoingsByUser(user,RecommendationCategory.PARTNERSHIP));
			records.add(relationService.getNumberOfFirstWroteStatisticsByCategory(user,RecommendationCategory.PARTNERSHIP).values().toArray()[0]);
		}

		if(this.exportCheckBoxFriendshipMessage.getValue())
		{
			records.add(messageRepository.countIncomingsByUser(user,RecommendationCategory.FRIENDSHIP));
			records.add(messageRepository.countOutgoingsByUser(user,RecommendationCategory.FRIENDSHIP));
			records.add(relationService.getNumberOfFirstWroteStatisticsByCategory(user,RecommendationCategory.FRIENDSHIP).values().toArray()[0]);
		}

		if(this.exportCheckBoxSatisfaction.getValue())
		{
			records.add(i18NRepository.satisfactionWithHarmony(user.getId()));
			records.add(i18NRepository.successOfOtherPlatform(user.getId()));
			records.add(i18NRepository.successOfMediation(user.getId()));
		}

		if(this.exportCheckBoxReasonForCancellation.getValue())
		{
			records.add(user.getCancelReasons().toString());
		}

		if(this.exportCheckBoxAvtar.getValue())
		{
			records.add(avatarRepository.findByUserAndCategory(user,RecommendationCategory.PARTNERSHIP)!=null?"YES":"NO");
			records.add(avatarRepository.findByUserAndCategory(user,RecommendationCategory.FRIENDSHIP)!=null?"YES":"NO");
			records.add(audioService.isAudioPresentForPartnership(user)?"YES":"NO");
			records.add(audioService.isAudioPresentForFriendship(user)?"YES":"NO");

		}

		return records;
	}

	public void startExport()
	{
		disableStartExport();

		if(isAnyAttributeOrQuestionareSelected())
		{
			final ListenableFuture<ByteArrayOutputStream> byteArrayOutputStreamFuture = exportService.exportUserDataAdmin(this.users, this);
			byteArrayOutputStreamFuture.addCallback(result -> {
				StreamResource resource = new StreamResource(() -> {
					InputStream inputStream = null;
					try {
						LOG.info("addcallback of startExport");
						inputStream = new ByteArrayInputStream(byteArrayOutputStreamFuture.get().toByteArray());
					} catch (InterruptedException | ExecutionException e) {
						LOG.error("Error in Data export of users from User Controll", e);
					}
					return inputStream;
				}, EXPORT_FILE_NAME);

				setResource(EXPORT_DATA_RESOURCE_KEY, resource);
				ResourceReference ref = ResourceReference.create(resource, this, EXPORT_DATA_RESOURCE_KEY);
				Page.getCurrent().open(ref.getURL(), null);
				enableStartExport();
			}, result ->
			{
				Notification.show("Export failure", Notification.Type.ERROR_MESSAGE);
				LOG.error("Export started failure", result);
				enableStartExport();
			});
		}
		else
		{

			StreamResource resource = new StreamResource(() -> {
				InputStream inputStream = null;

				inputStream = exportService.exportUserDataAdminBasic(this.users);

				return inputStream;
			}, EXPORT_FILE_NAME);

			setResource(EXPORT_DATA_RESOURCE_KEY, resource);
			ResourceReference ref = ResourceReference.create(resource, this, EXPORT_DATA_RESOURCE_KEY);
			Page.getCurrent().open(ref.getURL(), null);
			enableStartExport();
		}

	}

	public String dateDiffInYearMonthDays(LocalDateTime createDate)
	{
		LocalDate pdate = LocalDate.of(createDate.getYear(), createDate.getMonth(), createDate.getDayOfMonth());
		LocalDate now = LocalDate.now();

		Period diff = Period.between(pdate, now);

		return (diff.getYears()!=0?diff.getYears()+" Years ":"")+(diff.getMonths()!=0?diff.getMonths()+" Months ":"")+ (diff.getDays()!=0?diff.getDays()+" Days ":"");
	}

	private CheckBox createExportCheckBox(String label)
	{
		final CheckBox checkBox = ComponentFactory.getInstance().createField(CheckBox.class, label);
		checkBox.setValue(false);
		checkBox.setVisible(true);

		return checkBox;
	}


	public void disableStartExport()
	{
		waiting.setVisible(true);
		waitingLabel.setVisible(true);
		export.setEnabled(false);
	}

	public void enableStartExport()
	{
		waiting.setVisible(false);
		waitingLabel.setVisible(true);
		/*waitingLabel.setCaption("Export Done");*/
		export.setEnabled(true);
	}

	public I18NEntity.Language getlanguage()
	{
		return I18NEntity.Language.DE;
	}

}

