package de.binaerebauten.gleichklang.memberweb.view.popup;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.matching.Relationship.Affiliation;
import de.binaerebauten.gleichklang.core.model.media.MediaGallery;
import de.binaerebauten.gleichklang.core.model.media.MediaGallery_;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.ComboBoxUtils;
import de.binaerebauten.gleichklang.core.utils.DefaultI18N;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.FormPanel;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.core.view.popup.GenericPopup;
import de.binaerebauten.gleichklang.memberweb.view.I18N;

import java.util.Collection;
import java.util.Objects;

public class MediaGalleryPopup extends GenericPopup
{
	public interface MediaGalleryPopupCallback
	{
		void save(MediaGallery mediaGallery) throws ValidationException;
	}

	private final SaveHelper saveHelper;
	private final ComponentGroup<MediaGallery> mediaGalleryComponentGroup;
	private final MediaGalleryPopupCallback callback;
	private final CheckBox onlyForPositiveCheckBox;

	public MediaGalleryPopup(MediaGallery mediaGallery, Collection<RecommendationCategory> recommendationCategories, Collection<Relationship> relationships, MediaGalleryPopupCallback callback, DefaultI18N caption, Device device)
	{
		setCaption(caption.msg());
		setIcon(new ThemeResource("img/media.svg"));

		Objects.requireNonNull(mediaGallery);
		Objects.requireNonNull(callback);
		
		this.callback = callback;
		this.mediaGalleryComponentGroup = new ComponentGroup<>(MediaGallery.class);
		this.saveHelper = new SaveHelper(this::commit, this::save, true);
		this.onlyForPositiveCheckBox = createOnlyForPositiveCheckBox();

		this.setDraggable(false);

		final VerticalLayout layout = new VerticalLayout();

		final Component mediaForm = createMediaGalleryComponent(recommendationCategories, relationships);
        addFooterComponent(saveHelper.getSaveButton());

		layout.addComponent(mediaForm);
		layout.setComponentAlignment(mediaForm, Alignment.MIDDLE_CENTER);

		createResponsiveLayout(device);
		this.setStyleName(CssStyle.MEDIA_GALLERY_POPUP.getStyleName());
		setPopupContent(layout);

		setDraggable(false);

		
		// set data source later, so that all components are initialized correctly and change listener working
		this.mediaGalleryComponentGroup.setItemDataSource(mediaGallery);
		onlyForPositiveCheckBox.setValue(Affiliation.POSITIVE.equals(mediaGallery.getVisibleAffiliation()));
		
		saveHelper.addFields(mediaGalleryComponentGroup);
	}
	
	private void commit()
	{
		final MediaGallery mediaGallery = mediaGalleryComponentGroup.getItemDataSource().getBean();
		final Affiliation visibleAffiliation = onlyForPositiveCheckBox.getValue() ? Affiliation.POSITIVE : null;
		mediaGallery.setVisibleAffiliation(visibleAffiliation);
	}
	
	private void save() throws ValidationException
	{
		callback.save(mediaGalleryComponentGroup.getItemDataSource().getBean());
		close();
	}
	
	private Component createMediaGalleryComponent(Collection<RecommendationCategory> recommendationCategories, Collection<Relationship> relationships)
	{
		final FormPanel formPanel = new FormPanel();
		formPanel.setWidth("80%");

		final CheckBox secretCheckBox = mediaGalleryComponentGroup.buildAndBind(I18N.MEDIAGALLERYPOPUP_CAPTION_SECRET.msg(), CheckBox.class, MediaGallery_.secret);

		formPanel.addComponent(mediaGalleryComponentGroup.buildAndBind(I18N.MEDIAGALLERYPOPUP_CAPTION_NAME.msg(), MediaGallery_.name));
		formPanel.addComponent(secretCheckBox);

		final ComboBox visibleCategoryComponent = mediaGalleryComponentGroup.buildAndBind(I18N.MEDIAGALLERY_VISIBLE_CATEGORY.msg(), ComboBox.class, MediaGallery_.visibleCategory);
		visibleCategoryComponent.setInputPrompt(I18N.MEDIAGALLERY_VISIBLE_INPUTPROMPT.msg());
		visibleCategoryComponent.setNullSelectionAllowed(true);
		if(recommendationCategories.size() == 1)
		{
			visibleCategoryComponent.setEnabled(false);
			visibleCategoryComponent.select(recommendationCategories.iterator().next());
		}

		for (RecommendationCategory r : recommendationCategories)
		{
			ComboBoxUtils.addEntry(visibleCategoryComponent, r);
		}
		
		final TwinColSelect relationshipsTwinColSelect = mediaGalleryComponentGroup.buildAndBind(I18N.MEDIAGALLERY_RELATIONSHIPS.msg(), TwinColSelect.class, MediaGallery_.visibleRelationships);
		relationshipsTwinColSelect.setContainerDataSource(new BeanItemContainer<>(Relationship.class, relationships));
		relationshipsTwinColSelect.setNullSelectionAllowed(true);
		relationshipsTwinColSelect.setItemCaptionPropertyId(Relationship.NAME);
		relationshipsTwinColSelect.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		relationshipsTwinColSelect.setLeftColumnCaption(I18N.MEDIAGALLERY_RELATIONSHIPS_HIDDEN.msg());
		relationshipsTwinColSelect.setRightColumnCaption(I18N.MEDIAGALLERY_RELATIONSHIPS_ENABLED.msg());

		formPanel.addComponent(visibleCategoryComponent);
		formPanel.addComponent(onlyForPositiveCheckBox);
		secretCheckBox.addValueChangeListener(event ->
		{
			if (secretCheckBox.getValue())
			{
				formPanel.addComponent(relationshipsTwinColSelect);
				formPanel.removeComponent(visibleCategoryComponent);
				formPanel.removeComponent(onlyForPositiveCheckBox);
			}
			else
			{
				formPanel.removeComponent(relationshipsTwinColSelect);
				formPanel.addComponent(visibleCategoryComponent);
				formPanel.addComponent(onlyForPositiveCheckBox);
			}
		});

		return formPanel;
	}
	
	private CheckBox createOnlyForPositiveCheckBox()
	{
		final CheckBox onlyForPositiveCheckBox = ComponentFactory.getInstance().createField(CheckBox.class, I18N.MEDIAGALLERYPOPUP_CAPTION_ONLYFORPOSITIVE.msg());
		saveHelper.addFields(onlyForPositiveCheckBox);
		return onlyForPositiveCheckBox;
	}

	@Override
	public void onDeviceChanged(Device device)
	{
		createResponsiveLayout(device);
	}

	private void createResponsiveLayout(Device device)
	{
		if (device == Device.DESKTOP)
			createDesktopView();
		else if (device == Device.TABLET)
			createTabletView();
		else if (device == Device.MOBILE)
			createMobileView();

	}

	private void createMobileView()
	{
		this.setWidth("95%");
		this.setHeight("80%");
		this.center();
	}

	private void createTabletView()
	{
		this.setWidth("70%");
		this.setHeight("80%");
		this.center();
	}

	private void createDesktopView()
	{
		this.setWidth("50%");
		this.setHeight("80%");
		this.center();
	}
}
