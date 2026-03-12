package de.binaerebauten.gleichklang.adminweb.view.popup.usermanage;

import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.adminweb.view.component.UserQuickBar.UserQuickBarListener;
import de.binaerebauten.gleichklang.adminweb.view.model.UserControlData;
import de.binaerebauten.gleichklang.adminweb.view.popup.I18N;
import de.binaerebauten.gleichklang.core.model.matching.MatrixValue.Strictness;
import de.binaerebauten.gleichklang.core.model.payment.Subscription.SubscriptionState;
import de.binaerebauten.gleichklang.core.model.user.BlockedStatus;
import de.binaerebauten.gleichklang.core.model.user.MemberStatus;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.view.component.ToggleButton;
import de.binaerebauten.gleichklang.core.view.component.ToggleButton.SimpleToggleClickListener;
import de.binaerebauten.gleichklang.core.view.component.ToggleButton.ToggleStyle;

import java.time.LocalDate;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

import static de.binaerebauten.gleichklang.adminweb.service.matching.GenerateSuggestionService.CHECK_PERIOD;
import static de.binaerebauten.gleichklang.core.utils.MatchingUtils.ALLOWED_MISSING_ANSWERS_RATIO;
import static de.binaerebauten.gleichklang.core.view.component.ToggleButton.ToggleGoal.NO;
import static de.binaerebauten.gleichklang.core.view.component.ToggleButton.ToggleGoal.YES;

public class OverviewComponent extends CustomComponent
{
	public interface OverviewComponentListener extends UserQuickBarListener
	{
		boolean isMailBlacklisted(User user);
		
		void removeFromBlacklist(User user);
		
		void addToBlacklist(User user);
		
		void activateCategory(User user, RecommendationCategory category);
		
		void deactivateCategory(User user, RecommendationCategory category);
		
		void activateRecommendationBreak(UserManagePopup sender, User user, RecommendationCategory category);
		
		void deactivateRecommendationBreak(UserManagePopup sender, User user, RecommendationCategory category);
		
		Map<RecommendationCategory, LocalDate> getRecommendationBreaks(User user);
		
		void saveUser(User user);
	}
	
	private final OverviewComponentListener listener;
	private final UserControlData userControlData;
	private final UserManagePopup sender;
	
	public OverviewComponent(UserManagePopup sender, OverviewComponentListener listener, UserControlData userControlData)
	{
		this.sender = Objects.requireNonNull(sender);
		this.listener = Objects.requireNonNull(listener);
		this.userControlData = Objects.requireNonNull(userControlData);
		setCompositionRoot(createLayout());
		setEnabled(!MemberStatus.DELETED.equals(userControlData.getUser().getMemberStatus()));
	}
	
	private Component createLayout()
	{
		final User user = userControlData.getUser();
		
		final AdminFormPanel layout = new AdminFormPanel();
		if(user.getBlockedStatus().equals(BlockedStatus.BLOCKED)){
			layout.addHeadline("BLOCKED");
		}
		else if(user.getBlockedStatus().equals(BlockedStatus.ADMIN_BLOCKED)){
			layout.addHeadline("BLOCKED BY ADMIN");
		}
		layout.addComponent(createQuickButtons(user));
		layout.addComponent(createMatchRequirements(user));
		layout.addComponent(createConfirmationSettings(user));
		
		if(SubscriptionState.PENDING.equals(userControlData.getCurrentSubscriptionState()))
			layout.addComponent(createMarketingSettings(user));
		
		layout.addComponent(createUserSettings(user));
		
		return layout;
	}
	
