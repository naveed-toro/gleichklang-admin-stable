package de.binaerebauten.gleichklang.memberweb.view.component;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.themes.ValoTheme;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.component.HTMLLayout;
import de.binaerebauten.gleichklang.core.view.component.Savable;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.view.I18N;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.teemu.wizards.Wizard;
import org.vaadin.teemu.wizards.WizardProgressBar;
import org.vaadin.teemu.wizards.WizardStep;

import java.util.Objects;

public class RegistrationWizard extends Wizard
{
	public static final String PERSONAL_STEP = "personal";
	public static final String REGISTRATION_PAYMENT_RESULT_STEP = "RegistrationPaymentResult";
	public static final String REGISTRATION_PAYMENT_STEP = "RegistrationPayment";
	public static final String EXTERNAL_PAYMENT_STEP = "ExternalPayment";
	public static final String FIRST_STEP = "firststep";
	public static final String LAST_STEP = "laststep";
	public static final String CONFIG_NOTIFICATIONS_STEP = "configNotifications";
	public static final String MASTER_STEP = "User_Master";
	public static final String POST_TEMPLATE_SUFFIX = "_post";
	public static final String PRE_TEMPLATE_SUFFIX = "_pre";
	public static final String ERROR_PARAM = "?payment_error=";
	private static final Logger logger = LoggerFactory.getLogger(RegistrationWizard.class);
	
	public RegistrationWizard()
	{
		super();
		
		updateFooter();
		getNextButton().setEnabled(true);
		getFinishButton().setVisible(false);
		setUriFragmentEnabled(true);
	}
	
	public void destroyWizard()
	{
		setUriFragmentEnabled(false);
	}
	
	/**
	 * Updates Wizard Header.
	 * Sets wizard step captions and the last completed step.
	 */
	public void updateHeader()
	{
		/*
		 * Crazy calculation because first_step, post and pre are not really part of the registration wizard anymore. The real removing of these produce other errors that are complicated to find.
		 * The PostRegistration should be rewritten so that this crazy calculation are not exists anymore. Currently it is a efficiently solution.
		 */
		final long stepsSum = steps.stream().map(WizardStep::getCaption).filter(s -> !s.contains(FIRST_STEP) && !s.contains(POST_TEMPLATE_SUFFIX) && !s.contains(PRE_TEMPLATE_SUFFIX)).count();
		final long currentIndex = steps.subList(0, steps.indexOf(currentStep) + 1).stream().map(WizardStep::getCaption).filter(s -> !s.contains(FIRST_STEP) && !s.contains(POST_TEMPLATE_SUFFIX) && !s.contains(PRE_TEMPLATE_SUFFIX)).count() - 1;
		
		final float percentageProgress = currentIndex <= 0 ? 0 : (float) (currentIndex + 1) / (float) stepsSum * 100;
		final double logPercentageProgress = Math.log(percentageProgress / 25f + 1f) / Math.log(4) * 86f;
		String completedStepsLabel = I18N.REGISTRATIONVIEW_WIZARD_STEP.msg(Math.round(logPercentageProgress));
		
		WizardProgressBar progressBar = ((WizardProgressBar) this.getHeader());
		progressBar.setWidthUndefined();
		VerticalLayout verticalLayout = (VerticalLayout) progressBar.iterator().next();
		verticalLayout.setStyleName(CssStyle.WIZARD_HEADER.getStyleName());
		for (Component component : verticalLayout)
		{
			if (component instanceof HorizontalLayout)
			{
				HorizontalLayout stepCaptions = (HorizontalLayout) component;
				stepCaptions.setStyleName(CssStyle.WIZARD_STEP_CAPTIONS.getStyleName());
				stepCaptions.setSizeUndefined();
				stepCaptions.removeAllComponents();
				final Label stepLabel = new Label(completedStepsLabel, ContentMode.HTML);
				stepLabel.setStyleName(CssStyle.WIZARD_STEP_CAPTION.getStyleName());
				stepCaptions.addComponent(stepLabel);
			}
			
			if (component instanceof ProgressBar)
			{
				component.setSizeUndefined();
			}
		}
	}
	
	private void updateFooter()
	{
		footer.setStyleName(CssStyle.WIZARD_FOOTER.getStyleName());
		
		final Button nextButton = getNextButton();
		nextButton.setCaption(I18N.REGISTRATION_WIZARD_NAVIGATION_NEXT.msg());
		nextButton.setIcon(FontAwesome.CHEVRON_RIGHT);
		nextButton.setStyleName(CssStyle.WIZARD_FOOTER_NEXT.getStyleName());
		nextButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_RIGHT);
		
		final Button backButton = getBackButton();
		backButton.setCaption("");
		backButton.setIcon(FontAwesome.CHEVRON_LEFT);
		backButton.setStyleName(CssStyle.WIZARD_FOOTER_BACK.getStyleName());
		backButton.setVisible(false);
		
		refreshClickListeners();
		
