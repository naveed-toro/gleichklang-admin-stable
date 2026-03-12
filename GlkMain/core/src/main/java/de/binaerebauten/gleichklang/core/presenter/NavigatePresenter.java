package de.binaerebauten.gleichklang.core.presenter;

import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.utils.ParametersHolder;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.NavigateView.NavigateViewListener;
import de.binaerebauten.gleichklang.core.view.component.NavigationEnum;
import de.binaerebauten.gleichklang.core.view.component.Popup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public abstract class NavigatePresenter extends AbstractPresenter implements NavigateViewListener, PopupOpener
{
	private interface OnDeviceChangeListener
	{
		void onDeviceChanged(Device device);
	}
	private static final Logger LOG = LoggerFactory.getLogger(NavigatePresenter.class);
	private final NavigateView<?> view;
	private final Collection<OnDeviceChangeListener> onDeviceChangeListeners = new ArrayList<>();
	private final Map<String, Popup> openPopups = new HashMap<>();
	
	public NavigatePresenter(NavigateView<?> view)
	{
		Objects.requireNonNull(view);
		this.view = view;
		
		onDeviceChangeListeners.add(view::onDeviceChanged);
	}
	
	@Override
	public void enter(ParametersHolder parameterMap)
	{
	
	}
	
	public void enter()
	{
		enter((ParametersHolder) null);
		enter((String) null);
	}
	
	public NavigateView<?> getView()
	{
		return view;
	}
	
	public void onDeviceChanged(Device device)
	{
		onDeviceChangeListeners.forEach(listener -> listener.onDeviceChanged(device));
	}
	
	@Override
	public void leave()
	{
		new HashSet<>(openPopups.values()).forEach(Window::close);
	}

	@Override
	public boolean tryOpenPopup(Popup popup)
	{
		// Fix if condition for GR3-158 only for AdminWorkItemPopup
		// handling UserDataExportPopup close issue
		if(popup.getUniqueName().contains("AdminWorkItemPopup") || popup.getUniqueName().contains("UserDataExportPopup"))
		{
			openPopups.remove(popup.getUniqueName());
		}
		else
		{
			if (popup == null || openPopups.containsKey(popup.getUniqueName()))
				return false;
		}

		openPopups.put(popup.getUniqueName(), popup);
		popup.addCloseListener(event -> openPopups.remove(popup.getUniqueName()));

		final OnDeviceChangeListener onDeviceChanged = popup::onDeviceChanged;
		onDeviceChangeListeners.add(onDeviceChanged);
		popup.addCloseListener(event -> onDeviceChangeListeners.remove(onDeviceChanged));

		popup.show();
		return true;
	}
	
	@Override
	public void navigateTo(NavigationEnum... navigationEnums)
	{
		final String viewKey = Arrays.stream(navigationEnums)
				.map(NavigationEnum::getPath)
				.collect(Collectors.joining("/"));
		
		try
		{
			UI.getCurrent().getNavigator().navigateTo(viewKey);
		}
		catch (IllegalArgumentException ex)
		{
			LOG.warn("Could not navigate to {}", viewKey, ex);
		}
	}
}
