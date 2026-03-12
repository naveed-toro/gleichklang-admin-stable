package de.binaerebauten.gleichklang.core;

import de.binaerebauten.gleichklang.core.model.user.ClientInformation;
import de.binaerebauten.gleichklang.core.navigation.DefaultNavigator;
import de.binaerebauten.gleichklang.core.presenter.ManualNavigatePresenter;
import de.binaerebauten.gleichklang.core.utils.ParametersHolder;
import de.binaerebauten.gleichklang.core.view.DeactivateProlongationView;
import org.springframework.context.ApplicationContext;

import java.util.Objects;

public abstract class DeactivateProlongationPresenter<T extends DeactivateProlongationView.DeactivateProlongationViewListener> extends ManualNavigatePresenter implements DeactivateProlongationView.DeactivateProlongationViewListener
{

	private final DeactivateProlongationView<T> view;
	private final ClientInformation.Device device;
	private final DefaultNavigator navigator;

	@Override
 	public void goToOverviewPage()
	{
		navigator.navigateTo("HOME");
	}
	public DeactivateProlongationPresenter(ApplicationContext ctx, DeactivateProlongationView<T> deactivateView, ClientInformation.Device device, DefaultNavigator navigator)
	{
		super(deactivateView);

		Objects.requireNonNull(ctx, "ctx == null");
		this.device=device;
		this.view = Objects.requireNonNull(deactivateView, "loginView == null");
		deactivateView.setListener((T) this);
		this.navigator=navigator;
	}

	@Override
	public void enter(ParametersHolder parameterMap)
	{

	}

}
