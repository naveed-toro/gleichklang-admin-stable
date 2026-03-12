package de.binaerebauten.gleichklang.core.service;

import de.binaerebauten.gleichklang.core.model.media.Avatar;
import de.binaerebauten.gleichklang.core.model.media.FileEntity;
import de.binaerebauten.gleichklang.core.model.media.Media;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.user.Address;
import de.binaerebauten.gleichklang.core.model.user.MemberStatus;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.I18NRepository;
import de.binaerebauten.gleichklang.core.repository.PaymentRepository;
import de.binaerebauten.gleichklang.core.repository.user.AddressRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.service.file.AvatarService;
import de.binaerebauten.gleichklang.core.service.file.AvatarUploadFile;
import de.binaerebauten.gleichklang.core.service.file.FileService;
import de.binaerebauten.gleichklang.core.service.file.MediaService;
import de.binaerebauten.gleichklang.core.service.mail.MailSendService;
import de.binaerebauten.gleichklang.core.service.mail.UserMailTemplateService;
import de.binaerebauten.gleichklang.core.service.payment.ExternalPaymentService;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserDataServiceTest
{
	@InjectMocks
	private UserDataService userDataService;
	
	@Mock
	private FileService fileService;

	@Mock
	private AnswerService answerService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private AddressRepository addressRepository;
	
	@Mock
	private ExternalPaymentService externalPaymentService;
	
	@Mock
	private SubscriptionService subscriptionService;
	
	@Mock
	private PaymentRepository paymentRepository;
	
	@Mock
	private MailSendService mailSendService;
	
	@Mock
	private UserMailTemplateService userMailTemplateService;
	
	@Mock
	private MediaService mediaService;
	
	@Mock
	private AvatarService avatarService;

	@Mock
	private I18NRepository i18NRepository;

	@Test
	public void deleteUserDataTest() throws PaymentException
	{
		final User user = new User();
		final Avatar avatar = new Avatar();
		final Media media = new Media();
		final Subscription subscription = new Subscription();
		final List<Address> addresses = user.getAddresses();
		addresses.add(new Address());
		
		avatar.setFile(new FileEntity());
		media.setFile(new FileEntity());
		
		final AvatarUploadFile avatarUploadFile = new AvatarUploadFile(avatar, fileService);
		
		when(subscriptionService.findCurrentSubscription(user)).thenReturn(Optional.of(subscription));
		when(paymentRepository.findPendingPayments(user)).thenReturn(Collections.emptySet());
		when(avatarService.getAvatars(user)).thenReturn(Collections.singletonList(avatarUploadFile));

		userDataService.deleteUserData(user);

		verify(subscriptionService).cancelSubscription(eq(subscription));
		verify(userRepository).save(user);
		verify(userRepository).deleteUser(MemberStatus.DELETED, user);
		verify(fileService).deleteAvatarUploadFile(avatarUploadFile);
		verify(mediaService).deleteAllMedias(user);
		
		assertThat(user.getAddresses().size(), equalTo(0));
	}
}
