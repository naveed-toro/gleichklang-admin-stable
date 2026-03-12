package de.binaerebauten.gleichklang.core.service;

import de.binaerebauten.gleichklang.core.model.user.*;
import de.binaerebauten.gleichklang.core.repository.user.UserRegistrationStateRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.service.payment.ExternalPaymentService;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest_V2
{
	@InjectMocks
	private UserService userService;
	
	@Mock
	private UserRepository userRepository;
	
	@Mock
	private ExternalPaymentService externalPaymentService;
	
	@Mock
	private UserRegistrationStateRepository userRegistrationStateRepository;
	
	@Test
	public void disableUserNewsNotificationsTest()
	{
		final UserSettings userSettings = new UserSettings();
		userSettings.setDisableNewsNotifications(false);
		
		final User user = new User();
		user.setUserSettings(userSettings);
		
		final List<User> users = new ArrayList<>();
		users.add(user);
		
		when(userRepository.findUsersWithUserNewsActivated(anyCollectionOf(String.class), eq(MemberStatus.CANCELED))).thenReturn(users);
		
		final List<String> emails = new ArrayList<>();
		
		final int count = 1500;
		
		for (int i = 0; i < count; i++)
		{
			emails.add(i + "test@example.com");
		}
		
		final long result = userService.disableUserNewsNotifications(emails);
		
		assertThat(result, equalTo(2L));
		assertThat(user.getUserSettings().isDisableNewsNotifications(), equalTo(true));
		
		verify(userRepository, times(2)).findUsersWithUserNewsActivated(anyCollectionOf(String.class), eq(MemberStatus.CANCELED));
	}
	
	@Test
	public void reuseUserTest() throws PaymentException
	{
		final User user = new User();
		user.setMemberStatus(MemberStatus.CANCELED);
		
		when(externalPaymentService.usesExternalPayment(any(User.class))).thenReturn(true);
		
		userService.reuseUser(user);
		
		ArgumentCaptor<UserRegistrationState> userRegistrationState = ArgumentCaptor.forClass(UserRegistrationState.class);
		
		verify(externalPaymentService).deregister(user);
		verify(userRepository).save(user);
		verify(userRegistrationStateRepository).save(userRegistrationState.capture());
		
		assertThat(user.getMemberStatus(), equalTo(MemberStatus.REGISTRATION));
		assertThat(userRegistrationState.getValue().getRegistrationState(), equalTo(RegistrationState.PAYMENT));
	}
}
