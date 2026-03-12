package de.binaerebauten.gleichklang.adminweb.view.popup;

import com.vaadin.data.validator.BigDecimalRangeValidator;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.adminweb.presenter.handler.DefaultInvoiceHandler;
import de.binaerebauten.gleichklang.adminweb.presenter.handler.UserControlHandler;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.reminder.AdminReminder;
import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.ProductRepository;
import de.binaerebauten.gleichklang.core.service.RelationshipService;
import de.binaerebauten.gleichklang.core.service.SubscriptionService;
import de.binaerebauten.gleichklang.core.service.mail.UndeliverableMailService;
import de.binaerebauten.gleichklang.core.service.payment.PaymentService;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.core.view.popup.GenericPopup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PaybackPopup  extends GenericPopup {

    private static final Logger Log = LoggerFactory.getLogger(PaybackPopup.class);

    private String popupHeight = "650px", popupWidth = "950px";
    private VerticalLayout parentLayout, dataTextLayout;
    private HorizontalLayout recurranceLayout;
    DateField dateField;
    UserControlHandler userControlHandler;
    Button calculateButton;
    TextField textField;
    DefaultInvoiceHandler defaultInvoiceHandler;
    private final SubscriptionService service;
    private final PaymentService paymentService;
    private final RelationshipService relationshipService;
    private final ProductRepository productRepository;
    BigDecimal maxRefundAmount = BigDecimal.ZERO;
    private final User user;
    Optional<AbstractPayment> abstractPayment;


    public PaybackPopup(PaymentService paymentService, User user, SubscriptionService subscriptionService, UserControlHandler userControlHandler, RelationshipService relationshipService,
                        DefaultInvoiceHandler defalutInvoiceHandler, ProductRepository productRepository) {

        super();
        this.userControlHandler = userControlHandler;
        setStyleName(CssStyle.ADMIN_MESSAGE_POPUP.getStyleName());
        parentLayout = new VerticalLayout();
        dataTextLayout = new VerticalLayout();
        parentLayout.setMargin(true);
        parentLayout.setSpacing(true);
        dataTextLayout.setWidth("730px");

        service = subscriptionService;
        this.paymentService = paymentService;
        this.defaultInvoiceHandler = defalutInvoiceHandler;
        this.relationshipService = relationshipService;
        this.productRepository = productRepository;

        this.setWidth(popupWidth);
        this.setHeight(popupHeight);
        setCaption(user.getAlias() + "," + user.getEmail());
        this.user = user;
        createDatelayout();


    }

    private void createDatelayout() {
        recurranceLayout = new HorizontalLayout();
        dateField = new DateField("Zeitpunkt des Widerrufs setzen");
        dateField.setValue(new Date());
        calculateButton = new Button("Speichern");

        recurranceLayout.addComponent(dateField);
        recurranceLayout.setComponentAlignment(dateField, Alignment.TOP_LEFT);
        recurranceLayout.setSpacing(true);
        dataTextLayout.addComponent(recurranceLayout);
        dataTextLayout.addComponent(calculateButton);
        dataTextLayout.setSpacing(true);
        dataTextLayout.setMargin(true);
        dataTextLayout.setComponentAlignment(recurranceLayout, Alignment.MIDDLE_LEFT);
        if (paymentService.currentPayment(user)) {
            createRevocationFieldsLayout(true);
            calculateButton.setEnabled(false);
            dateField.setEnabled(false);
        }

        calculateButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                if(dateField.getValue()==null)
                {
                    Notification.show("Date field Can not be empty", Notification.Type.WARNING_MESSAGE);
                    return;
                }
                if (dateField.getValue() != null && paymentService.isEligibleForMoneyBack(user, dateField.getValue()) || true) {
                    createRevocationFieldsLayout(false);
                    calculateButton.setEnabled(false);
                    dateField.setEnabled(false);
                } else {
                    MessageBox.show("Not Eligible for money back");
                }
            }
        });

        setContent(parentLayout);
        parentLayout.addComponent(dataTextLayout);
    }

    private void createRevocationFieldsLayout(boolean bool) {

        VerticalLayout verticalLayout = new VerticalLayout();
        CheckBox checkBox = new CheckBox("Rücküberweisen als Vorkasse");
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        HorizontalLayout buttonLayout = new HorizontalLayout();
        Label label = new Label();

        abstractPayment = paymentService.currentAbsctractPayment(user);
        // final Label originalAmount = new Label(I18N.PAYMENTREFUNDPOPUP_CAPTION_PAYMENTAMOUNT.msg(abstractPayment.get().getAmount()));

        final BigDecimal minRefundAmount = abstractPayment.get().getAmount().getAmount().negate();
        final BigDecimal maxRefundAmount = BigDecimal.ZERO;
        final Label refundAmountLabel = new Label(I18N.PAYMENTREFUNDPOPUP_CAPTION_REFUNDAMOUNT.msg(minRefundAmount, maxRefundAmount));

        label.setValue("Rechnungsbetrag " + abstractPayment.get().getAmount() +" " +refundAmountLabel);

        //label.setValue("Rechnungsbetrag"+ decimal +"Ruckerstattungsbetrag");
        textField = createRefundAmountTextField(BigDecimal.ZERO, maxRefundAmount);
        BigDecimal revoteAmount = BigDecimal.ZERO;
        if (dateField.getValue() != null) {
            Long noOfViewedSuggestions = relationshipService.numberOfViewedSuggestions(user, dateField.getValue());
            revoteAmount = calculateRevokeAmont(noOfViewedSuggestions, abstractPayment.get());
        }
        Log.info("abstractPayment=========="+abstractPayment.get());
        Log.info("revoteAmount== " + revoteAmount);
        if (abstractPayment.get().getRevocationAmount() != null && bool == true) {
            textField.setConverter(BigDecimal.class);
            textField.setConvertedValue((abstractPayment.get().getRevocationAmount()));
            dateField.setConvertedValue(abstractPayment.get().getRevocationDate());
        } else {
            Log.info("revokeAmount========"+revoteAmount +" String value=========" + String.valueOf(revoteAmount));
            textField.setConverter(BigDecimal.class);
            textField.setConvertedValue(revoteAmount);
            //textField.setValue(revoteAmount.toString());
        }

        Label label1 = new Label("Berechnung erfolgte zum: " + LocalDateTime.now());
        label1.setWidth("390px");
        horizontalLayout.addComponent(label);
        horizontalLayout.addComponent(textField);
       // if (abstractPayment.get() instanceof ExternalPayment) {
            horizontalLayout.addComponent(checkBox);
        //}
        horizontalLayout.setSpacing(true);

        horizontalLayout.setComponentAlignment(label, Alignment.BOTTOM_LEFT);
        horizontalLayout.setComponentAlignment(textField, Alignment.BOTTOM_RIGHT);
        verticalLayout.addComponent(horizontalLayout);


        verticalLayout.addComponent(label1);

        Button revokeButton = new Button("Widerruf ausführen");
        revokeButton.setWidth("180px");
        Button extendButton = new Button("Widerrufsfrist verlängern");
        extendButton.setWidth("250px");
        Button recalculateButton = new Button("Neu berechnen");
        recalculateButton.setWidth("180px");

        buttonLayout.addComponent(revokeButton);
        buttonLayout.addComponent(extendButton);
        buttonLayout.addComponent(recalculateButton);
        buttonLayout.setComponentAlignment(revokeButton, Alignment.BOTTOM_LEFT);
        buttonLayout.setComponentAlignment(extendButton, Alignment.BOTTOM_CENTER);
        buttonLayout.setComponentAlignment(recalculateButton, Alignment.BOTTOM_RIGHT);
        horizontalLayout.setSpacing(true);

        recalculateButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                calculateButton.setEnabled(true);
                dateField.setEnabled(true);
                dataTextLayout.removeComponent(verticalLayout);
                dateField.setValue(null);
                //createRevocationFieldsLayout();
                // verticalLayout.setVisible(false);
                // verticalLayout.setEnabled(false);
                //dateField.setValue(null);

            }
        });

        revokeButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {

                MessageBox.show("Are you Sure want to refund " + textField.getValue() + " EUR ", MessageBox.MessageBoxButtons.YES_NO, dialogResult ->
                {
                    if (dialogResult.equals(MessageBox.DialogResult.YES))
                        refundPayment(user, checkBox.getValue());
                        cancelSubscrption();
                });
            }

        });

        extendButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                paymentService.updateRevocation(new BigDecimal(textField.getValue()), abstractPayment.get(), dateField.getValue());
                close();
            }
        });

        verticalLayout.addComponent(buttonLayout);
        verticalLayout.setSpacing(true);
        verticalLayout.setWidth("250px");
        verticalLayout.setHeight("250px");
        dataTextLayout.addComponent(verticalLayout);
        //parentLayout.addComponent(dataTextLayout);


    }

    private TextField createRefundAmountTextField(BigDecimal minRefundAmount, BigDecimal maxRefundAmount) {
        final TextField textField = ComponentFactory.getInstance().createField(TextField.class);
        textField.setConverter(BigDecimal.class);

        //final String validationMessage = I18N.PAYMENTREFUNDPOPUP_NOTIFICATION_INVALIDREFUNDAMOUNT.msg(minRefundAmount, maxRefundAmount);
        // final BigDecimalRangeValidator refundAmountValidator = new BigDecimalRangeValidator(validationMessage, minRefundAmount, maxRefundAmount);
        //refundAmountValidator.setMaxValueIncluded(false);

        //textField.addValidator(refundAmountValidator);
        //textField.setConvertedValue(minRefundAmount);
        textField.setRequired(true);

        return textField;
    }

    private BigDecimal calculateRevokeAmont(Long noOfViewedSuggestions, AbstractPayment abstractPayment) {
        Product product = productRepository.findProduct(user.getId());
        SubscriptionOffer offer = ((SubscriptionOffer) product);
        BigDecimal primaryAmount = BigDecimal.ZERO;
        BigDecimal secondaryAmount = BigDecimal.ZERO;

        Log.info("Suggestions== " + noOfViewedSuggestions, "Payment== " + abstractPayment.getAmount());
        if (product.getDtype().equals(Product.ProductType.UPGRADE_OFFER) && product.getProductType().UPGRADE_OFFER.equals(UpgradeType.DONATION)) {
            return product.getAmount().getAmount().negate();
        }
        if (abstractPayment != null && abstractPayment.getAmount() != null) {
            if (offer.getDuration() == 12 && offer.getDurationUnit() == DurationUnit.MONTHS) {
                if (noOfViewedSuggestions != 0) {
                    primaryAmount = abstractPayment.getAmount().getAmount().divide(new BigDecimal(3), 2, RoundingMode.HALF_UP);
                    BigDecimal amountForPercent = abstractPayment.getAmount().getAmount().subtract(primaryAmount);
                    Long percent = 100-(noOfViewedSuggestions * 100) / 60;
                    BigDecimal bigDecimal = new BigDecimal(percent);
                    BigDecimal percentageAmout = amountForPercent.multiply(bigDecimal);
                    secondaryAmount = percentageAmout.divide(new BigDecimal(100));
                    return primaryAmount.add(secondaryAmount).negate();
                } else {
                    return abstractPayment.getAmount().getAmount().negate();
                }
            } else if ((offer.getDuration() == 24 || offer.getDuration() == 36) && offer.getDurationUnit() == DurationUnit.MONTHS) {
                int topAmount = abstractPayment.getAmount().getAmount().intValue() - 89;
                primaryAmount = BigDecimal.valueOf(89).divide(new BigDecimal(3), 2, RoundingMode.HALF_UP);
                primaryAmount.add(BigDecimal.valueOf(topAmount));
                if (noOfViewedSuggestions != 0) {
                    BigDecimal amountForPercent = abstractPayment.getAmount().getAmount().subtract(primaryAmount);
                    Long percent = 100-(noOfViewedSuggestions * 100) / 60;
                    BigDecimal bigDecimal = new BigDecimal(percent);
                    BigDecimal percentageAmout = amountForPercent.multiply(bigDecimal);
                    secondaryAmount = percentageAmout.divide(new BigDecimal(100));
                    return primaryAmount.add(secondaryAmount).negate();
                }
                else {
                    return abstractPayment.getAmount().getAmount().negate();
                }
            }
        }

        return new BigDecimal(0).negate();
    }

    private void refundPayment(User user, Boolean checkBox) {
        AbstractPayment abstractPayment = paymentService.currentAbsctractPayment(user).get();
        final MonetaryAmount refundMonetaryAmount = new MonetaryAmount((BigDecimal) textField.getConvertedValue(), abstractPayment.getAmount().getCurrency());
        defaultInvoiceHandler.refundPayment(abstractPayment, refundMonetaryAmount, checkBox);
        close();
    }

    private void cancelSubscrption(){
        if(service.findCurrentSubscription(user).isPresent()) {
            service.cancelSubscription(service.findCurrentSubscription(user).get());
        }
    }
}