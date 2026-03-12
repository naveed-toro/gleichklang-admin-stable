package de.binaerebauten.gleichklang.core.view;

import com.vaadin.server.FontAwesome;

import com.vaadin.server.VaadinSession;
import com.vaadin.ui.*;

import de.binaerebauten.gleichklang.core.initializer.AppUI;
import de.binaerebauten.gleichklang.core.model.ActiveDeactiveSubscription;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.message.ReceiverEnvelope;
import de.binaerebauten.gleichklang.core.model.message.SenderEnvelope;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation;
import de.binaerebauten.gleichklang.core.model.user.SignableUser;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.ActivateDeactiveSubscriptionRepository;
import de.binaerebauten.gleichklang.core.service.MessageService;
import de.binaerebauten.gleichklang.core.service.SubscriptionService;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.view.component.FormPanel;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;


public abstract class DeactivateProlongationViewImpl<T extends DeactivateProlongationView.DeactivateProlongationViewListener> extends AbstractNavigateView<T> implements DeactivateProlongationView<T> {
    private final ClientInformation.Device device;

    private VerticalLayout rootLayout;
    private String page;
    Button button;
    Button buttonOne;
    Button buttonTwo;
    private static final String AUTHENTICATED_USER_TYPE_ATTRIBUTE = "AUTHENTICATED_USER_TYPE";
    private Button  page1button3,page2button2,page3button2;
    public DeactivateProlongationViewImpl(ClientInformation.Device device) {
        this.device = device;
        initUI();
    }

    private void initUI() {
        onDeviceChanged(device);
        page1button3=new Button(I18N.DEACTIVATE_PROLONGATION_PAGE1_BUTTON3_CAPTION.msg());

        page2button2=new Button(I18N.DEACTIVATE_PROLONGATION_PAGE2_BUTTON2_CAPTION.msg());

        page3button2=new Button(I18N.DEACTIVATE_PROLONGATION_PAGE3_BUTTON2_CAPTION.msg());

        page1button3.addClickListener(event -> {
                this.rootLayout = createDeactivateProlongationFirstFormButton3ClickView();
                setCustomCompositionRoot(rootLayout);
            });

        page2button2.addClickListener(event -> {

            this.rootLayout = createDeactivateProlongationSecondFormButton2ClickView();
            setCustomCompositionRoot(rootLayout);

        });

        page3button2.addClickListener(event->{

                UserService userService=AppUI.getApplicationContext().getBean(UserService.class);
                SubscriptionService subscriptionService=AppUI.getApplicationContext().getBean(SubscriptionService.class);
                ActivateDeactiveSubscriptionRepository activateDeactiveSubscriptionRepository=AppUI.getApplicationContext().getBean(ActivateDeactiveSubscriptionRepository.class);

                final Optional<Subscription> subscription=subscriptionService.findCurrentSubscription(userService.getCurrentUser());
                if(subscription.isPresent()){
                    Objects.requireNonNull(subscription,"subscription == null");

                    if(Boolean.TRUE.equals(subscription.get().getCurrent())){

                        ActiveDeactiveSubscription activeDeactiveSubscription=new ActiveDeactiveSubscription();
                        activeDeactiveSubscription.setSubscription(subscription.get());
                        activeDeactiveSubscription.setCreateDate(LocalDateTime.now());
                        activeDeactiveSubscription.setType("Deactivated");
                        activeDeactiveSubscription.setUserType("Member");
                        activeDeactiveSubscription.setUser(subscription.get().getUser());
                        activeDeactiveSubscription.setAlias(subscription.get().getUser().getAlias());
                        activateDeactiveSubscriptionRepository.save(activeDeactiveSubscription);
                        subscription.get().setAutomaticRenewal(false);
                        subscriptionService.deactivateAutoRenewal(subscription.get());
                    }
                }
            this.rootLayout = createDeactivateProlongationFirstFormButton1ClickView(true);
            setCustomCompositionRoot(rootLayout);

        });

        this.rootLayout = createDeactivateProlongationFirstForm();
        setCustomCompositionRoot(rootLayout);

    }

