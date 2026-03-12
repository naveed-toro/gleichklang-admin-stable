package de.binaerebauten.gleichklang.memberweb.view.component;

import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.initializer.AppUI;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation;
import de.binaerebauten.gleichklang.core.service.SubscriptionService;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.view.popup.I18N;

import java.util.Optional;

public class ActivateProlongationComponent extends CustomComponent {

    SubscriptionService subscriptionService = AppUI.getApplicationContext().getBean(SubscriptionService.class);

    UserService userService = AppUI.getApplicationContext().getBean(UserService.class);


    private VerticalLayout wrapper=null;

    public ActivateProlongationComponent(ClientInformation.Device device)
    {
        if(subscriptionService.findCurrentSubscription(userService.getCurrentUser())!=null && subscriptionService.findCurrentSubscription(userService.getCurrentUser()).isPresent() &&
                !subscriptionService.findCurrentSubscription(userService.getCurrentUser()).get().isAutomaticRenewal()) {
        wrapper = new VerticalLayout();
        wrapper.setSizeFull();
        wrapper.setSpacing(true);
        wrapper.addStyleName(CssStyle.PANEL_WRAPPER.getStyleName());
        setCompositionRoot(wrapper);


            Label label = new Label();
            label.setValue(I18N.EXTENSION_DISABLED.msg());
            label.setStyleName(CssStyle.AUTO_RENEWAL_TEXT_FIELD.getStyleName());

            Button button = new Button(I18N.ENABLE_EXTENSION_BUTTON.msg());
            button.setStyleName(CssStyle.AUTO_RENEWAL_BUTTON.getStyleName());
            button.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent clickEvent) {
                    activateProlongation();
                    MessageBox.show("Verlängerung aktiviert", MessageBox.MessageBoxButtons.OK,
                            dialogResult ->{
                                UI.getCurrent().getPage().reload();
                            });
                }
            });
            Label label1 = new Label();
            label1.setValue(I18N.ADVISE_ACTIVATE_EXTENSION.msg());
            label1.setStyleName(CssStyle.AUTO_RENEWAL_TEXT_FIELD.getStyleName());

            wrapper.addComponent(label);
            wrapper.addComponent(button);
            wrapper.addComponent(label1);
            wrapper.setComponentAlignment(button,Alignment.MIDDLE_CENTER);
        }
    }

    private void activateProlongation(){
        final Optional<Subscription> subscription = subscriptionService.findCurrentSubscription(userService.getCurrentUser());
         if(subscription.isPresent()){
                 subscription.get().setAutomaticRenewal(true);
                 subscriptionService.saveAndUpdateStatus(subscription.get());
        }
    }
}