package de.binaerebauten.gleichklang.core.service;

import com.vaadin.server.VaadinSession;
import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;
import de.binaerebauten.gleichklang.core.model.payment.AbstractPayment;
import de.binaerebauten.gleichklang.core.model.payment.PaymentState;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.questionnaire.Answer;
import de.binaerebauten.gleichklang.core.model.user.*;
import de.binaerebauten.gleichklang.core.repository.I18NRepository;
import de.binaerebauten.gleichklang.core.repository.PaymentRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.service.file.AvatarService;
import de.binaerebauten.gleichklang.core.service.file.AvatarUploadFile;
import de.binaerebauten.gleichklang.core.service.file.MediaService;
import de.binaerebauten.gleichklang.core.service.mail.MailSendService;
import de.binaerebauten.gleichklang.core.service.mail.UserMailTemplateService;
import de.binaerebauten.gleichklang.core.service.payment.ExternalPaymentService;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;
import de.binaerebauten.gleichklang.core.utils.UserProfileUtil;
import de.binaerebauten.gleichklang.core.utils.UserProfileUtil.UserInfo;
import de.binaerebauten.gleichklang.core.view.component.UserProfile.UserProfileData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;
import java.util.function.BiFunction;

@Service
public class UserDataService {
    private final AnswerService answerService;
    private final UserRepository userRepository;
    private final ExternalPaymentService externalPaymentService;
    private final SubscriptionService subscriptionService;
    private final PaymentRepository paymentRepository;
    private final MailSendService mailSendService;
    private final UserMailTemplateService userMailTemplateService;
    private final AvatarService avatarService;
    private final MediaService mediaService;
    private static final String AUTHENTICATED_USER_TYPE_ATTRIBUTE = "AUTHENTICATED_USER_TYPE";
    private final I18NRepository i18NRepository;

    private static final Logger LOG = LoggerFactory.getLogger(UserDataService.class);

    @Autowired
    public UserDataService(AnswerService answerService,
                           UserRepository userRepository,
                           ExternalPaymentService externalPaymentService,
                           SubscriptionService subscriptionService,
                           PaymentRepository paymentRepository,
                           MailSendService mailSendService,
                           UserMailTemplateService userMailTemplateService,
                           AvatarService avatarService,
                           MediaService mediaService,
                           I18NRepository i18NRepository) {
        this.answerService = Objects.requireNonNull(answerService);
        this.userRepository = Objects.requireNonNull(userRepository);
        this.externalPaymentService = Objects.requireNonNull(externalPaymentService);
        this.subscriptionService = Objects.requireNonNull(subscriptionService);
        this.paymentRepository = Objects.requireNonNull(paymentRepository);
        this.mailSendService = Objects.requireNonNull(mailSendService);
        this.userMailTemplateService = Objects.requireNonNull(userMailTemplateService);
        this.avatarService = Objects.requireNonNull(avatarService);
        this.mediaService = Objects.requireNonNull(mediaService);
        this.i18NRepository = Objects.requireNonNull(i18NRepository);
    }

    /**
     * @param arg1
     * @param valueGetter
     * @param <T>
     * @param <A>
     * @return
     */
    public static <T, A> EnumMap<RecommendationCategory, T> createEnumMap(A arg1, BiFunction<A, RecommendationCategory, T> valueGetter) {
        return createEnumMap(arg1, valueGetter, RecommendationCategory.class);
    }

    /**
     * @param arg1
     * @param valueGetter
     * @param <T>
     * @param <A>
     * @return
     */
    public static <T, E extends Enum<E>, A> EnumMap<E, T> createEnumMap(A arg1, BiFunction<A, E, T> valueGetter, Class<E> enumClass) {
        final EnumMap<E, T> result = new EnumMap<>(enumClass);
        for (E enumValue : enumClass.getEnumConstants()) {
            result.put(enumValue, valueGetter.apply(arg1, enumValue));
        }
        return result;
    }

    private File getAvatarImage(User user, RecommendationCategory category) {
        final AvatarUploadFile avatar = avatarService.getAvatar(user, category);
        return avatar != null ? avatar.toFile() : null;
    }

    public File getThumbnailAvatarImage(User user, RecommendationCategory category) {
        final AvatarUploadFile avatar = avatarService.getAvatar(user, category);
        return avatar != null ? avatar.toThumbnailFile() : null;
    }

    public UserProfileData createUserProfileData(User user, RecommendationCategory recommendationCategory) {
        final LinkedHashMap<UserInfo, List<Answer>> userAnswers = new LinkedHashMap<>();
        for (UserInfo userInfo : UserInfo.values()) {
            final List<Answer> answers = UserProfileUtil.getNaturalKeys(userInfo, Collections.singleton(recommendationCategory), user, answerService::getAnswersVisibleToOtherUsers);
            if (answers != null && !answers.isEmpty()) {
                userAnswers.put(userInfo, answers);
            }
        }

        return new UserProfileData(
                user,
                answerService.getFreeText(user, recommendationCategory),
                getAvatarImage(user, recommendationCategory),
                recommendationCategory,
                userAnswers);
    }


