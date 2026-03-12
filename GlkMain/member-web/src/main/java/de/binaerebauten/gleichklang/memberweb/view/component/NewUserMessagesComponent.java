package de.binaerebauten.gleichklang.memberweb.view.component;

import com.vaadin.server.*;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import de.binaerebauten.gleichklang.core.initializer.AppUI;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.utils.AppUrlBuilder;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.view.I18N;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.*;

/**
 * Created by rgoerner on 19.05.16.
 */

public class NewUserMessagesComponent extends CustomComponent
{
    public interface NewUserMessagesHandler
    {
        void onMessageClicked(RecommendationCategory recommendationCategory);
    }

    public interface NewMatchesHandler
    {
        void onMatchesClicked(RecommendationCategory recommendationCategory);
    }

    private final VerticalLayout layout;
    private final NewUserMessagesHandler messagesHandler;
    private final NewMatchesHandler matchesHandler;
    private final VerticalLayout header;
    private final VerticalLayout subscriptionInfoLayout;
    private final HorizontalLayout overviewWrapper;

    private RecommendationCategory activeCategory;
    private List<RecommendationCategory> categories;
    private List<RecommendationCategory> visibleCategories;
    private Map<RecommendationCategory, HorizontalLayout> overview;
    private Map<RecommendationCategory, Long> suggestionsByCategory;
    private Map<RecommendationCategory, Long> messagesByCategory;
    private Map<String, Long> suggestionsByCategoryUser;
    private Map<String, Long> messagesByCategoryUser;
    private UserService userService;
    private UserRepository userRepository;
    /*private User currentUser;*/


    int currentReccatPosition;
    MyAccountComponent.GotoActivateCategoryHandler gotoActivateCategoryHandler;
    public NewUserMessagesComponent(NewUserMessagesHandler messagesHandler,  NewMatchesHandler macthesHandler,MyAccountComponent.GotoActivateCategoryHandler gotoActivateCategoryHandler)
    {
        Objects.requireNonNull(messagesHandler);
        Objects.requireNonNull(macthesHandler);

        this.userService= AppUI.getApplicationContext().getBean(UserService.class);
        this.userRepository= AppUI.getApplicationContext().getBean(UserRepository.class);

        this.messagesHandler = messagesHandler;
        this.matchesHandler = macthesHandler;
        this.gotoActivateCategoryHandler = gotoActivateCategoryHandler;

        layout = new VerticalLayout();
        layout.setWidth("100%");
        layout.setHeight("255px");
        layout.addStyleName(CssStyle.PANEL_WRAPPER.getStyleName());

        header = new VerticalLayout();
        header.setHeight("50px");

        subscriptionInfoLayout = new VerticalLayout();

        overviewWrapper = new HorizontalLayout();
        overviewWrapper.setHeight("205px");
        overviewWrapper.setWidth("100%");
        overviewWrapper.setStyleName(CssStyle.OVERVIEW_WRAPPER.getStyleName());

        overview = new HashMap<>();
        suggestionsByCategory = new HashMap<>();
        messagesByCategory = new HashMap<>();
        suggestionsByCategoryUser = new HashMap<>();
        messagesByCategoryUser = new HashMap<>();

        currentReccatPosition = 0;

        createLayout();

        setCompositionRoot(layout);

    }

    public void createLayout()
    {
        layout.removeAllComponents();

        layout.addComponents(header, overviewWrapper);
        layout.setExpandRatio(header, 0.196078431f);
        layout.setExpandRatio(overviewWrapper, 0.803921569f);
    }


    public void setNewSuggestions(Map<RecommendationCategory, Long> suggestions)
    {
        suggestionsByCategory = suggestions;
    }

    public void setNewSuggestionsUser(Map<String, Long> suggestions)
    {
        suggestionsByCategoryUser = suggestions;
    }