    private VerticalLayout createDeactivateProlongationFirstForm() {

        page="PAGE1";
        final HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeFull();

        final FormPanel formPanel = new FormPanel(I18N.DEACTIVATE_PROLONGATION_PAGE1_CAPTION_TITLE.msg());

        final Label label = new Label();
        label.setCaption(I18N.DEACTIVATE_PROLONGATION_PAGE1_CAPTION_DESCRIPTION.msg() + "\n\n" + I18N.DEACTIVATE_PROLONGATION_PAGE1_CAPTION_DESCRIPTIONS.msg());
        label.addStyleName(CssStyle.TEXT_COLOR.getStyleName());

        formPanel.addStyleName(CssStyle.GK_PANEL.getStyleName());
        formPanel.addComponent(label);
        formPanel.addComponent(layout);

        layout.addComponent(getButtonTwo(I18N.DEACTIVATE_PROLONGATION_PAGE1_BUTTON1_CAPTION.msg()));

        layout.addComponent(getButtonOne(I18N.DEACTIVATE_PROLONGATION_PAGE1_BUTTON2_CAPTION.msg()));
        layout.addComponent(new Label("\t"));
        layout.addComponent(page1button3);

        rootLayout = new VerticalLayout();
        rootLayout.setSpacing(false);
        rootLayout.addComponent(formPanel);

        return rootLayout;

    }

