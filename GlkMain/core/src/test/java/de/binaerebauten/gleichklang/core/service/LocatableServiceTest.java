package de.binaerebauten.gleichklang.core.service;

import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.user.Address;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.LocatableRepository;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.DefaultStaticEntityFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LocatableServiceTest
{
	@InjectMocks
	private LocatableService locatableService;
	
	@Mock
	private MessageService messageService;
	
	@Mock
	private LocatableRepository locatableRepository;
	
	@Test
	public void notifyMissingZipsTest() throws ValidationException
	{
		final User user = DefaultStaticEntityFactory.createDefaultUser("test", "test");
		
		locatableService.notifyMissingZips(user);
		Mockito.verifyZeroInteractions(messageService);
		
		final Address address = DefaultStaticEntityFactory.createDefaultAddress(DefaultStaticEntityFactory.createDefaultCountry(DefaultStaticEntityFactory.createDefaultContinent()));
		address.setTmpZip("missingZip");
		user.getAddresses().add(address);
		
		final Message message = new Message();
		Mockito.when(messageService.createNewMessage(user)).thenReturn(message);
		
		locatableService.notifyMissingZips(user);
		Mockito.verify(messageService).sendMessageToAdmin(message, null);
	}
}