    private void createRecommendationCategoryCarousel() {
        header.removeAllComponents();

        final HorizontalLayout carouselWrapper = new HorizontalLayout();
        carouselWrapper.setSizeFull();


        if (categories != null){
            if (categories.size() ==0){
                String str =  visibleCategories.size() == 1 ? visibleCategories.get(0).getName() :I18N.NEWUSER_MESSAGES_ACTIVATED.msg();
                final Button subscriptionButton = new Button(I18N.NEWUSER_MESSAGES_ACTIVATE.msg() + str +" " +I18N.NEWUSER_MESSAGES_ACTIVATES.msg());
                //final Button subscriptionButton = new Button(I18N.NEWUSER_MESSAGES_ACTIVATE.msg());

                subscriptionButton.addClickListener(event -> gotoActivateCategoryHandler.onGotoActivateCategoryClicked());
                subscriptionButton.setIcon(FontAwesome.CHEVRON_RIGHT);
                carouselWrapper.setStyleName(CssStyle.OVERVIEW_WRAPPER.getStyleName());
                carouselWrapper.addComponent(subscriptionButton);
                header.addComponent(carouselWrapper);
            } else if(categories.size() > 0) {
                final Label currentReccatName = new Label();
                carouselWrapper.setStyleName(CssStyle.RECCAT_CAROUSEL_WRAPPER.getStyleName());

                carouselWrapper.addComponents(currentReccatName);
               // changeCategory(categories.get(currentReccatPosition));
                header.addComponent(carouselWrapper);
            }
        }


    }

    private void updateContent(Button right, Button left, Label reccatName,
                               Button leftPreview, Button rightPreview, int nextReccatPosition)
    {
        right.setEnabled(nextReccatPosition < categories.size()-1);
        rightPreview.setEnabled(nextReccatPosition < categories.size()-1);
        left.setEnabled(nextReccatPosition > 0);
        leftPreview.setEnabled(nextReccatPosition > 0);

        if (right.isEnabled())
        {
            if (getSumOfNewsByReccat(categories.get(nextReccatPosition+1)) > 0)
                rightPreview.setCaption("" + getSumOfNewsByReccat(categories.get(nextReccatPosition + 1)));

            rightPreview.setIcon(categories.get(nextReccatPosition+1).getIcon());
        }
        else
        {
            rightPreview.setCaption("");
            rightPreview.setIcon(null);
        }

        if (left.isEnabled())
        {
            if (getSumOfNewsByReccat(categories.get(nextReccatPosition-1)) > 0)
                leftPreview.setCaption("" + getSumOfNewsByReccat(categories.get(nextReccatPosition - 1)));

            leftPreview.setIcon(categories.get(nextReccatPosition-1).getIcon());
        }
        else
        {
            leftPreview.setCaption("");
            leftPreview.setIcon(null);
        }

        reccatName.setValue(categories.get(nextReccatPosition).getName());
        reccatName.setIcon(categories.get(nextReccatPosition).getIcon());
    }

    private int getSumOfNewsByReccat(RecommendationCategory category)
    {
        int result = 0;

        for (Map.Entry suggestions : suggestionsByCategory.entrySet())
        {
            if (suggestions.getKey() == category)
                result += (Long) suggestions.getValue();
        }

        for (Map.Entry messages : messagesByCategory.entrySet())
        {
            if (messages.getKey() == category)
                result += (Long) messages.getValue();
        }

        return result;
    }


    public void setUserRecCats(Set<RecommendationCategory> reccats)
    {
        this.categories = new ArrayList<>(reccats);
    }

    public void setVisibleCategories(Set<RecommendationCategory> visibleCategories)
    {
        this.visibleCategories = new ArrayList<>(visibleCategories);
    }

    public void setNewMessages(HashMap<RecommendationCategory, Long> map)
    {
        messagesByCategory = map;
        createRecommendationCategoryCarousel();
        initMessagesAndSuggestions();
    }

