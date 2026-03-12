package de.binaerebauten.gleichklang.adminweb.view.component;

import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.adminweb.view.I18N;

import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.service.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class UserResetBar extends CustomComponent
{
    public interface UserResetBarListener
    {
        void resetUserLogin();
    }

    public interface UserUnblockListner
    {
        void unblockUser();

    }

    public interface AbuserLoginListener
    {
        void abuserLogin(String abuserLgin);
    }

    public interface ReminderLoginListener
    {
        void reminderLogin(User user);
    }

    private final HorizontalLayout layout;
    private  UserResetBarListener listener;
    private UserUnblockListner unblockListner;
    private AbuserLoginListener abuserLoginListener;
    private ReminderLoginListener reminderLoginListener;
    private String abuseUserMessageBody;
    private final Map<Component, Supplier<Boolean>> components = new HashMap<>();
    private User user = null;

    public UserResetBar(String s){
        layout =  new HorizontalLayout();
        layout.setSpacing(true);
        layout.setSizeFull();
        layout.setWidthUndefined();
        VerticalLayout v = new VerticalLayout();
        HorizontalLayout h = new HorizontalLayout();
        h.addComponent(addButton(I18N.USERMANAGE_ACTION_RESETOGINS.msg(), event -> listener.resetUserLogin()));
        h.addComponent(addButton("Unblock", event -> unblockListner.unblockUser(), userBlocked()));
        h.addComponent(addButton("Reminder", event -> reminderLoginListener.reminderLogin(user)));
        if(s!=null && (s.contains("Abuse") || s.contains("Missbrauch"))){
            h.addComponent(addButton("Abuser Login", event -> abuserLoginListener.abuserLogin(this.getAbuseUserMessageBody())));
        }
        v.addComponent(h);
        layout.addComponent(v);
        //addButton(I18N.USERMANAGE_ACTION_RESETOGINS.msg(), event -> listener.resetUserLogin());
        setCompositionRoot(layout);
    }
    private Button addButton(String caption, Button.ClickListener listener)
    {
        return addButton(caption, listener, defaultEnabler());
    }
    private Button addButton(String caption, Button.ClickListener listener, Supplier<Boolean> enabler)
    {
        final Button button = new Button(caption, listener);
        addComponent(button, enabler);

        return button;
    }
    private void addComponent(Component component, Supplier<Boolean> enabler)
    {
        component.setEnabled(true);

        components.put(component, enabler);
        layout.addComponent(component);
        layout.setComponentAlignment(component, Alignment.TOP_LEFT);
    }
    private Supplier<Boolean> defaultEnabler()
    {
        return () -> user != null && listener != null;
    }

    public void setListener(UserResetBarListener listener)
    {
        this.listener = listener;
       // refresh();
    }

    public void setListener(UserUnblockListner listener)
    {
        this.unblockListner = listener;
        // refresh();
    }

    public void setListener(AbuserLoginListener listener)
    {
        this.abuserLoginListener = listener;
        // refresh();
    }

    public void setListener(ReminderLoginListener listener)
    {
        this.reminderLoginListener = listener;
        // refresh();
    }


    private Supplier<Boolean> userBlocked()
    {
        return () -> defaultEnabler().get() && user.isBlocked();
    }

    public  void setAbuseUserMessageBody(String abuseUserMessageBody){
        this.abuseUserMessageBody=abuseUserMessageBody;
    }

    public String getAbuseUserMessageBody(){
        return this.abuseUserMessageBody;
    }

    private Supplier<Boolean> isAbuserTab(){
        return () -> false;
    }
}