package de.binaerebauten.gleichklang.adminweb.presenter;

import com.google.common.base.Strings;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import de.binaerebauten.gleichklang.adminweb.navigation.DefaultNavigatorFactory.AdminMenuItem;
import de.binaerebauten.gleichklang.adminweb.presenter.handler.UserControlHandler;
import de.binaerebauten.gleichklang.adminweb.service.payment.BackofficePaymentService;
import de.binaerebauten.gleichklang.adminweb.view.HeidelpayView;
import de.binaerebauten.gleichklang.adminweb.view.HeidelpayView.HeidelpayTab;
import de.binaerebauten.gleichklang.adminweb.view.HeidelpayView.HeidelpayViewListener;
import de.binaerebauten.gleichklang.core.model.filter.TemplateContext;
import de.binaerebauten.gleichklang.core.model.payment.ExternalPaymentRegistration;
import de.binaerebauten.gleichklang.core.presenter.SubNavigatePresenter;
import de.binaerebauten.gleichklang.core.presenter.filter.DefaultFilterControlHandler;
import de.binaerebauten.gleichklang.core.repository.ExternalPaymentRegistrationRepository;
import de.binaerebauten.gleichklang.core.repository.heidelpay.RegistrationRepository;
import de.binaerebauten.gleichklang.core.service.FilterControlService;
import de.binaerebauten.gleichklang.core.utils.filter.FilterSpecificationBuilder;
import de.binaerebauten.gleichklang.core.view.component.FilterControlComponent.FilterControlFeature;
import org.springframework.context.ApplicationContext;

public class HeidelpayPresenter extends SubNavigatePresenter<HeidelpayTab> implements HeidelpayViewListener
{
	private final BackofficePaymentService heidelpayService;
	private final HeidelpayView view;
	private final RegistrationRepository registrationRepository;
	private final ExternalPaymentRegistrationRepository externalPaymentRegistrationRepository;
	
	private final UserControlHandler userControlHandler;
	
	private final DefaultFilterControlHandler userFilterControlHandler;
	private final FilterSpecificationBuilder filterSpecificationBuilder;
	
	public HeidelpayPresenter(ApplicationContext ctx, HeidelpayView view)
	{
		super(view, HeidelpayTab.class, AdminMenuItem.HEIDELPAY);
		this.view = view;
		
		heidelpayService = ctx.getBean(BackofficePaymentService.class);
		registrationRepository = ctx.getBean(RegistrationRepository.class);
		externalPaymentRegistrationRepository = ctx.getBean(ExternalPaymentRegistrationRepository.class);
		
		userControlHandler = new UserControlHandler(ctx, this);
		
		filterSpecificationBuilder = ctx.getBean(FilterSpecificationBuilder.class);
		userFilterControlHandler = new DefaultFilterControlHandler(ctx.getBean(FilterControlService.class));
		userFilterControlHandler.setTemplateContext(TemplateContext.HEIDELPAY);
		userFilterControlHandler.removeFilterControlFeatures(FilterControlFeature.PREVIEW);
		
		view.setListener(this);
	}
	
	@Override
	public void synchronizeRegistrations()
	{
		heidelpayService.synchronizeRegistrations();
		refresh();
	}
	
	@Override
	public void synchronizeChargebacks()
	{
		heidelpayService.synchronizeChargebacks();
	}
	
	@Override
	public void synchronizeRefunds()
	{
		heidelpayService.synchronizeRefunds();
	}
	
	@Override
	public void enter(HeidelpayTab navigationEnum, String parameters)
	{
		view.setUserFilterHandlerAndBuilder(userFilterControlHandler, filterSpecificationBuilder);
		
		switch(navigationEnum)
		{
			case LOCAL_REGISTRATION:
				view.setLocalRegistrationHandler(externalPaymentRegistrationRepository::findAll);
				break;
			case REGISTRATION:
				view.setRegistrationHandler(registrationRepository::findAll);
				break;
		}
	}
	
	@Override
	public void openUser(String registrationUniqueId)
	{
		if(!Strings.isNullOrEmpty(registrationUniqueId))
		{
			ExternalPaymentRegistration externalPaymentRegistration = externalPaymentRegistrationRepository.findByRegistrationId(registrationUniqueId);
			if(externalPaymentRegistration != null)
			{
				userControlHandler.openUser(externalPaymentRegistration.getUser());
				return;
			}
		}
		
		Notification.show("Keine lokale ExternalPaymentRegistration gefunden für diese ID\nFür den Nutzer an sich kann trotzdem eine andere Registrierung existieren", Type.WARNING_MESSAGE);
	}
	
	@Override
	public void openUser(Long userId)
	{
		userControlHandler.openUser(userId);
	}
}