    public void setNewMessagesUser(HashMap<String, Long> map)
    {
        messagesByCategoryUser = map;
        createRecommendationCategoryCarousel();
        initMessagesAndSuggestions();
    }
    private void initMessagesAndSuggestions()
    {
        overviewWrapper.removeAllComponents();

        Long resultM = 0L;
        Long resultS = 0L;
        //create newMessages and suggestions layout
        for(Map.Entry<String, Long> entry : suggestionsByCategoryUser.entrySet())
        {
            final HorizontalLayout overwiew = new HorizontalLayout();
            overwiew.setSizeFull();

            final VerticalLayout suggestionsWrapper= new VerticalLayout();
            suggestionsWrapper.setSizeFull();
            suggestionsWrapper.setStyleName(CssStyle.MATCH_WRAPPER.getStyleName());

            resultS = (Long) entry.getValue();

            final Component suggestions = createLayout(resultS, false);
            suggestionsWrapper.addComponent(suggestions);

            overwiew.addComponent(suggestionsWrapper);
            overwiew.setComponentAlignment(suggestionsWrapper, Alignment.MIDDLE_CENTER);


            for (Map.Entry<String, Long> e : messagesByCategoryUser.entrySet())
            {
                resultM = (Long) e.getValue();

                VerticalLayout messagesWrapper = new VerticalLayout();
                messagesWrapper.setSizeFull();
                messagesWrapper.setStyleName(CssStyle.MATCH_WRAPPER.getStyleName());

                final Component messages = createLayout(resultM, true);
                messagesWrapper.addComponent(messages);

                overwiew.addComponent(messagesWrapper);
                overwiew.setComponentAlignment(messagesWrapper, Alignment.MIDDLE_CENTER);
           }

                overviewWrapper.addComponent(overwiew);
                overviewWrapper.setComponentAlignment(overwiew, Alignment.MIDDLE_CENTER);
        }
    }

   private void changeCategory(RecommendationCategory category)
    {
        if (activeCategory != category)
        {
            overviewWrapper.removeAllComponents();
            for(Map.Entry<RecommendationCategory, HorizontalLayout> entry : overview.entrySet())
            {
                if (entry.getKey() == category)
                {
                    Component overview = entry.getValue();
                    overviewWrapper.addComponent(overview);
                    overviewWrapper.setComponentAlignment(overview, Alignment.MIDDLE_CENTER);
                }
            }

            activeCategory = category;
        }
    }

    private Component createLayout(Long nr, boolean isNewMessages )
    {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        final User currentUser= userRepository.findById(userService.getCurrentUser().getId());

        HorizontalLayout numberWrapper = new HorizontalLayout();
        numberWrapper.setWidth("124px");
        numberWrapper.setHeight("124px");

        if (isNewMessages)
        {
            numberWrapper.addStyleName(CssStyle.ICON_NEW_MESSAGES.getStyleName());
            numberWrapper.addLayoutClickListener(event -> messagesHandler.onMessageClicked(null));
        }
        else
        {
            numberWrapper.addStyleName(CssStyle.ICON_NEW_SUGGESTIONS.getStyleName());
            numberWrapper.addLayoutClickListener(event -> matchesHandler.onMatchesClicked(null));
        }

        Button number = new Button();

        if (nr != null && nr > 0 )
        {
            number.addStyleName(CssStyle.RECCAT_ICON.getStyleName());
            number.setCaption(nr.toString());
            number.setEnabled(false);
        }
        else
            number.addStyleName(CssStyle.RECCAT_ICON_EMPTY.getStyleName());

        numberWrapper.addComponent(number);
        layout.addComponent(numberWrapper);

        if (currentUser!=null && currentUser.getCategories().size()==0 )
        {
            numberWrapper.setVisible(false);
        }


        layout.setComponentAlignment(numberWrapper, Alignment.MIDDLE_CENTER);

        final Label label = new Label();

        if (isNewMessages)
            label.setValue(I18N.NEWUSER_MESSAGES_CREATE.msg());
        else
            label.setValue(I18N.NEWUSER_MESSAGES_SUGGESTIONS.msg());

        label.addStyleName(CssStyle.RECCAT_NAME.getStyleName());
        layout.addComponent(label);

        if (currentUser!=null && currentUser.getCategories().size()==0 )
        {
            label.setVisible(false);
        }
        layout.setExpandRatio(numberWrapper, 0.8f);
        layout.setExpandRatio(label, 0.2f);

        return layout;



    }

}

