package de.binaerebauten.gleichklang.adminweb.view;

import de.binaerebauten.gleichklang.adminweb.view.HeidelpayView.HeidelpayTab;
import de.binaerebauten.gleichklang.adminweb.view.HeidelpayView.HeidelpayViewListener;
import de.binaerebauten.gleichklang.core.model.heidelpay.Registration;
import de.binaerebauten.gleichklang.core.model.payment.ExternalPaymentRegistration;
import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;
import de.binaerebauten.gleichklang.core.utils.filter.FilterSpecificationBuilder;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.SubNavigateView;
import de.binaerebauten.gleichklang.core.view.component.FilterControlComponent.FilterControlHandler;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.core.view.component.NavigationComponent.SubNavigationListener;
import de.binaerebauten.gleichklang.core.view.component.NavigationEnum;

public interface HeidelpayView extends SubNavigateView<HeidelpayTab, HeidelpayViewListener>
{
	enum HeidelpayTab implements DefaultEnumI18N, NavigationEnum
	{
		LOCAL_REGISTRATION,
		REGISTRATION;
		
		@Override
		public String getPath()
		{
			return name();
		}
		
		@Override
		public String toString()
		{
			return msg();
		}
	}
	
	interface HeidelpayViewListener extends NavigateView.NavigateViewListener, SubNavigationListener
	{
		void synchronizeRegistrations();
		
		void synchronizeChargebacks();
		
		void synchronizeRefunds();
		
		void openUser(String registrationUniqueId);
		
		void openUser(Long userId);
	}
	
	void setLocalRegistrationHandler(LazyBeanFilteredItemsHandler<ExternalPaymentRegistration> handler);
	
	void setRegistrationHandler(LazyBeanFilteredItemsHandler<Registration> handler);
	
	void setUserFilterHandlerAndBuilder(FilterControlHandler filterControlHandler, FilterSpecificationBuilder filterSpecificationBuilder);
}
