package de.binaerebauten.gleichklang.memberweb.view.component;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.media.Footprint;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.view.popup.FootprintPopup;
import de.binaerebauten.gleichklang.memberweb.view.popup.I18N;
import de.binaerebauten.gleichklang.memberweb.view.popup.RelationshipPopup;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static de.binaerebauten.gleichklang.memberweb.view.I18N.RELATIONSHIP_VIEW_FOOTPRINT_NODATE;

/**
 * Created by rgoerner on 18.10.16.
 */
public class FootprintComponent extends CustomComponent
{
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	private FootprintPopup footprintPopup = null;
	
	public FootprintComponent(Relationship relationship, RelationshipPopup.RelationshipData relationshipData, ClientInformation.Device device)
    {
		this.setCompositionRoot(createFootprintComponent(relationship, relationshipData, device));
    }

    public HorizontalLayout createFootprintComponent(Relationship relationship, RelationshipPopup.RelationshipData relationshipData, ClientInformation.Device device)
    {
        final VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setStyleName(CssStyle.FOOTPRINT_WRAPPER.getStyleName());

        final HorizontalLayout footprintsWrapper = new HorizontalLayout();
        footprintsWrapper.setSizeFull();
        footprintsWrapper.addStyleName(CssStyle.FOOTPRINT_TO_USER.getStyleName());

        final HorizontalLayout footprintToCurrentUser = new HorizontalLayout();
        final HorizontalLayout footprintFromCurrentUser = new HorizontalLayout();

        footprintToCurrentUser.addComponent(createFootprintLayout(relationship, true, relationshipData.getFootprint(), relationshipData, device));
        footprintFromCurrentUser.addComponent(createFootprintLayout(relationship, false, relationship.getFootprint(), relationshipData, device));

        footprintFromCurrentUser.setVisible(false);

        final Button leftButton = new Button();
        leftButton.setIcon(FontAwesome.CHEVRON_LEFT);
        leftButton.setCaption(I18N.RELATIONSHIPPOPUP_FOOTPRINT_FROM_TARGET_USER.msg());
        leftButton.addStyleName("left");
        leftButton.setEnabled(false);

        final Button rightButton = new Button();
        rightButton.setIcon(FontAwesome.CHEVRON_RIGHT);
        rightButton.setCaption(I18N.RELATIONSHIPPOPUP_FOOTPRINT_INPUTPROMPT.msg());
        rightButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_RIGHT);
        rightButton.addStyleName("right");

        rightButton.addClickListener(event ->
        {
            rightButton.setEnabled(false);
            leftButton.setEnabled(true);
            footprintToCurrentUser.setVisible(false);
            footprintFromCurrentUser.setVisible(true);
            footprintsWrapper.removeStyleName(CssStyle.FOOTPRINT_TO_USER.getStyleName());
            footprintsWrapper.addStyleName(CssStyle.FOOTPRINT_FROM_USER.getStyleName());
        });

        leftButton.addClickListener(event ->
        {
            rightButton.setEnabled(true);
            leftButton.setEnabled(false);
            footprintToCurrentUser.setVisible(true);
            footprintFromCurrentUser.setVisible(false);
            footprintsWrapper.removeStyleName(CssStyle.FOOTPRINT_FROM_USER.getStyleName());
            footprintsWrapper.addStyleName(CssStyle.FOOTPRINT_TO_USER.getStyleName());
        });

        rightButton.setVisible(relationshipData.getFootprint() != null);
        leftButton.setVisible(relationshipData.getFootprint() != null);
        footprintToCurrentUser.setVisible(relationshipData.getFootprint() != null);
        footprintFromCurrentUser.setVisible(relationshipData.getFootprint() == null);

        footprintsWrapper.addComponents(leftButton, footprintToCurrentUser, footprintFromCurrentUser, rightButton);
        footprintsWrapper.setComponentAlignment(leftButton, Alignment.MIDDLE_LEFT);
        footprintsWrapper.setComponentAlignment(footprintToCurrentUser, Alignment.MIDDLE_CENTER);
        footprintsWrapper.setComponentAlignment(footprintFromCurrentUser, Alignment.MIDDLE_CENTER);
        footprintsWrapper.setComponentAlignment(rightButton, Alignment.MIDDLE_RIGHT);
        footprintsWrapper.setExpandRatio(leftButton, device != ClientInformation.Device.DESKTOP ? 0.15f : 0.3f);
        footprintsWrapper.setExpandRatio(footprintToCurrentUser, device != ClientInformation.Device.DESKTOP ? 0.7f : 0.4f);
        footprintsWrapper.setExpandRatio(footprintFromCurrentUser, device != ClientInformation.Device.DESKTOP ? 0.7f : 0.4f);
        footprintsWrapper.setExpandRatio(rightButton, device != ClientInformation.Device.DESKTOP ? 0.15f : 0.3f);

        layout.addComponent(footprintsWrapper);