	private AdminFormPanel createMatchRequirements(User user)
	{
		final Function<Entry<Strictness, Long>, String> matchCountStringMapping = e -> I18N.USERMANAGEPOPUP_CAPTION_STATISTICMATCHCOUNTSTRICTNESS.msg(e.getKey().toString(), e.getValue().toString());
		
		final Map<RecommendationCategory, LocalDate> recommendationBreaks = listener.getRecommendationBreaks(user);
		final Map<RecommendationCategory, String> recommendationBreakStrings = new HashMap<>();
		final Map<RecommendationCategory, Button> recommendationBreakButtons = new HashMap<>();
		final Map<RecommendationCategory, String> matchCount = new HashMap<>();
		final Map<RecommendationCategory, String> allocatableMatchCount = new HashMap<>();
		final Map<RecommendationCategory, ToggleButton> activatedCategories = new HashMap<>();
		
		for(RecommendationCategory category : RecommendationCategory.values())
		{
			if(!recommendationBreaks.keySet().contains(category))
			{
				recommendationBreakStrings.put(category, I18N.USERMANAGEPOPUP_CAPTION_STATISTICRECOMMENDATIONBREAKSNO.msg());
				recommendationBreakButtons.put(category, new Button(I18N.USERMANAGEPOPUP_ACTION_ACTIVATERECOMMENDATIONBREAK.msg(), event -> listener.activateRecommendationBreak(sender, user, category)));
			}
			else
			{
				final LocalDate endDate = recommendationBreaks.get(category);
				final String text = endDate == null ? I18N.USERMANAGEPOPUP_CAPTION_STATISTICRECOMMENDATIONBREAKSWITHOUTEND.msg() : I18N.USERMANAGEPOPUP_CAPTION_STATISTICRECOMMENDATIONBREAKSTILLDATE.msg(endDate.toString());
				recommendationBreakStrings.put(category, text);
				recommendationBreakButtons.put(category, new Button(I18N.USERMANAGEPOPUP_ACTION_DEACTIVATERECOMMENDATIONBREAK.msg(), event -> listener.deactivateRecommendationBreak(sender, user, category)));
			}
			
			final String matchCountValue = userControlData.getMatchCount().get(category).entrySet().stream()
					.filter(e -> e.getValue() > 0)
					.sorted(Entry.comparingByKey())
					.map(matchCountStringMapping)
					.reduce((s1, s2) -> s1 + " - " + s2)
					.orElse(I18N.USERMANAGEPOPUP_CAPTION_STATISTICMATCHCOUNTNOTHING.msg());
			matchCount.put(category, matchCountValue);
			
			final String allocatableMatchCountValue = userControlData.getAllocatableMatchCount().get(category).entrySet().stream()
					.filter(e -> e.getValue() > 0)
					.sorted(Entry.comparingByKey())
					.map(matchCountStringMapping)
					.reduce((s1, s2) -> s1 + " - " + s2)
					.orElse(I18N.USERMANAGEPOPUP_CAPTION_STATISTICMATCHCOUNTNOTHING.msg());
			allocatableMatchCount.put(category, allocatableMatchCountValue);

			final ToggleButton categoryToggleButton = new ToggleButton();
			categoryToggleButton.setAutoRefresher(() -> user.getCategories().contains(category));
			categoryToggleButton.setCaption( I18N.USERMANAGEPOPUP_CAPTION_STATISTICACTIVATEDCATEGORIESACTIVATED.msg(), I18N.USERMANAGEPOPUP_CAPTION_STATISTICACTIVATEDCATEGORIESDEACTIVATED.msg());
			categoryToggleButton.addClickListener(event -> listener.activateCategory(user, category), event -> listener.deactivateCategory(user, category));
			activatedCategories.put(category, categoryToggleButton);
			if(user.getMemberStatus()==MemberStatus.REGISTRATION){
				categoryToggleButton.setEnabled(false);
			}
		}
		
		final Map<String, Map<RecommendationCategory, ?>> matchStatisticMatrix = new LinkedHashMap<>();
		matchStatisticMatrix.put(I18N.USERMANAGEPOPUP_CAPTION_STATISTICMISSINGREQUIREDANSWERSRATIO.msg(ALLOWED_MISSING_ANSWERS_RATIO), userControlData.getMissingRequiredAnswersRatio());
		matchStatisticMatrix.put(I18N.USERMANAGEPOPUP_CAPTION_STATISTICMATCHCOUNT.msg(), matchCount);
		matchStatisticMatrix.put(I18N.USERMANAGEPOPUP_CAPTION_STATISTICALLOCATABLEMATCHCOUNT.msg(), allocatableMatchCount);
		matchStatisticMatrix.put(I18N.USERMANAGEPOPUP_CAPTION_STATISTICACTIVATEDCATEGORIES.msg(), activatedCategories);
		matchStatisticMatrix.put(I18N.USERMANAGEPOPUP_CAPTION_STATISTICRECOMMENDATIONBREAKS.msg(), recommendationBreakStrings);
		matchStatisticMatrix.put("", recommendationBreakButtons);
		matchStatisticMatrix.put(I18N.USERMANAGEPOPUP_CAPTION_STATISTICINITIALSUGGESTION.msg(), userControlData.getSuggestionInitial());
		matchStatisticMatrix.put(I18N.USERMANAGEPOPUP_CAPTION_STATISTICRELATIONSHIPCOUNTSINCECHECKPERIOD.msg(CHECK_PERIOD.toDays()), userControlData.getRelationshipCountSinceCheckPeriod());
		
		final AdminFormPanel matchingPanel = new AdminFormPanel(I18N.USERMANAGEPOPUP_CAPTION_STATISTICMATCHTITLE.msg());
		matchingPanel.addLine(I18N.USERMANAGEPOPUP_CAPTION_STATISTICCURRENTSUBSCRIPTIONSTATE.msg(), userControlData.getCurrentSubscriptionState().toString());
		matchingPanel.addLine(I18N.USERMANAGEPOPUP_CAPTION_STATISTICDELETEDUSER.msg(), user.isDataDeleted() ? I18N.USERMANAGEPOPUP_CAPTION_STATISTICDELETEDUSERYES.msg() : I18N.USERMANAGEPOPUP_CAPTION_STATISTICDELETEDUSERNO.msg());
		matchingPanel.addLineBreak();
		matchingPanel.addMatrix(matchStatisticMatrix);
		return matchingPanel;
	}

