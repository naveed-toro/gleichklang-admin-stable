package de.binaerebauten.gleichklang.core.navigation;

import com.vaadin.navigator.ViewProvider;
import com.vaadin.ui.UI;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.view.NavigateView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractNavigator implements ViewProvider
{
	public interface ViewItem
	{
		String getKey();
		
		String getCaption();
		
		NavigatePresenter getPresenter(Device device);
	}
	
	public static class DefaultViewItem implements ViewItem
	{
		private final String key;
		private final String caption;
		
		public DefaultViewItem(String key, String caption)
		{
			this.key = key;
			this.caption = caption;
		}
		
		@Override
		public String getKey()
		{
			return key;
		}
		
		@Override
		public String getCaption()
		{
			return caption;
		}
		
		@Override
		public NavigatePresenter getPresenter(Device device)
		{
			return null;
		}
	}
	
	private static final int TABLET_MAX = 1023;
	private static final int TABLET_MIN = 640;
	
	private final Map<String, ViewItem> viewItems = new HashMap<>();
	
	private ViewItem startViewItem;
	private Device device;
	private NavigatePresenter currentPresenter = null;
	
	public AbstractNavigator()
	{
		device = computeDevice();
		addDeviceChangeListener(); //TODO new DefaultNavigator should first remove listener
	}
	
	public void addViewItem(ViewItem viewItem)
	{
		Objects.requireNonNull(viewItem);
		Objects.requireNonNull(viewItem.getKey());
		Objects.requireNonNull(viewItem.getCaption());
		
		viewItems.put(viewItem.getKey(), viewItem);
	}
	
	private ViewItem getViewItem(String key)
	{
		final ViewItem viewItem = viewItems.get(key);
		return viewItem != null ? viewItem : startViewItem;
	}
	
	public void setStartViewItem(ViewItem startViewItem)
	{
		this.startViewItem = startViewItem;
		
		if(currentPresenter == null) navigateTo("");
	}
	
	protected abstract void navigateTo(String viewKey);
	
	@Override
	public String getViewName(String viewAndParameters)
	{
		final String[] parsedViewAndParameters = viewAndParameters.split("/");
		final String view = parsedViewAndParameters[0];
		final ViewItem viewItem = getViewItem(view);
		return viewItem != null ? viewItem.getKey() : null;
	}
	
	@Override
	public NavigateView<?> getView(String viewName)
	{
		final ViewItem viewItem = getViewItem(viewName);
		currentPresenter = viewItem != null ? viewItem.getPresenter(device) : null;
		
		return currentPresenter != null ? currentPresenter.getView() : null;
	}
	
	private Device computeDevice()
	{
		final int browserWidth = UI.getCurrent().getPage().getBrowserWindowWidth();
		
		if (browserWidth < TABLET_MIN)
			return Device.MOBILE;
		
		if (browserWidth <= TABLET_MAX)
			return Device.TABLET;
		
		return Device.DESKTOP;
	}
	
	private void addDeviceChangeListener()
	{
		UI.getCurrent().getPage().addBrowserWindowResizeListener(event ->
		{
			final Device newDevice = computeDevice();
			if (!newDevice.equals(device))
			{
				onDeviceChanged(newDevice);
			}
		});
	}
	
	private void onDeviceChanged(Device newDevice)
	{
		this.device = newDevice;
		if (currentPresenter != null) currentPresenter.onDeviceChanged(device);
	}
	
	public Device getDevice()
	{
		return device;
	}
}
