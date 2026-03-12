package de.binaerebauten.gleichklang.adminweb.presenter;

import com.vaadin.server.Sizeable;
import de.binaerebauten.gleichklang.adminweb.navigation.DefaultNavigatorFactory.AdminMenuItem;
import de.binaerebauten.gleichklang.adminweb.presenter.handler.UserControlHandler;
import de.binaerebauten.gleichklang.adminweb.service.AdminUserLoginService;
import de.binaerebauten.gleichklang.adminweb.view.UserManageView;
import de.binaerebauten.gleichklang.adminweb.view.UserManageView.UserManageTab;
import de.binaerebauten.gleichklang.adminweb.view.UserManageView.UserManageViewListener;
import de.binaerebauten.gleichklang.adminweb.view.popup.AdminReminderPopup;
import de.binaerebauten.gleichklang.adminweb.view.popup.usermanage.UserDataExportPopup;
import de.binaerebauten.gleichklang.core.model.filter.TemplateContext;
import de.binaerebauten.gleichklang.core.model.filter.UserFilter.UserFilterType;
import de.binaerebauten.gleichklang.core.model.mail.Newsletter;
import de.binaerebauten.gleichklang.core.model.mail.UndeliverableMail;
import de.binaerebauten.gleichklang.core.model.mail.UndeliverableMailReason;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.payment.SubscriptionWithState;
import de.binaerebauten.gleichklang.core.model.user.*;
import de.binaerebauten.gleichklang.core.presenter.SubNavigatePresenter;
import de.binaerebauten.gleichklang.core.presenter.filter.DefaultFilterControlHandler;
import de.binaerebauten.gleichklang.core.repository.user.CompleteUserRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserPinRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.security.AuthenticationService;
import de.binaerebauten.gleichklang.core.service.*;
import de.binaerebauten.gleichklang.core.service.file.AbstractUpload.UploadResult;
import de.binaerebauten.gleichklang.core.service.file.AudioService;
import de.binaerebauten.gleichklang.core.service.file.InMemoryUpload;
import de.binaerebauten.gleichklang.core.service.file.MessageUploadFile;
import de.binaerebauten.gleichklang.core.service.mail.NewsletterService;
import de.binaerebauten.gleichklang.core.service.mail.UndeliverableMailService;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.filter.FilterSpecificationBuilder;
import de.binaerebauten.gleichklang.core.view.component.FilterControlComponent.FilterControlFeature;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanTable;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import de.binaerebauten.gleichklang.core.view.component.MessageBox.DialogResult;
import de.binaerebauten.gleichklang.core.view.component.MessageBox.MessageBoxButtons;
import de.binaerebauten.gleichklang.core.view.component.message.NewMessagePopup;
import de.binaerebauten.gleichklang.core.view.popup.GenericPopup;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.domain.Specification;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class UserManagePresenter extends SubNavigatePresenter<UserManageTab> implements UserManageViewListener
{
    private static final Logger LOG = LoggerFactory.getLogger(UserManagePresenter.class);

    private final DefaultFilterControlHandler userFilterControlHandler;

    private final DefaultFilterControlHandler blockedUserFilterControlHandler;
    private final DefaultFilterControlHandler undeliverableMailFilterControlHandler;
    private final FilterSpecificationBuilder filterSpecificationBuilder;

    private final UserRepository userRepository;
    private final UserPinRepository userPinRepository;

    private final ReminderService reminderService;
    private final UserControlHandler userControlHandler;
    private final MessageService messageService;
    private final ClientInformationService clientInformationService;

    private final UserService userService;
    private final AudioService audioService;
    private final AdminUserLoginService adminUserLoginService;
    private final UndeliverableMailService undeliverableMailService;
    private final NewsletterService newsletterService;
    private final SubscriptionService subscriptionService;

    private final UserManageView view;
    private final Admin currentAdmin;
    private final User currentUser;
    private UserDataExportPopup userDataExportPopup = null;

    public UserManagePresenter(ApplicationContext ctx, UserManageView view)
    {
        super(view, UserManageTab.class, AdminMenuItem.USER_CONTROL);

        this.view = view;

        userService = ctx.getBean(UserService.class);
        userRepository = ctx.getBean(UserRepository.class);
        userPinRepository = ctx.getBean(UserPinRepository.class);
        audioService = ctx.getBean(AudioService.class);
        reminderService = ctx.getBean(ReminderService.class);
        adminUserLoginService = ctx.getBean(AdminUserLoginService.class);
        undeliverableMailService = ctx.getBean(UndeliverableMailService.class);
        newsletterService = ctx.getBean(NewsletterService.class);
        subscriptionService = ctx.getBean(SubscriptionService.class);
        clientInformationService = ctx.getBean(ClientInformationService.class);

        filterSpecificationBuilder = ctx.getBean(FilterSpecificationBuilder.class);
        userFilterControlHandler = new DefaultFilterControlHandler(ctx.getBean(FilterControlService.class));
        userFilterControlHandler.setTemplateContext(TemplateContext.USER_MANAGE);
        userFilterControlHandler.removeFilterControlFeatures(FilterControlFeature.PREVIEW);
        userFilterControlHandler.setPinnedUserFilterTypes(UserFilterType.MAIL_FILTER, UserFilterType.ALIAS_FILTER,UserFilterType.FIRST_NAME_FILTER, UserFilterType.LAST_NAME_FILTER, UserFilterType.EXTERNAL_REFERENCE_ID_FILTER);


        blockedUserFilterControlHandler= new DefaultFilterControlHandler(ctx.getBean(FilterControlService.class));
        blockedUserFilterControlHandler.setTemplateContext(TemplateContext.USER_MANAGE);
        blockedUserFilterControlHandler.removeFilterControlFeatures(FilterControlFeature.PREVIEW);
        blockedUserFilterControlHandler.setPinnedUserFilterTypes(UserFilterType.MAIL_FILTER, UserFilterType.ALIAS_FILTER,UserFilterType.FIRST_NAME_FILTER, UserFilterType.LAST_NAME_FILTER, UserFilterType.EXTERNAL_REFERENCE_ID_FILTER);
        userControlHandler = new UserControlHandler(ctx, this);

        undeliverableMailFilterControlHandler = new DefaultFilterControlHandler(ctx.getBean(FilterControlService.class));



        undeliverableMailFilterControlHandler.setUserFilterTypes(UserFilterType.MAIL_FILTER, UserFilterType.ALIAS_FILTER, UserFilterType.LAST_NAME_FILTER, UserFilterType.FIRST_NAME_FILTER);
        undeliverableMailFilterControlHandler.removeFilterControlFeatures(FilterControlFeature.PREVIEW);

        currentAdmin = ctx.getBean(AdminService.class).getCurrentUser();
        final UserControlHandler userControlHandler = new UserControlHandler(ctx, this);
        userControlHandler.addValueChangedListener(this::refresh);
        view.setUserQuickBarListener(userControlHandler);
        messageService = ctx.getBean(MessageService.class);
        currentUser = ctx.getBean(CompleteUserRepository.class).findById(ctx.getBean(AuthenticationService.class).getAuthenticatedUserId());
        view.setListener(this);
    }

    @Override
    public void enter(UserManageTab navigationEnum, String parameters)
    {
        switch (navigationEnum)
        {
            case USER_CONTROL:
                this.view.setUserFilterHandlerAndBuilder(userFilterControlHandler, filterSpecificationBuilder);
                this.view.setUserHandler(userRepository::findAll);
                break;
            case BLACKLIST:
                this.view.setUndeliverableMailFilterHandlerAndBuilder(undeliverableMailFilterControlHandler, filterSpecificationBuilder);
                this.view.setUndeliverableMailHandler(undeliverableMailService.createUndeliverableMailHandler());
                break;
            case NEWSLETTER:
                this.view.setNewsletterHandler(newsletterService.createNewsletterHandler());
                break;
            case BLOCKED:
                this.view.setBlockedUserFilterHandlerAndBuilder(blockedUserFilterControlHandler, filterSpecificationBuilder);
                this.view.setBlockedUserHandler(userService.createBlockedUserHandler(null));
                break;
        }
    }

    public LazyBeanItemContainer.LazyBeanFilteredItemsHandler<User> createUnBlockedUserHandler()
    {
        final Specification<User> specs = (root, query, cb) -> cb.equal(root.get(User_.isBlocked), false);
        return (specification, pageable) -> userRepository.findAll(specs, pageable);
    }

    public LazyBeanItemContainer.LazyBeanFilteredItemsHandler<User> createBlockedUserHandler()
    {

        final Specification<User> specs = (root, query, cb) -> cb.equal(root.get(User_.isBlocked), true);
        return (specification, pageable) -> userRepository.findAll(specs, pageable);

    }

    @Override
    public void leave()
    {
        this.view.setUserFilterHandlerAndBuilder(null, null);
        this.view.setUndeliverableMailFilterHandlerAndBuilder(null, null);
        this.view.setUserHandler(null);
        this.view.setUndeliverableMailHandler(null);

        super.leave();
    }

    @Override
    public boolean isOnline(User user)
    {
        return userService.isOnline(user);
    }

    @Override
    public boolean isBlacklisted(User user)
    {
        return undeliverableMailService.isBlocked(user.getEmail());
    }

    @Override
    public boolean isBlocked(User user)
    {
        return userRepository.findById(user.getId()).isBlocked();
    }

    @Override
    public void onBlackListUpload(InMemoryUpload uploadFile, UploadResult uploadResult, CsvSeparator csvSeparator)
    {
        switch (uploadResult)
        {
            case SUCCESS:
                try
                {
                    final CSVFormat csvFormat = CSVFormat.DEFAULT.withDelimiter(csvSeparator.getSeparator());
                    final CSVParser csvParser = new CSVParser(uploadFile.createStreamReader(), csvFormat);
                    final Collection<String> mails = StreamSupport.stream(csvParser.spliterator(), false).map(record -> record.get(0)).collect(Collectors.toSet());
                    final long addedEmails = undeliverableMailService.addToBlacklist(mails, UndeliverableMailReason.MAILER_LITE);
                    refresh();
                    MessageBox.show(addedEmails + " Einträge zur Blacklist hinzugefügt");
                }
                catch (IOException e)
                {
                    MessageBox.show("Falsches Format: " + e.getLocalizedMessage());
                }
                break;
            case FAILED:
            case VIRUS:
                MessageBox.show("Fehler beim Hochladen");
                break;
        }
    }

    @Override
    public void resetLogins()
    {
        adminUserLoginService.reset(currentAdmin);
    }

	@Override
	public byte[] export(Specification<User> specification)
	{
		List<User> users = userRepository.findAll(specification);
		LOG.info(String.format("Exporting %d users ...", users.size()));

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(byteArrayOutputStream);
		try
		{
			CSVPrinter csvPrinter = CSVFormat.DEFAULT
					.withDelimiter(';')
					.withHeader("email", "lastname", "firstname", "alias", "subscription_state")
					.print(outputStreamWriter);
			for (User user : users)
			{
				csvPrinter.printRecord(user.getEmail(),
						user.getLastName(), user.getFirstName(),
						user.getAlias(), user.getMemberStatus());
			}
		}
		catch (IOException e)
		{
			LOG.error(e.getMessage());
		}
		finally
		{
			try
			{
				outputStreamWriter.close();
				byteArrayOutputStream.close();
			}
			catch (IOException ignored) {}
		}

		return byteArrayOutputStream.toByteArray();
	}

    @Override
    public void removeFromBlacklist(UndeliverableMail undeliverableMail)
    {
        MessageBox.show(I18N.USERMANAGEPRESENTER_CAPTION_REMOVEFROMBLACKLIST.msg(), MessageBoxButtons.YES_NO, dialogResult ->
        {
            if(DialogResult.YES.equals(dialogResult))
            {
                undeliverableMailService.remove(undeliverableMail);
                refresh();
            }
        });
    }

    @Override
    public void removeBlacklist(User user) {

        if(Objects.isNull(user.getEmail()))
        {
            MessageBox.show(I18N.USERMANAGEPRESENTER_VALIDATION_NOTPOSSIBLE.msg());
            return;
        }

        MessageBox.show(I18N.USERMANAGEPRESENTER_CAPTION_REMOVEFROMBLACKLIST.msg(user.getEmail()), MessageBoxButtons.YES_NO, dialogResult ->
        {
            if(DialogResult.YES.equals(dialogResult)) {
                try {

                    undeliverableMailService.deleteFromBlackList(user);
                    refresh();

                } catch (ValidationException e) {

                    MessageBox.show(I18N.USERMANAGEPRESENTER_VALIDATION_ALREADYONBLACKLIST.msg());
                }
            }
        });
    }

    @Override
    public void addToBlacklist(User user)
    {
        if(Objects.isNull(user.getEmail()))
        {
            MessageBox.show(I18N.USERMANAGEPRESENTER_VALIDATION_NOTPOSSIBLE.msg());
            return;
        }

        MessageBox.show(I18N.USERMANAGEPRESENTER_CAPTION_ADDTOBLACKLIST.msg(user.getEmail()), MessageBoxButtons.YES_NO, dialogResult ->
        {
            if(DialogResult.YES.equals(dialogResult))
            {
                try
                {
                    undeliverableMailService.addUserToBlacklist(user);
                }
                catch (ValidationException e)
                {
                    MessageBox.show(I18N.USERMANAGEPRESENTER_VALIDATION_ALREADYONBLACKLIST.msg());
                }
            }
        });
    }


    @Override
    public void addToUnblockList(User user) {

        MessageBox.show("Are you sure want to unblock User "+(user.getEmail()), MessageBoxButtons.YES_NO, dialogResult ->
        {
            if(DialogResult.YES.equals(dialogResult))
            {
                try
                {
                    userService.addUserToUnblockList(user);
                    refreshBlockedUsers();
                }
                catch (ValidationException e)
                {
                    MessageBox.show(I18N.USERMANAGEPRESENTER_VALIDATION_ALREADYONBLACKLIST.msg());
                }
            }
        });

    }

    public void addToBlockList(User user) {

        if (!user.isBlocked()) {

            MessageBox.show(I18N.USERMANAGEPRESENTER_VALIDATION_ALREADYONBLOCK.msg(user.getEmail()), MessageBoxButtons.YES_NO, dialogResult ->
            {
                if (DialogResult.YES.equals(dialogResult)) {
                    try {
                        userService.addUserToBlockList(user);
                        refresh();
                    } catch (ValidationException e) {
                        MessageBox.show(I18N.USERMANAGEPRESENTER_VALIDATION_ALREADYONBLOCK.msg());
                    }
                }
            });
        }
        else{
            MessageBox.show(I18N.USERMANAGEPRESENTER_VALIDATION_ALREADYONBLOCKS.msg());
        }
    }

    public void refreshBlockedUsers() {
        this.view.refreshBlockedUsers();
    }


    @Override
    public void onNewsletterUpload(InMemoryUpload uploadFile, UploadResult uploadResult)
    {
        switch (uploadResult)
        {
            case SUCCESS:
                try
                {
                    final CSVFormat csvFormat = CSVFormat.DEFAULT;
                    final CSVParser csvParser = new CSVParser(uploadFile.createStreamReader(), csvFormat);
                    final Collection<String> mails = StreamSupport.stream(csvParser.spliterator(), false).map(record -> record.get(0)).collect(Collectors.toSet());
                    final long removedNewsletters = newsletterService.removeFromNewsletter(mails);
                    final long disabledNotifications = userService.disableUserNewsNotifications(mails);
                    refresh();
                    MessageBox.show(removedNewsletters + " Einträge aus Newsletter entfernt\n" + disabledNotifications + " Notifications ausgeschaltet");
                }
                catch (IOException e)
                {
                    MessageBox.show("Falsches Format: " + e.getLocalizedMessage());
                }
                break;
            case FAILED:
            case VIRUS:
                MessageBox.show("Fehler beim Hochladen");
                break;
        }
    }

    @Override
    public void deleteFromNewsletter(Newsletter newsletter)
    {
        newsletterService.delete(newsletter);
        refresh();
    }

    @Override
    public InputStream exportNewsletter()
    {
        return newsletterService.export();
    }

    @Override
    public void ShowUserProfile(User user) {

        userControlHandler.openUser(user);

    }
    @Override
    public void ShowBlackListedUserProfile(UndeliverableMail undeliverableMail){
        userControlHandler.openUser(undeliverableMail.getId());
    }

    @Override
    public void adminBlocked(User user) {
        if (!user.getBlockedStatus().equals(BlockedStatus.ADMIN_BLOCKED)) {

            if (user.getBlockedStatus().equals(BlockedStatus.BLOCKED)) {
                MessageBox.show("Are you sure want to permanently block the  User " + (user.getEmail()), MessageBoxButtons.YES_NO, dialogResult ->
                {
                    if (DialogResult.YES.equals(dialogResult)) {
                        userService.adminBlocked(user);
                    }

                });
            } else {
                MessageBox.show("Only Blocked members can be confirmed Block", MessageBox.MessageBoxButtons.OK, MessageBox.MessageBoxStyle.ATTENTION, null);
            }
        }
        else {
            MessageBox.show("User already blocked by Admin", MessageBox.MessageBoxButtons.OK, MessageBox.MessageBoxStyle.ATTENTION, null);

        }
    }

    public void writeMessage(User targetUser)
    {
        //final NewMessagePopup popup = new NewMessagePopup(messageService.createFirstMessageByAdmin(targetUser , currentAdmin));

        final NewMessagePopup popup = new NewMessagePopup(messageService.createNewMessageByAdmin(currentUser , targetUser, currentAdmin));
        popup.setNewUploadCallback(this::newUpload);
        popup.setSendCallback(this::sendMessage);
        popup.setSaveCallback(this::saveMessage);
        popup.setReceiver(targetUser);

        popup.setBoxSize(GenericPopup.BoxSize.WIDE);
        popup.setHeight(80, Sizeable.Unit.PERCENTAGE);
        tryOpenPopup(popup);
    }

    @Override
    public void openReminderPopup(User item) {

        AdminReminderPopup reminderPopup = new AdminReminderPopup(reminderService, AdminReminderPopup.Operation.ADD, null,  currentAdmin, userControlHandler);
        reminderPopup.userNameOrAlias.setValue(item.getAlias());
        reminderPopup.show();
    }

    @Override
    public void showPin(User user) {
        UserPin userPin = userPinRepository.findByUserId(user.getId());
        if (Objects.nonNull(userPin) && Objects.nonNull(userPin.getPin()) &&
                (Objects.nonNull(userPin.getChangeDate()) && userPin.getChangeDate().toLocalDate().isEqual(LocalDate.now())
                        || (Objects.nonNull(userPin.getCreateDate()) && userPin.getCreateDate().toLocalDate().isEqual(LocalDate.now())))) {
            MessageBox.show("Die PIN des Mitglieds " + user.getAlias() + " ist " + userPin.getPin());
        } else {
            MessageBox.show("PIN heute noch nicht erstellt");
        }
    }


    private void sendMessage(Message message, List<MessageUploadFile> messageUploadFiles) throws ValidationException
    {
        messageService.sendMessageFromAdmin(message, messageUploadFiles, currentAdmin);
    }

    private void saveMessage(Message message, List<MessageUploadFile> messageUploadFiles) throws ValidationException
    {
        messageService.saveAdminMessage(message, messageUploadFiles, currentAdmin);
    }

    private MessageUploadFile newUpload(Message message, Collection<MessageUploadFile> messageUploadFiles) throws ValidationException
    {
        return messageService.createMessageAttachment(message, messageUploadFiles.size());
    }

    @Override
    public boolean audioForFriendship(User user) {
        return audioService.isAudioPresentForFriendship(user);
    }

    @Override
    public boolean audioForPartnership(User user) {
        return audioService.isAudioPresentForPartnership(user);
    }

    @Override
    public SubscriptionWithState getCurrentSubscription(User user) {
        SubscriptionWithState subscriptionWithState =
                subscriptionService.getCurrentSubscriptionWithState(user, null);
        return  subscriptionWithState;
    }

    @Override
    public String lastLoginDate(User user) {
        return clientInformationService.getLastLoginDate(user);
    }


}

