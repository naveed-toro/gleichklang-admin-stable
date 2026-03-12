package de.binaerebauten.gleichklang.core.service;

import com.vaadin.server.Page;
import com.vaadin.server.WebBrowser;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Browser;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.OS;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation_;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.user.ClientInformationRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.security.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;

@Service
public class ClientInformationService
{
	@Autowired
	private ClientInformationRepository clientInformationRepository;
	
	@Autowired
	private AuthenticationService authenticationService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Transactional
	public void updateClientInformation(User currentUser, Device device)
	{
		Objects.requireNonNull(currentUser);
		
		final ClientInformation currentClientInformation = createClientInformation(currentUser, device);
		final ClientInformation existingClientInformation = clientInformationRepository.findByKey
				(
						currentClientInformation.getUser(),
						currentClientInformation.getBrowser(),
						currentClientInformation.getBrowserMajorVersion(),
						currentClientInformation.getBrowserMinorVersion(),
						currentClientInformation.getOs()
				);
		Pageable pageable = new PageRequest(0, 1);
		List<ClientInformation> cl =  clientInformationRepository.findByIdAndLaseSeen(currentUser, pageable);
		if(cl!=null && !cl.isEmpty()) {
			cl.get(0).setLastSeen(false);
			clientInformationRepository.save(cl);
		}
		if (existingClientInformation != null)
		{
			currentClientInformation.setId(existingClientInformation.getId());
			currentClientInformation.setCreateDate(existingClientInformation.getCreateDate());

		}
		currentClientInformation.setLastSeen(true);
		clientInformationRepository.save(currentClientInformation);
	}

	@Transactional
	public void updateClientInformation(Device device)
	{
		final Long id = authenticationService.getAuthenticatedUserId();
		final User user = userRepository.findOne(id);
		updateClientInformation(user, device);
	}
	
	private ClientInformation createClientInformation(User currentUser, Device device)
	{
		final ClientInformation clientInformation = new ClientInformation();
		clientInformation.setUser(currentUser);
		
		final Page currentPage = Page.getCurrent();
		final WebBrowser currentWebBrowser = currentPage.getWebBrowser();
		
		if (currentWebBrowser.isChrome())
			clientInformation.setBrowser(Browser.CHROME);
		else if (currentWebBrowser.isEdge())
			clientInformation.setBrowser(Browser.EDGE);
		else if (currentWebBrowser.isIE())
			clientInformation.setBrowser(Browser.IE);
		else if (currentWebBrowser.isSafari())
			clientInformation.setBrowser(Browser.SAFARI);
		else if (currentWebBrowser.isFirefox())
			clientInformation.setBrowser(Browser.FIREFOX);
		else if (currentWebBrowser.isOpera())
			clientInformation.setBrowser(Browser.OPERA);
		else
			clientInformation.setBrowser(Browser.UNKNOWN);
		
		clientInformation.setBrowserMajorVersion(currentWebBrowser.getBrowserMajorVersion());
		clientInformation.setBrowserMinorVersion(currentWebBrowser.getBrowserMinorVersion());
		clientInformation.setBrowserOutdated(currentWebBrowser.isTooOldToFunctionProperly());
		clientInformation.setTimezoneOffset(currentWebBrowser.getRawTimezoneOffset());
		clientInformation.setScreenWidth(currentWebBrowser.getScreenWidth());
		clientInformation.setScreenHeight(currentWebBrowser.getScreenHeight());
		clientInformation.setTouchDevice(currentWebBrowser.isTouchDevice());
		clientInformation.setCountry(currentWebBrowser.getLocale().getCountry());
		clientInformation.setLanguage(currentWebBrowser.getLocale().getLanguage());
		clientInformation.setBrowserWidth(currentPage.getBrowserWindowWidth());
		clientInformation.setBrowserHeight(currentPage.getBrowserWindowHeight());
		clientInformation.setLayout(device);
		
		if (currentWebBrowser.isAndroid())
			clientInformation.setOs(OS.ANDROID);
		else if (currentWebBrowser.isLinux())
			clientInformation.setOs(OS.LINUX);
		else if (currentWebBrowser.isWindows())
			clientInformation.setOs(OS.WINDOWS);
		else if (currentWebBrowser.isIOS())
			clientInformation.setOs(OS.IOS);
		else if (currentWebBrowser.isMacOSX())
			clientInformation.setOs(OS.MAC_OSX);
		else if (currentWebBrowser.isWindows())
			clientInformation.setOs(OS.WINDOWS);
		else
			clientInformation.setOs(OS.UNKNOWN);
		
		return clientInformation;
	}
	
	public List<ClientInformation> findAll(User user)
	{
		return clientInformationRepository.findByUser(user);
	}


	public String getLastLoginDate(User user){
		Pageable pageable = new PageRequest(0, 1);
		List<ClientInformation> cl = clientInformationRepository.findByIdAndLaseSeen(user, pageable);//source
		if(cl!=null && !cl.isEmpty() && cl.get(0).getChangeDate()!=null)
			return cl.get(0).getChangeDate().toString();
		else if(cl!=null && !cl.isEmpty())
			return cl.get(0).getCreateDate().toString() != null ? cl.get(0).getCreateDate().toString() : "NA";
		else
			return  "NA";
	}

	public LazyBeanFilteredItemsHandler<ClientInformation> createSubscriptionHandler(User user)
	{
		final Specifications<ClientInformation> specs = Specifications.where((root, query, cb) -> cb.equal(root.get(ClientInformation_.user), user));
		return (specification, pageable) -> clientInformationRepository.findAll(specs.and(specification), pageable);
	}
}
