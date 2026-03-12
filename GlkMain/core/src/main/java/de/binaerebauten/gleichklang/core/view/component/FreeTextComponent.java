package de.binaerebauten.gleichklang.core.view.component;

import com.google.common.base.Strings;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.FreeTextElement;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rgoerner on 16.09.16.
 */
public class FreeTextComponent extends CustomComponent
{
    public FreeTextComponent(UserProfile.UserProfileData userProfileData)
    {
        final Component freeText = createFreeTextComponent(userProfileData);
        setCompositionRoot(freeText);
    }

    private Component createFreeTextComponent(UserProfile.UserProfileData userProfileData)
    {
        final Panel panel = new Panel();
        panel.setSizeFull();

        final VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setStyleName(CssStyle.USER_PROFILE_FREETEXT.getStyleName());

        final Label freeTextLabel = new Label();
        freeTextLabel.setContentMode(ContentMode.HTML);

        final List<Label> freeTextLabels = new ArrayList<>();

        int cnt = 1;

        for (FreeTextElement freeText : userProfileData.getFreeText())
        {

//            final Label label = new Label();
            if (!freeText.isEmpty())
            {
                if (!Strings.isNullOrEmpty(freeText.getHeader())) {
                    final Label headerLabel = new Label();
                    headerLabel.setValue(freeText.getHeader());
                    headerLabel.setContentMode(ContentMode.HTML);
                    headerLabel.addStyleName(CssStyle.FREE_TEXT_HEADER.getStyleName() + "-" + cnt);
                    if (cnt == 4)
                        cnt = 1;
                    else
                        cnt++;
                    freeTextLabels.add(headerLabel);
                }

                if (!Strings.isNullOrEmpty(freeText.getContent())) {
                    final Label contentLabel = new Label();
                    contentLabel.setContentMode(ContentMode.HTML);
                    contentLabel.setValue(freeText.getContent());
                    contentLabel.addStyleName(CssStyle.FREE_TEXT_BODY.getStyleName());
                    freeTextLabels.add(contentLabel);
                }
            }
        }
        int count = 0;

        for (Label label : freeTextLabels)
        {
            layout.addComponent(label);
            layout.setComponentAlignment(label, Alignment.MIDDLE_LEFT);

            if (count > 0 && label.getStyleName().contains(CssStyle.FREE_TEXT_HEADER.getStyleName())
                    && freeTextLabels.get(count-1).getStyleName().contains(CssStyle.FREE_TEXT_HEADER.getStyleName()))
            {
                layout.removeComponent(freeTextLabels.get(count-1));
            }
            count++;
        }

        panel.setContent(layout);

        return panel;
    }
}