	private AdminFormPanel createQuickButtons(User user)
	{
		final AdminFormPanel quickButtonPanel = new AdminFormPanel(I18N.USERMANAGEPOPUP_CAPTION_QUICKBUTTONTITLE.msg());
		final HorizontalLayout quickButtonLayout = new HorizontalLayout();
		quickButtonLayout.setSpacing(true);

		final HorizontalLayout hl = new HorizontalLayout();
		hl.setSpacing(true);

		Label label = new Label();

		final Button generateLoginButton = new Button(de.binaerebauten.gleichklang.adminweb.view.I18N.USERMANAGE_ACTION_CREATELOGIN.msg());
		generateLoginButton.addClickListener(event -> listener.generateLogin(user));

		final ToggleButton blacklistButton = new ToggleButton();
		blacklistButton.setAutoRefresher(() -> listener.isMailBlacklisted(user));
		blacklistButton.setCaption(I18N.USERMANAGEPOPUP_ACTION_REMOVEFROMBLACKLIST.msg(), I18N.USERMANAGEPOPUP_ACTION_ADDTOBLACKLIST.msg());
		blacklistButton.addClickListener(NO, event -> listener.removeFromBlacklist(user));
		blacklistButton.addClickListener(YES, event -> listener.addToBlacklist(user));

		final Button answers = new Button(de.binaerebauten.gleichklang.adminweb.view.I18N.ADMINMESSAGEVIEW_MESSAGE_ANSWER.msg());
		answers.addClickListener(event -> listener.writeMessage(user));

		final Button reload = new Button("Reload");
		final Button reminderButton = new Button("Reminder");
		final Button revocationButton = new Button("Widerruf");
		revocationButton.addClickListener(event -> listener.openRevocationPopup(user));

		reminderButton.addClickListener(event -> listener.openReminder(user));

		reload.addClickListener(clickEvent ->
		{
			listener.closePopup();
			listener.openUser(user);
		});

		quickButtonLayout.addComponent(generateLoginButton);
		quickButtonLayout.addComponent(blacklistButton);
		quickButtonLayout.addComponent(answers);
		quickButtonLayout.addComponent(reload);
		//quickButtonLayout.addComponent(reminderButton);
		//quickButtonLayout.addComponent(revocationButton);
		hl.addComponent(reminderButton);
		hl.addComponent(revocationButton);

		quickButtonPanel.addComponent(quickButtonLayout);
		quickButtonPanel.addComponent(label);
		quickButtonPanel.addComponent(hl);

		return quickButtonPanel;
	}
	
	private AdminFormPanel createConfirmationSettings(User user)
	{
		final SimpleToggleClickListener saveUserListener = value -> listener.saveUser(user);
		
		final ToggleButton confirmationButton = new ToggleButton(ToggleStyle.CHECKBOX);
		confirmationButton.addToggleClickListener(user::setEmailConfirmed);
		confirmationButton.addToggleClickListener(saveUserListener);
		confirmationButton.setAutoRefresher(user::isEmailConfirmed);
		
		final AdminFormPanel confirmationFormPanel = new AdminFormPanel(I18N.USERMANAGEPOPUP_CAPTION_CONFIRMATIONTITLE.msg());
		confirmationFormPanel.addValueList(Collections.singletonMap(I18N.USERMANAGEPOPUP_CAPTION_CONFIRMATION.msg(), confirmationButton));
		
		return confirmationFormPanel;
	}
	