    @Transactional
    public void deleteUserData(User user) {
        List<Object> satisfactionWithHarmony = new ArrayList<>();
        String successOfMediation = null;
        if (i18NRepository != null && user.getId() != null &&
                i18NRepository.satisfactionWithHarmony(user.getId()) != null
                && i18NRepository.satisfactionWithHarmony(user.getId()).size()>0) {
            satisfactionWithHarmony = i18NRepository.satisfactionWithHarmony(user.getId());
            successOfMediation = i18NRepository.successOfMediation(user.getId());
        }

        Set<CancelReason> cancelReason = user.getCancelReasons();
        if (user.isCanceled()) {
            if (i18NRepository != null && user.getId() != null &&
                            i18NRepository.satisfactionWithHarmony(user.getId()) != null
                            && !i18NRepository.satisfactionWithHarmony(user.getId()).isEmpty() && (
                    satisfactionWithHarmony!=null && satisfactionWithHarmony.size()>0 ?(satisfactionWithHarmony.contains("Zufrieden")|| satisfactionWithHarmony.contains("Satisfied")):false ||
                                    cancelReason.contains(CancelReason.SUCCESS_THROW_GK) ||
                                    successOfMediation!=null ?successOfMediation.equalsIgnoreCase("Ich habe partnerschaft gefunden.")||
                                    successOfMediation.equalsIgnoreCase("ich habe freundschaft gefunden.")||
                                    successOfMediation.equalsIgnoreCase("ich habe partnerschaft und freundschaft gefunden."):false) && !cancelReason.contains(CancelReason.UNHAPPY_WITH_SERVICE)) {
                mailSendService.sendEmail(user, userMailTemplateService.createMailTemplateInstance(UserMailTemplate.USER_DELETED_WITH_SATISFACTION, user));
            } else {
                mailSendService.sendEmail(user, userMailTemplateService.createMailTemplateInstance(UserMailTemplate.USER_DELETED, user));
            }
        } else {
            if (
                    i18NRepository != null && user.getId() != null &&
                            i18NRepository.satisfactionWithHarmony(user.getId()) != null
                            && !i18NRepository.satisfactionWithHarmony(user.getId()).isEmpty() && (

                            satisfactionWithHarmony !=null ?(satisfactionWithHarmony.contains("Zufrieden")|| satisfactionWithHarmony.contains("Satisfied")):false ||
                                    cancelReason.contains(CancelReason.SUCCESS_THROW_GK) ||
                                    successOfMediation!=null ?successOfMediation.equalsIgnoreCase("Ich habe partnerschaft gefunden.")||
                                    successOfMediation.equalsIgnoreCase("ich habe freundschaft gefunden.")||
                                    successOfMediation.equalsIgnoreCase("ich habe partnerschaft und freundschaft gefunden."):false) && !cancelReason.contains(CancelReason.UNHAPPY_WITH_SERVICE)) {
                mailSendService.sendEmail(user, userMailTemplateService.createMailTemplateInstance(UserMailTemplate.SUBSCRIPTION_CANCELLED_USER_DELETED_WITH_SATISFACTION, user));
            } else {
                mailSendService.sendEmail(user, userMailTemplateService.createMailTemplateInstance(UserMailTemplate.SUBSCRIPTION_CANCELLED_USER_DELETED, user));
            }
        }

        // Deactivate user's subscription and/or deregister him from an external payment system
        Optional<Subscription> subscription = subscriptionService.findCurrentSubscription(user);
        try {
            subscription.ifPresent(subscriptionService::cancelSubscription);

            if (externalPaymentService.usesExternalPayment(user)) {
                externalPaymentService.deregister(user);
            }
        } catch (PaymentException e) {
            throw new RuntimeException("User deregistration failed for: " + user.getEmail());
        }

        // cancel all pending payments
        final Set<AbstractPayment> pendingPayments = paymentRepository.findPendingPayments(user);
        pendingPayments.forEach(p -> p.setState(PaymentState.CANCELED));
        paymentRepository.save(pendingPayments);

        // Delete all other stuff
        user.setAddresses(new ArrayList<>());
        userRepository.save(user);
        try {
            if (getAuthenticatedUserType().equals(SignableUser.UserType.ADMIN))
                userRepository.deleteUser(MemberStatus.ADMIN_DELETED, user);
            else userRepository.deleteUser(MemberStatus.DELETED, user);

        } catch (NullPointerException e) {
            userRepository.deleteUser(MemberStatus.DELETED, user);
        }

        avatarService.getAvatars(user).forEach(AvatarUploadFile::delete);
        //answerService.deleteAnswerByUser(user);
        mediaService.deleteAllMedias(user);
    }

    /**
     * Returns the authenticated user UserType.
     *
     * @return the authenticated user User type in the current session or empty
     */

    public SignableUser.UserType getAuthenticatedUserType() {
        Object authenticatedUserType = VaadinSession.getCurrent().getAttribute(AUTHENTICATED_USER_TYPE_ATTRIBUTE);
        LOG.info(VaadinSession.getCurrent().getAttribute(AUTHENTICATED_USER_TYPE_ATTRIBUTE).toString());
        return authenticatedUserType != null ? (SignableUser.UserType) authenticatedUserType : null;
    }
}
