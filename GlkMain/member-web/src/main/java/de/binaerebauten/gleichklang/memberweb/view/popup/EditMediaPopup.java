package de.binaerebauten.gleichklang.memberweb.view.popup;

import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Image;
import de.binaerebauten.gleichklang.core.model.media.Avatar;
import de.binaerebauten.gleichklang.core.model.media.MediaGallery;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.service.file.MediaUploadFile;
import de.binaerebauten.gleichklang.core.utils.ImageUtil;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.core.view.popup.GenericPopup;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by rgoerner on 10.04.17.
 */
public class EditMediaPopup extends GenericPopup
{
    public interface RotateMediaCallback
    {
        void refreshGallery(MediaUploadFile file);
    }

    public interface SaveAvatarsCallback
    {
        void saveRotatedAvatars(BufferedImage rotatedImage, List<Avatar> avatars);
    }

    public interface DescriptionChangedCallback
    {
        void saveDescription(MediaUploadFile file);
    }

    public interface SetAsAvatarCallback
    {
        void saveAsAvatar(File file, BufferedImage image, RecommendationCategory category, MediaGallery mediaGallery);
    }

    private Image image;
    private Image newImage;
    private BufferedImage rotatedImage;
    private BufferedImage inputImage;

    private RotateMediaCallback rotateMediaCallback;
    private DescriptionChangedCallback descriptionChangedCallback;
    private SetAsAvatarCallback setAsAvatarCallback;
    private SaveAvatarsCallback saveAvatarsCallback;

    private final Set<RecommendationCategory> categories;
    private final List<CheckBox> avatarForCategory;
    private final List<Avatar> avatars;

    private List<Avatar> avatarsToRotate;

