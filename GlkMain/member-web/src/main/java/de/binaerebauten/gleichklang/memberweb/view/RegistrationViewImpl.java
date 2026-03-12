package de.binaerebauten.gleichklang.memberweb.view;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.presenter.question.QuestionnairePresenter;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.QuestionnaireView;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.view.RegistrationReportingView.RegistrationReportingListener;
import de.binaerebauten.gleichklang.memberweb.view.component.RegistrationWizard;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.teemu.wizards.WizardStep;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static de.binaerebauten.gleichklang.memberweb.view.component.RegistrationWizard.*;

public class RegistrationViewImpl
		extends AbstractNavigateView<RegistrationView.RegistrationViewListener>
		implements RegistrationView
{
	private static final Logger LOG = LoggerFactory.getLogger(RegistrationViewImpl.class);
	
	private final RegistrationWizard wizard;
	private final boolean enableDebugFeatures;
	private final BiMap<Questionnaire, String> questionnaireIdMap = HashBiMap.create();
	private final Button quickFillBtn;
	private Button quickRegisterUserBtn;
	private final ComboBox goToField;
	
	public RegistrationViewImpl(boolean enableDebugFeatures)
	{
		this.enableDebugFeatures = enableDebugFeatures;
		VerticalLayout root = new VerticalLayout();
		//		Label label = new Label(I18N.REGISTRATIONVIEW_WIZARD_LABEL.msg());
		//		root.addComponent(label);
		root.addStyleName(CssStyle.REGISTRATION_CONTENT_VIEW.getStyleName());
		
		this.wizard = new RegistrationWizard();
		
		this.goToField = new ComboBox();
		this.quickFillBtn = new Button(I18N.REGISTRATIONVIEW_QUICKFILL_BUTTON.msg());
		this.goToField.setVisible(false);
		this.quickFillBtn.setVisible(false);
		
		if (enableDebugFeatures)
		{
			this.goToField.setVisible(true);
			this.quickFillBtn.setVisible(true);
			
			HorizontalLayout horizontalLayout = new HorizontalLayout();
			horizontalLayout.addStyleName("temp-components");
			this.goToField.setInputPrompt(I18N.REGISTRATIONVIEW_GOTO_FIELD.msg());
			horizontalLayout.addComponent(this.goToField);
			horizontalLayout.setSpacing(true);
			this.quickRegisterUserBtn = new Button(I18N.REGISTRATIONVIEW_QUICKREGISTER_USER.msg());
			this.quickRegisterUserBtn.setStyleName(CssStyle.DANGER.getStyleName());
			
			this.quickFillBtn.setStyleName(CssStyle.DANGER.getStyleName());
			horizontalLayout.addComponents(this.quickRegisterUserBtn, this.quickFillBtn);
			
			root.addComponent(horizontalLayout);
		}
		
		root.addComponent(wizard);
		
		root.setSpacing(true);
		//		root.setMargin(true);
		root.setSizeFull();
		setCompositionRoot(root);
	}
	
	@Override
	public void setRegistrationStepViews(LinkedHashMap<Questionnaire, QuestionnairePresenter> questionnairePresenterMap, Map<RecommendationCategory, RegistrationReportingListener> reportingStepViewMap)
	{
		wizard.addHTMLTemplateStep(FIRST_STEP, true, false);
		
		RecommendationCategory lastRecommendationCategory = null;
		
		for (Questionnaire questionnaire : questionnairePresenterMap.keySet())
		{
			
			final RecommendationCategory currentRecommendationCategory = questionnaire.getRecommendationCategory();
			if (lastRecommendationCategory != null)
			{
				addReportStep(lastRecommendationCategory, currentRecommendationCategory, reportingStepViewMap);
			}
			addHTMLTemplateStepsForRecommendationCategories(lastRecommendationCategory, currentRecommendationCategory, reportingStepViewMap);
			
			QuestionnaireView questionnaireView = questionnairePresenterMap.get(questionnaire).getQuestionnaireView();
			
			wizard.addNavigateViewWizardStep(questionnaireView, questionnaire.getName());
			questionnaireIdMap.put(questionnaire, questionnaire.getI18nKey());
			
			lastRecommendationCategory = currentRecommendationCategory;
		}
		
		RegistrationReportingView registrationReportingView = reportingStepViewMap.get(null).getView();
		wizard.addNavigateViewWizardStep(registrationReportingView, "report " + registrationReportingView.getTemplateName());
		
		if (enableDebugFeatures)
		{
			initTempComponents(questionnaireIdMap);
		}
	}
	
	private void addHTMLTemplateStepsForRecommendationCategories(RecommendationCategory lastRecommendationCategory,
			RecommendationCategory currentRecommendationCategory, Map<RecommendationCategory, RegistrationReportingListener> reportingStepViewMap)
	{
		// first pre_category template
		if (lastRecommendationCategory == null && currentRecommendationCategory != null)
		{
			addHTMLTemplateStepForRecommendationCategory(currentRecommendationCategory, PRE_TEMPLATE_SUFFIX);
		}
		
		// post_category template and pre_category template
		else if (!Objects.equals(lastRecommendationCategory, currentRecommendationCategory))
		{
			if (currentRecommendationCategory != null)
			{
				addHTMLTemplateStepForRecommendationCategory(currentRecommendationCategory, PRE_TEMPLATE_SUFFIX);
			}
			else
			{
				addHTMLTemplateStepForRecommendationCategory(null, PRE_TEMPLATE_SUFFIX);
			}
		}
	}
	
	private void addReportStep(RecommendationCategory lastRecommendationCategory,
			RecommendationCategory currentRecommendationCategory, Map<RecommendationCategory, RegistrationReportingListener> reportingStepViewMap)
	{
		
		RegistrationReportingView registrationReportingView = reportingStepViewMap.get(lastRecommendationCategory).getView();
		if (!Objects.equals(lastRecommendationCategory, currentRecommendationCategory))
		{
			wizard.addNavigateViewWizardStep(registrationReportingView, "report " + registrationReportingView.getTemplateName());
		}
		
	}
	
	private void addHTMLTemplateStepForRecommendationCategory(RecommendationCategory recommendationCategory, String suffix)
	{
		final String templateName = getTemplateNameForRecommendationCategory(recommendationCategory, suffix);
		wizard.addHTMLTemplateStep(templateName, true, true);
	}
	
	private String getTemplateNameForRecommendationCategory(RecommendationCategory recommendationCategory, String suffix)
	{
		String prefix;
		if (recommendationCategory == null)
		{
			prefix = PERSONAL_STEP.toLowerCase();
		}
		else
		{
			prefix = recommendationCategory.name().toLowerCase();
		}
		
		return prefix + suffix;
	}
	
	@Override
	public void setSubscriptionOfferView(SubscriptionOfferPaymentView subscriptionOfferPaymentView)
	{
		wizard.addNavigateViewWizardStep(subscriptionOfferPaymentView, REGISTRATION_PAYMENT_STEP);
		goToField.addItem(REGISTRATION_PAYMENT_STEP);
	}
	
	@Override
	public void setExternalPaymentFormView(ExternalPaymentFormView externalPaymentFormView)
	{
		wizard.addNavigateViewWizardStep(externalPaymentFormView, EXTERNAL_PAYMENT_STEP);
		goToField.addItem(EXTERNAL_PAYMENT_STEP);
	}
	
	@Override
	public void setPaymentResultView(PaymentResultView paymentResultView)
	{
		wizard.addNavigateViewWizardStep(paymentResultView, REGISTRATION_PAYMENT_RESULT_STEP);
	}
	
	@Override
	public void setPreConfigNotificationsView(PreConfigNotificationsView preConfigNotificationsView)
	{
		wizard.addNavigateViewWizardStep(preConfigNotificationsView, CONFIG_NOTIFICATIONS_STEP);
		goToField.addItem(CONFIG_NOTIFICATIONS_STEP);
	}
	
	@Override
	public void setLastQuestionSteps(Map<Questionnaire, QuestionnaireView> questionnaireViewsMap)
	{
		for (Questionnaire questionnaire : questionnaireViewsMap.keySet())
		{
			QuestionnaireView questionView = questionnaireViewsMap.get(questionnaire);
			wizard.addNavigateViewWizardStep(questionView, questionnaire.getName());
			goToField.addItem(questionnaire.getName());
		}
	}
	
	@Override
	public void setPersonalDataStep(PersonalDataView personalDataView)
	{
		wizard.addNavigateViewWizardStep(personalDataView, MASTER_STEP);
		goToField.addItem(MASTER_STEP);
	}
	
	@Override
	public void setFinalStep()
	{
		wizard.addHTMLTemplateStep(LAST_STEP, false, true);
		goToField.addItem(LAST_STEP);
	}
	
	/**
	 * Quick Access component for test purposes
	 *
	 * @param questionnaireIdMap
	 * @return
	 */
	
	private void initTempComponents(BiMap<Questionnaire, String> questionnaireIdMap)
	{
		questionnaireIdMap.clear();
		
		for (WizardStep wizardStep : this.wizard.getSteps())
		{
			final Component content = wizardStep.getContent();
			if (content instanceof QuestionnaireView)
			{
				QuestionnaireView registrationQuestionnaireView = (QuestionnaireView) content;
				final Questionnaire questionnaire = registrationQuestionnaireView.getQuestionnaire();
				final String stepId = questionnaire.getName();
				goToField.addItem(stepId);
				questionnaireIdMap.inverse().put(stepId, questionnaire);
			}
			else
			{
				String id = content.getId();
				if (id != null)
				{
					goToField.addItem(id);
				}
			}
		}
		
		goToField.addValueChangeListener(event ->
		{
			String selectedStep = (String) event.getProperty().getValue();
			if (StringUtils.isNotBlank(selectedStep))
			{
				this.wizard.activateStep(selectedStep);
			}
		});
		
		quickRegisterUserBtn.addClickListener(event ->
		{
			fireEvent(RegistrationViewListener::quickRegisterUser);
			quickRegisterUserBtn.setEnabled(false);
			quickRegisterUserBtn.setDescription(I18N.REGISTRATIONVIEW_QUICKREGISTER_USER_DESC.msg());
		});
		quickFillBtn.addClickListener(event ->
		{
			fireEvent(RegistrationViewListener::quickFill);
		});
	}
	
	@Override
	public void enableNextQuestionnaire(boolean show)
	{
		//		wizard.setEnabledNext(show);
	}
	
	@Override
	public RegistrationWizard getRegistrationWizard()
	{
		return wizard;
	}
}