	private Component createMarketingSettings(User user)
	{
		final SimpleToggleClickListener saveUserListener = value -> listener.saveUser(user);
		
		final ToggleButton marketingNotificationsButton = new ToggleButton(ToggleStyle.CHECKBOX);
		marketingNotificationsButton.addToggleClickListener(value -> user.getUserSettings().setEnableMarketingNotifications(value));
		marketingNotificationsButton.addToggleClickListener(saveUserListener);
		marketingNotificationsButton.setAutoRefresher(() -> user.getUserSettings().isEnableMarketingNotifications());
		
		final AdminFormPanel marketingsNotificationsFormPanel = new AdminFormPanel(I18N.USERMANAGEPOPUP_CAPTION_MARKETINGNOTIFICATIONSTITLE.msg());
		marketingsNotificationsFormPanel.addValueList(Collections.singletonMap(I18N.USERMANAGEPOPUP_CAPTION_ENABLEMARKETINGNOTIFICATIONS.msg(), marketingNotificationsButton));
		
		return marketingsNotificationsFormPanel;
	}
	
	private AdminFormPanel createUserSettings(User user)
	{
		final SimpleToggleClickListener saveUserListener = value -> listener.saveUser(user);
		
		final ToggleButton enableRecommendationNotifications = new ToggleButton(ToggleStyle.CHECKBOX);
		enableRecommendationNotifications.addToggleClickListener(value -> user.getUserSettings().setDisableRecommendationNotifications(!value));
		enableRecommendationNotifications.addToggleClickListener(saveUserListener);
		enableRecommendationNotifications.setAutoRefresher(() -> !user.getUserSettings().isDisableRecommendationNotifications());
		
		final ToggleButton enableCipherMessageNotifications = new ToggleButton(ToggleStyle.CHECKBOX);
		enableCipherMessageNotifications.addToggleClickListener(value -> user.getUserSettings().setDisableCipherMessageNotifications(!value));
		enableCipherMessageNotifications.addToggleClickListener(saveUserListener);
		enableCipherMessageNotifications.setAutoRefresher(() -> !user.getUserSettings().isDisableCipherMessageNotifications());
		
		final ToggleButton enablePositiveRankingNotifications = new ToggleButton(ToggleStyle.CHECKBOX);
		enablePositiveRankingNotifications.addToggleClickListener(value -> user.getUserSettings().setDisablePositiveRankingNotifications(!value));
		enablePositiveRankingNotifications.addToggleClickListener(saveUserListener);
		enablePositiveRankingNotifications.setAutoRefresher(() -> !user.getUserSettings().isDisablePositiveRankingNotifications());
		
		final ToggleButton enableFootprintNotifications = new ToggleButton(ToggleStyle.CHECKBOX);
		enableFootprintNotifications.addToggleClickListener(value -> user.getUserSettings().setDisableFootprintNotifications(!value));
		enableFootprintNotifications.addToggleClickListener(saveUserListener);
		enableFootprintNotifications.setAutoRefresher(() -> !user.getUserSettings().isDisableFootprintNotifications());
		
		final ToggleButton enableNewsNotifications = new ToggleButton(ToggleStyle.CHECKBOX);
		enableNewsNotifications.addToggleClickListener(value -> user.getUserSettings().setDisableNewsNotifications(!value));
		enableNewsNotifications.addToggleClickListener(saveUserListener);
		enableNewsNotifications.setAutoRefresher(() -> !user.getUserSettings().isDisableNewsNotifications());
		
		final Map<String, Component> userSettings = new LinkedHashMap<>();
		userSettings.put(I18N.USERMANAGEPOPUP_CAPTION_ENABLERECOMMENDATIONNOTIFICATIONS.msg(), enableRecommendationNotifications);
		userSettings.put(I18N.USERMANAGEPOPUP_CAPTION_ENABLECIPHERMESSAGENOTIFICATIONS.msg(), enableCipherMessageNotifications);
		userSettings.put(I18N.USERMANAGEPOPUP_CAPTION_ENABLEPOSITIVERANKINGNOTIFICATIONS.msg(), enablePositiveRankingNotifications);
		userSettings.put(I18N.USERMANAGEPOPUP_CAPTION_ENABLEFOOTPRINTNOTIFICATIONS.msg(), enableFootprintNotifications);
		userSettings.put(I18N.USERMANAGEPOPUP_CAPTION_ENABLENEWSNOTIFICATIONS.msg(), enableNewsNotifications);
		
		
		final AdminFormPanel userSettingsFormPanel = new AdminFormPanel(I18N.USERMANAGEPOPUP_CAPTION_USERSETTINGS.msg());
		userSettingsFormPanel.addValueList(userSettings);
		
		return userSettingsFormPanel;
	}
}