        return footprintsWrapper;
    }

    private Component createFootprintLayout(Relationship relationship, boolean isTargetUser, Footprint footprint, RelationshipPopup.RelationshipData relationshipData, ClientInformation.Device device)
    {

        final VerticalLayout wrapper = new VerticalLayout();
        wrapper.setSizeFull();

        final Button setFootprint = new Button();
        final Button deleteFootprint = new Button();
        deleteFootprint.setIcon(FontAwesome.TRASH_O);

        final Image image = new Image();
        image.setHeight(65, Sizeable.Unit.PIXELS);
        final Label footprintName = new Label();
        footprintName.setStyleName(CssStyle.FOOTPRINT_LABEL.getStyleName());

        if (footprint != null)
        {
            image.setSource(new ThemeResource(footprint.getPath()));

            if (isTargetUser)
            {
                final String dateString = relationshipData.getFootprintDate() == null ? RELATIONSHIP_VIEW_FOOTPRINT_NODATE.msg() : relationshipData.getFootprintDate().format(FORMATTER);
                image.setDescription(I18N.RELATIONSHIPPOPUP_CAPTION_FOOTPRINT_RECEIVED.msg(dateString, relationshipData.getFootprint().getName()));
            }
            else
            {
                final String dateString = relationship.getFootprintDate() == null ? RELATIONSHIP_VIEW_FOOTPRINT_NODATE.msg() : relationship.getFootprintDate().format(FORMATTER);
                image.setDescription(I18N.RELATIONSHIPPOPUP_CAPTION_FOOTPRINT_SENT.msg(dateString, relationship.getFootprint().getName()));
            }

            footprintName.setValue(footprint.getName());
            setFootprint.setIcon(FontAwesome.PENCIL);
            setFootprint.setCaption(I18N.RELATIONSHIPPOPUP_CHANGE_FOOTPRINT_BUTTON.msg());
            deleteFootprint.setCaption(I18N.RELATIONSHIPPOPUP_DELETE_FOOTPRINT_BUTTON.msg());
        }
        else
        {
            image.setVisible(false);
            footprintName.setValue(I18N.RELATIONSHIPPOPUP_ADD_FOOTPRINT_LABEL.msg());
            footprintName.setVisible(true);
            setFootprint.setIcon(FontAwesome.PLUS_CIRCLE);
            setFootprint.setCaption("");
        }

        final Label sender = new Label();
        sender.setStyleName(CssStyle.FOOTPRINT_SENDER.getStyleName());
        sender.setValue(I18N.RELATIONSHIPPOPUP_PREFIX_FROM.msg() + " " + relationshipData.getTargetUser().getAlias());

        final HorizontalLayout buttonWrapper = new HorizontalLayout();

        setFootprint.setStyleName(CssStyle.CHANGE_FOOTPRINT_BUTTON.getStyleName());
        setFootprint.addClickListener(event ->
        {
            if (footprintPopup == null)
            {
				footprintPopup = new FootprintPopup(new FootprintPopup.FootprintClickListener() {

					@Override
					public void onFootprintClicked(Footprint footprint) {
						// only if the footprint is already viewed the user must be notified about the new footprint
						// inverse equals because of initial state of null
					    if(!Objects.equals(Boolean.FALSE, relationship.getFootprintViewed())) relationship.setFootprintNotified(false);
						relationship.setFootprint(footprint);
						relationship.setFootprintViewed(false);
						relationship.setFootprintDate(LocalDateTime.now());
						image.setSource(new ThemeResource(footprint.getPath()));
						image.setDescription(LocalDateTime.now().format(FORMATTER));
						footprintName.setValue(footprint.getName());
						image.setVisible(true);
						footprintName.setVisible(true);
						deleteFootprint.setVisible(true);
						deleteFootprint.setCaption(I18N.RELATIONSHIPPOPUP_DELETE_FOOTPRINT_BUTTON.msg());
						setFootprint.setCaption(I18N.RELATIONSHIPPOPUP_CHANGE_FOOTPRINT_BUTTON.msg());
						setFootprint.setIcon(FontAwesome.PENCIL);
					}
				});

                if (device == ClientInformation.Device.MOBILE)
                    footprintPopup.setWidth("100%");
                else
                    footprintPopup.setWidth("50%");
    
                footprintPopup.addCloseListener(closed -> footprintPopup = null);
                footprintPopup.show();
            }
        });

        deleteFootprint.setVisible(footprint != null);
        deleteFootprint.addClickListener(event ->
        {
            image.setSource(new ThemeResource("img/icon_add_footprint.png"));
            image.setVisible(false);
            footprintName.setValue(I18N.RELATIONSHIPPOPUP_ADD_FOOTPRINT_LABEL.msg());
            footprintName.setVisible(true);
            relationship.setFootprint(null);
            deleteFootprint.setVisible(false);
            setFootprint.setIcon(FontAwesome.PLUS_CIRCLE);
            setFootprint.setCaption("");
        });

        buttonWrapper.addComponents(setFootprint, deleteFootprint);

        if (isTargetUser)
        {
            wrapper.addComponents(image, footprintName, sender);
            wrapper.setComponentAlignment(sender, Alignment.MIDDLE_CENTER);
        }
        else
        {
            wrapper.addComponents(image, footprintName, buttonWrapper);
            wrapper.setComponentAlignment(buttonWrapper, Alignment.MIDDLE_CENTER);
        }

        wrapper.setComponentAlignment(image, Alignment.MIDDLE_CENTER);
        wrapper.setComponentAlignment(footprintName, Alignment.MIDDLE_CENTER);


        return wrapper;
    }
	
	public void close()
	{
		if(footprintPopup != null) footprintPopup.close();
	}
}