    public void setCustomCompositionRoot(Component compositionRoot) {
        try {
            this.setCompositionRoot(compositionRoot);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private VerticalLayout createDeactivateProlongationFirstFormButton1ClickView(boolean page3button2) {

        final HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeFull();

        final FormPanel formPanel = new FormPanel(I18N.DEACTIVATE_PROLONGATION_PAGE1_CAPTION_TITLE.msg());

        final Label label = new Label();

        label.setCaption(I18N.DEACTIVATE_PROLONGATION_PAGE1_BUTTON1_NEXT_DESCRIPTION.msg());

        if(page3button2)
        {
            label.setCaption(I18N.DEACTIVATE_PROLONGATION_PAGE3_BUTTON1_NEXT_DESCRIPTION.msg());
        }
        label.addStyleName(CssStyle.TEXT_COLOR.getStyleName());

        formPanel.addStyleName(CssStyle.GK_PANEL.getStyleName());
        formPanel.addComponent(label);
        formPanel.addComponent(layout);

        layout.addComponent(getButton(I18N.DEACTIVATE_PROLONGATION_PAGE_BUTTON_BACK_BUTTON_CAPTION.msg()));
        layout.addComponent(new Label("\t\t"));
        layout.addComponent(getButton(I18N.DEACTIVATE_PROLONGATION_PAGE_BUTTON_NEXT_BUTTON_CAPTION.msg()));

        rootLayout = new VerticalLayout();
        rootLayout.setSpacing(false);
        rootLayout.addComponent(formPanel);

        return rootLayout;

    }

    private VerticalLayout createDeactivateProlongationSecondFormButton2ClickView() {

        page="PAGE2";
        final HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeFull();

        final FormPanel formPanel = new FormPanel(I18N.DEACTIVATE_PROLONGATION_PAGE3_CAPTION_TITLE.msg());

        final Label label = new Label();
        label.setCaption(I18N.DEACTIVATE_PROLONGATION_PAGE3_CAPTION_DESCRIPTION.msg());
        label.addStyleName(CssStyle.TEXT_COLOR.getStyleName());

        formPanel.addStyleName(CssStyle.GK_PANEL.getStyleName());
        formPanel.addComponent(label);
        formPanel.addComponent(layout);

        layout.addComponent(getButton(I18N.DEACTIVATE_PROLONGATION_PAGE3_BUTTON1_CAPTION.msg()));
        layout.addComponent(page3button2);
        rootLayout = new VerticalLayout();
        rootLayout.setSpacing(false);
        rootLayout.addComponent(formPanel);
        return rootLayout;
    }



    private VerticalLayout createDeactivateProlongationFirstFormButton3ClickView() {
        page="PAGE3";
        final HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeFull();

        final FormPanel formPanel = new FormPanel(I18N.DEACTIVATE_PROLONGATION_PAGE2_CAPTION_TITLE.msg());

        final Label label = new Label();
        label.setCaption(I18N.DEACTIVATE_PROLONGATION_PAGE2_BUTTON3_NEXT_DESCRIPTION.msg() + "\n\n" + I18N.DEACTIVATE_PROLONGATION_PAGE2_BUTTON3_NEXT_DESCRIPTIONS1.msg() + "\n\n" + I18N.DEACTIVATE_PROLONGATION_PAGE2_BUTTON3_NEXT_DESCRIPTIONS2.msg());
        label.addStyleName(CssStyle.TEXT_COLOR.getStyleName());

        formPanel.addStyleName(CssStyle.GK_PANEL.getStyleName());
        formPanel.addComponent(label);
        formPanel.addComponent(layout);

        layout.addComponent(getButton(I18N.DEACTIVATE_PROLONGATION_PAGE2_BUTTON1_CAPTION.msg()));
        layout.addComponent(page2button2);
        rootLayout = new VerticalLayout();
        rootLayout.setSpacing(false);
        rootLayout.addComponent(formPanel);
        return rootLayout;

    }


    private VerticalLayout createDeactivateProlongationFirstFormButton2ClickView() {
        final HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeFull();
        final FormPanel formPanel = new FormPanel(I18N.DEACTIVATE_PROLONGATION_PAGE1_CAPTION_TITLE.msg());

        final Label label = new Label();
        label.setCaption(I18N.MESAAGE_TO_ADMIN_UI.msg());
        label.addStyleName(CssStyle.TEXT_COLOR.getStyleName());

        formPanel.addStyleName(CssStyle.GK_PANEL.getStyleName());
        formPanel.addComponent(label);
        formPanel.addComponent(layout);

        layout.addComponent(getButton(I18N.DEACTIVATE_PROLONGATION_PAGE_BUTTON_BACK_BUTTON_CAPTION.msg()));
        layout.addComponent(new Label("\t\t"));
        layout.addComponent(getButton(I18N.DEACTIVATE_PROLONGATION_PAGE_BUTTON_NEXT_BUTTON_CAPTION.msg()));

        rootLayout = new VerticalLayout();
        rootLayout.setSpacing(false);
        rootLayout.addComponent(formPanel);

        return rootLayout;

    }

    public Button getButtonTwo(String caption){
        buttonTwo = new Button(caption);
        buttonTwo.setWidth("235px");
        if (buttonTwo.getCaption().equals(I18N.DEACTIVATE_PROLONGATION_PAGE1_BUTTON1_CAPTION.msg())
                || buttonTwo.getCaption().equals(I18N.DEACTIVATE_PROLONGATION_PAGE2_BUTTON1_CAPTION.msg())) {
            buttonTwo.addClickListener(event -> {
                this.rootLayout = createDeactivateProlongationFirstFormButton1ClickView(false);
                setCustomCompositionRoot(rootLayout);
            });
        }
        return buttonTwo;
    }


    public Button getButton(String caption) {

        button = new Button(caption);
        //button.setHeight("55px");

        if (caption.equals(I18N.DEACTIVATE_PROLONGATION_PAGE1_BUTTON2_CAPTION.msg()))
        {
            button.setWidth("470px");
            if (I18N.DEACTIVATE_PROLONGATION_PAGE1_BUTTON2_CAPTION.msg().contains("beraten lassen."))
            {
                button.setWidth("470px");
            }
        }

        if (caption.equals(I18N.DEACTIVATE_PROLONGATION_PAGE1_BUTTON1_CAPTION.msg())
                || caption.equals(I18N.DEACTIVATE_PROLONGATION_PAGE2_BUTTON1_CAPTION.msg()) ||
                caption.equals(I18N.DEACTIVATE_PROLONGATION_PAGE3_BUTTON1_CAPTION.msg())) {
            button.addClickListener(event -> {
                this.rootLayout = createDeactivateProlongationFirstFormButton1ClickView(false);
                setCustomCompositionRoot(rootLayout);
            });
        }

        if (caption.equals(I18N.DEACTIVATE_PROLONGATION_PAGE_BUTTON_NEXT_BUTTON_CAPTION.msg())) {
            button.addClickListener(event -> {
                getListener().goToOverviewPage();
            });
        }

        if (caption.equals(I18N.DEACTIVATE_PROLONGATION_PAGE_BUTTON_BACK_BUTTON_CAPTION.msg())) {
            button.addClickListener(event -> {
                if(this.page.equals("PAGE1"))
                {
                    this.rootLayout = createDeactivateProlongationFirstForm();
                    setCustomCompositionRoot(rootLayout);
                }
                if(this.page.equals("PAGE2"))
                {
                    this.rootLayout = createDeactivateProlongationFirstFormButton3ClickView();
                    setCustomCompositionRoot(rootLayout);
                }
                if(this.page.equals("PAGE3"))
                {
                    this.rootLayout = createDeactivateProlongationSecondFormButton2ClickView();
                    setCustomCompositionRoot(rootLayout);
                }
            });
        }

        return button;
    }

    public Button getButtonOne(String caption){
        buttonOne = new Button(caption);
        buttonOne.setWidth("506px");
        if (buttonOne.getCaption().equals(I18N.DEACTIVATE_PROLONGATION_PAGE1_BUTTON2_CAPTION.msg())) {

            buttonOne.addClickListener(event -> {

                MessageService messageService = AppUI.getApplicationContext().getBean(MessageService.class);
                UserService userService = AppUI.getApplicationContext().getBean(UserService.class);
                try {
                    User user = userService.getCurrentUser();
                    Message message = new Message();
                    message.setReceiverEnvelope(new ReceiverEnvelope());
                    message.setSenderEnvelope(new SenderEnvelope());
                    message.getReceiverEnvelope().setMessage(message);
                    message.getSenderEnvelope().setMessage(message);
                    message.getSenderEnvelope().setUser(user);
                    message.setSubject(I18N.MAIL_TO_ADMIN_SUBJECT.msg());
                    message.setBody(I18N.MAIL_TO_ADMIN_TEXT1.msg() + " " + user.getAlias() + " " + I18N.MAIL_TO_ADMIN_TEXT2.msg());

                    messageService.sendMessageToAdmin(message, null);

                } catch (ValidationException e) {

                }
                this.rootLayout = createDeactivateProlongationFirstFormButton2ClickView();
                setCustomCompositionRoot(rootLayout);
            });
        }
        return buttonOne;

    }


    @Override
    public void onDeviceChanged(ClientInformation.Device device)
    {
        createResponsiveLayout(device);
    }

    private void createResponsiveLayout(ClientInformation.Device device)
    {
        if (device == ClientInformation.Device.DESKTOP)
            createDesktopView();
        else if (device == ClientInformation.Device.TABLET)
            createTabletView();
        else if (device == ClientInformation.Device.MOBILE)
            createMobileView();

    }


    private void createMobileView()
    {
        if(this.button!=null)
            this.button.setWidth("120px");
        if(this.page1button3!=null)
            this.page1button3.setWidth("120px");
        if(this.buttonOne!=null) {
            this.buttonOne.setWidth("100px");
            buttonOne.addStyleName(CssStyle.MARGIN_LEFT.getStyleName());
        }
        if(this.buttonTwo!=null)
            this.buttonTwo.setWidth("100px");
    }

    private void createTabletView()
    {
        if(button!=null)
            this.button.setWidth("200px");
        if(this.page1button3!=null)
            this.page1button3.setWidth("200px");
        if(this.buttonOne!=null) {
            this.buttonOne.setWidth("200px");
            buttonOne.addStyleName(CssStyle.MARGIN_LEFT.getStyleName());
        }
        if(this.buttonTwo!=null)
            this.buttonTwo.setWidth("200px");
    }

    private void createDesktopView()
    {
        if(this.buttonOne!=null)
            buttonOne.setWidth("470px");
        if(this.button!=null)
            this.button.setWidth("280px");
        if(this.page1button3!=null)
            this.page1button3.setWidth("280px");
        if(this.buttonTwo!=null)
            this.buttonTwo.setWidth("235px");

    }
}