		final Button finishButton = getFinishButton();
		finishButton.setCaption(I18N.REGISTRATIONVIEW_WIZARD_BUTTON_FINISH.msg());
		finishButton.setStyleName(CssStyle.WIZARD_FOOTER_FINISH.getStyleName());
		finishButton.setIcon(new ThemeResource("img/double-chevron.svg"));
		finishButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_RIGHT);
		
		getCancelButton().setVisible(false);
	}
	
	/**
	 * Hides the next button for iFrame (the button inside iFrame should be used instead).
	 * Shows the back button for the payment step.
	 */
	private void updateBackAndNextButtons()
	{
		boolean isPaymentStep = Objects.equals(currentStep, idMap.get(EXTERNAL_PAYMENT_STEP));
		boolean isLastStep = Objects.equals(currentStep, idMap.get(LAST_STEP));
		
		getNextButton().setEnabled(!(isPaymentStep || isLastStep));
		getBackButton().setVisible(isPaymentStep);
		getFinishButton().setVisible(isLastStep);
		getNextButton().setVisible(!(isPaymentStep || isLastStep));
	}
	
	private void refreshClickListeners()
	{
		Button nextButton = getNextButton();
		nextButton.getListeners(Button.ClickEvent.class)
				.forEach(l -> nextButton.removeClickListener((ClickListener) l));
		
		nextButton.addClickListener(event ->
		{
			final Component component = currentStep.getContent();
			if (component instanceof Savable)
			{
				final Savable savable = (Savable) component;
				savable.saveComplete(result ->
				{
					if (result.isSuccess()) next();
				});
			}
			else
			{
				next();
			}
			
			updateBackAndNextButtons();
		});
		
		nextButton.addClickListener(event -> updateBackAndNextButtons());
	}
	
	public void next()
	{
		try
		{
			super.next();
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
			Notification.show(I18N.REGISTRATION_WIZARD_ERROR_TITLE.msg(),
					I18N.REGISTRATION_WIZARD_ERROR_MSG.msg(), Type.ERROR_MESSAGE);
			back();
		}
	}
	
	public void back()
	{
		super.back();
		updateBackAndNextButtons();
	}
	
	public void addOnFinishClickListener(Button.ClickListener listener)
	{
		this.getFinishButton().addClickListener(listener);
	}
	
	private WizardStep getAndProcessStepUrl(String id)
	{
		final WizardStep wizardStep;
		
		if (id.contains(ERROR_PARAM))
		{
			wizardStep = idMap.get(id.substring(0, id.indexOf(ERROR_PARAM)));
			
			final String fragment = id.substring(id.indexOf(ERROR_PARAM));
			final String errorMsg = StringUtils.substringAfterLast(fragment, ERROR_PARAM);
			
			Notification.show(errorMsg, de.binaerebauten.gleichklang.memberweb.presenter.I18N.SUBSCRIPTIONPRESENTER_PAYMENT_PURCHASED_ERROR.msg(), Type.WARNING_MESSAGE);
		}
		else
		{
			wizardStep = idMap.get(id);
		}
		
		return wizardStep;
	}
	
	/**
	 * This method is overridden to make it public accessible.
	 *
	 * @param id the wizard step id to activate
	 */
	@Override
	public void activateStep(String id)
	{
		final WizardStep wizardStep = getAndProcessStepUrl(id);
		
		Objects.requireNonNull(wizardStep, "wizardStep == null");
		//step is after current step AND page is NOT category_pre or firststep (static html) --> move forward
		if (steps.indexOf(wizardStep) >= steps.indexOf(getCurrentStep()) && !id.contains("_pre") && !id.contains("firststep"))
		{
			activateStep(wizardStep);
		}
		//step is after current step AND page is category_pre or firststep (static html) --> skip step and activate next
		else if (steps.indexOf(wizardStep) >= steps.indexOf(getCurrentStep()) && (id.contains("_pre") || id.contains("firststep")))
		{
			next();
			
		}
		//step is before current step (user pressed back) --> stay on current step
		else if (!Objects.equals(currentStep, idMap.get(EXTERNAL_PAYMENT_STEP)) && steps.indexOf(wizardStep) < steps.indexOf(getCurrentStep()))
		{
			activateStep(getCurrentStep());
		}
		
		updateBackAndNextButtons();
		UI.getCurrent().setScrollTop(0);
	}
	
	/**
	 * Adds a wizard step for the given parameters.
	 *
	 * @param navigateView
	 * @param id
	 * @return the added wizrd step
	 */
	public WizardStep addNavigateViewWizardStep(NavigateView navigateView, String id)
	{
		return addWizardStep(navigateView, id, true, true);
	}
	
	public void addHTMLTemplateStep(String templateName, boolean onAdvance, boolean onBack)
	{
		final HTMLLayout customLayout = new HTMLLayout(templateName);
		
		if (customLayout.isResourceExists(UI.getCurrent()))
		{
			this.addWizardStep(customLayout, templateName, onAdvance, onBack);
		}
		else
		{
			logger.warn("Resource {} wasn't found", templateName);
		}
	}
	
	private WizardStep addWizardStep(Component component, String id, boolean onAdvance, boolean onBack)
	{
		component.setStyleName(CssStyle.WIZARD_STEP.getStyleName());
		
		final WizardStep wizardStep = new WizardStep()
		{
			@Override
			public String getCaption()
			{
				return id;
			}
			
			@Override
			public Component getContent()
			{
				return component;
			}
			
			@Override
			public boolean onAdvance()
			{
				return onAdvance;
			}
			
			@Override
			public boolean onBack()
			{
				return onBack;
			}
		};
		
		if (!idMap.containsKey(id))
		{
			addStep(wizardStep, id);
		}
		
		return wizardStep;
	}
	
	public WizardStep getCurrentStep()
	{
		return currentStep;
	}
	
}