    public EditMediaPopup(MediaUploadFile mediaUploadFile, Set<RecommendationCategory> categories, List<Avatar> avatars)
    {
        this.newImage = null;
        this.rotateMediaCallback = null;
        this.descriptionChangedCallback = null;
        this.setAsAvatarCallback = null;
        this.saveAvatarsCallback = null;

        this.categories = categories;
        this.avatars = avatars;

        avatarsToRotate = new ArrayList<>();

        try
        {
            inputImage = ImageIO.read(new File(mediaUploadFile.getPath().toString()));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        avatarForCategory = new ArrayList<CheckBox>();

        final Component layout = createEditMediaLayout(mediaUploadFile);

        setPopupContent(layout);
        addStyleName(CssStyle.EDIT_MEDIA_POPUP.getStyleName());
        setCaption(I18N.EDIT_MEDIA_POPUP_CAPTION.msg());

    }

    private VerticalLayout createEditMediaLayout(MediaUploadFile mediaUploadFile)
    {
        final VerticalLayout layout = new VerticalLayout();

        final HorizontalLayout picWrapper = new HorizontalLayout();
        picWrapper.setStyleName(CssStyle.ROTATE_MEDIA_WRAPPER.getStyleName());
        picWrapper.setWidth("100%");

        final HorizontalLayout descriptionWrapper = new HorizontalLayout();
        descriptionWrapper.setWidth("100%");

        final  TextField description = new TextField();
        description.setStyleName(CssStyle.MEDIA_DESCRIPTION_WRAPPER.getStyleName());
        description.setMaxLength(255);
        description.setWidth("100%");
        description.setCaption(I18N.EDIT_MEDIA_POPUP_DESCRIPTION.msg());
        descriptionWrapper.addComponent(description);

        if (mediaUploadFile.getFileEntity().getDescription() != null)
            description.setValue(mediaUploadFile.getFileEntity().getDescription());

        final HorizontalLayout btnWrapper = new HorizontalLayout();
        btnWrapper.setStyleName(CssStyle.EDIT_MEDIA_BTN_WRAPPER.getStyleName());
        btnWrapper.setWidth("100%");

        image = createImage(getImageResource(mediaUploadFile));

        final Button rotate90 = new Button(I18N.EDIT_MEDIA_POPUP_ROTATE_BUTTON.msg());
        rotate90.setIcon(FontAwesome.ROTATE_LEFT);
        btnWrapper.addComponent(rotate90);
        btnWrapper.setComponentAlignment(rotate90, Alignment.MIDDLE_CENTER);
        rotate90.addClickListener(event ->
        {
            if (rotatedImage != null)
                inputImage = rotatedImage;

            rotatedImage = ImageUtil.rotate90(inputImage);
            newImage = new Image("", ImageUtil.createStreamResource(rotatedImage, mediaUploadFile.getPath().toString()));

            //avoid "jumping" of ui on component replacement
            picWrapper.setHeight("300px");
            picWrapper.removeAllComponents();
            picWrapper.addComponent(newImage);
            picWrapper.setComponentAlignment(newImage, Alignment.MIDDLE_CENTER);
            replaceImage(newImage);

        });


        final VerticalLayout setAsAvatarWrapper = new VerticalLayout();
        setAsAvatarWrapper.setStyleName(CssStyle.SET_AS_AVATAR_WRAPPER.getStyleName());
        setAsAvatarWrapper.setCaption(I18N.EDIT_MEDIA_POPUP_SET_AS_AVATAR.msg());

        for (RecommendationCategory r : categories)
        {
            final CheckBox chooseAsAvatar = new CheckBox();
            chooseAsAvatar.setCaption(r.getName());
            chooseAsAvatar.setIcon(r.getIcon());
            chooseAsAvatar.setData(r);
            setAsAvatarWrapper.addComponent(chooseAsAvatar);
            avatarForCategory.add(chooseAsAvatar);

            for (Avatar a : avatars)
            {
                if (a != null && a.getFile() != null && a.getFile().getName().equals(mediaUploadFile.getFileEntity().getName()) && a.getCategory().getName().equals(r.getName()))
                {
                    chooseAsAvatar.setValue(true);
                    chooseAsAvatar.setEnabled(false);
                    avatarsToRotate.add(a);
                }
            }

            chooseAsAvatar.addStyleName(chooseAsAvatar.getValue() ? CssStyle.ANSWERED.getStyleName() : CssStyle.EMPTY_ANSWER.getStyleName());

            if (!chooseAsAvatar.getValue())
            {
                chooseAsAvatar.setValue(false);
                chooseAsAvatar.setEnabled(true);
                chooseAsAvatar.addValueChangeListener((event) -> {
                    chooseAsAvatar.removeStyleName(chooseAsAvatar.getValue() ? CssStyle.EMPTY_ANSWER.getStyleName() : CssStyle.ANSWERED.getStyleName());
                    chooseAsAvatar.addStyleName(chooseAsAvatar.getValue() ? CssStyle.ANSWERED.getStyleName() : CssStyle.EMPTY_ANSWER.getStyleName());
                });
            }
        }

        final HorizontalLayout footerBtnWrapper = new HorizontalLayout();

        final Button save = new Button(I18N.EDIT_MEDIA_POPUP_SAVE.msg());
        save.setIcon(FontAwesome.SAVE);
        save.setStyleName(CssStyle.BTN_SAVE_MEDIA.getStyleName());

        final Button backButton = new Button(I18N.EDIT_MEDIA_POPUP_BACK.msg());
        this.setFooterVisible(true);
        backButton.addClickListener(event -> this.close());

        footerBtnWrapper.addComponents(backButton, save);

        this.setFooter(footerBtnWrapper);

        save.addClickListener(event ->
        {
            if (rotatedImage != null)
            {
                File file = new File(mediaUploadFile.getPath().toString());
                try
                {
                    ImageIO.write(rotatedImage, ImageUtil.JPG_FORMAT, file);
                    rotateMediaCallback.refreshGallery(mediaUploadFile);
                    saveAvatarsCallback.saveRotatedAvatars(rotatedImage, avatarsToRotate);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                //Notification.show(I18N.EDIT_MEDIA_POPUP_CHANGES_SAVED.msg(), Notification.Type.TRAY_NOTIFICATION);
            }

            if (description.getValue() != null && !description.getValue().isEmpty())
            {
                mediaUploadFile.getFileEntity().setDescription(description.getValue());
                descriptionChangedCallback.saveDescription(mediaUploadFile);
            }
            for (CheckBox b : avatarForCategory)
            {
                if (b.isEnabled() && b.getValue())
                {
                    File file = new File(mediaUploadFile.getPath().toString());

                    if (rotatedImage != null)
                        setAsAvatarCallback.saveAsAvatar(file, rotatedImage, (RecommendationCategory) b.getData(), mediaUploadFile.getMedia().getMediaGallery());
                    else
                        setAsAvatarCallback.saveAsAvatar(file, inputImage, (RecommendationCategory) b.getData(), mediaUploadFile.getMedia().getMediaGallery());

                    b.setEnabled(false);
                    b.addStyleName(CssStyle.ANSWERED.getStyleName());
                    b.removeStyleName(CssStyle.EMPTY_ANSWER.getStyleName());
                    //Notification.show(I18N.EDIT_MEDIA_POPUP_CHANGES_SAVED.msg(), Notification.Type.TRAY_NOTIFICATION);
                }

            }

            this.close();
        });

        picWrapper.addComponent(image);
        picWrapper.setComponentAlignment(image, Alignment.MIDDLE_CENTER);

        layout.addComponents(picWrapper, btnWrapper, descriptionWrapper, setAsAvatarWrapper);

        return layout;
    }

    private void replaceImage(Image newImage)
    {
        image = newImage;
    }

    private Resource getImageResource(MediaUploadFile mediaUploadFile)
    {
        if (mediaUploadFile == null || mediaUploadFile.getPath() == null)
            return null;

        return new FileResource(mediaUploadFile.getPath().toFile());
    }

    private Image createImage(Resource resource)
    {
        final Image image = new Image();
        image.setSource(resource);

        return image;
    }

    public void setRotateMediaCallback(RotateMediaCallback rotateMediaCallback)
    {
        this.rotateMediaCallback = rotateMediaCallback;
    }

    public void setDescriptionChangedCallback(DescriptionChangedCallback descriptionChangedCallback)
    {
        this.descriptionChangedCallback = descriptionChangedCallback;
    }

    public void setSetAsAvatarCallback(SetAsAvatarCallback setAsAvatarCallback)
    {
        this.setAsAvatarCallback = setAsAvatarCallback;
    }

    public void  setSaveAvatarsCallback(SaveAvatarsCallback saveAvatarsCallback)
    {
        this.saveAvatarsCallback = saveAvatarsCallback;
    }

}
