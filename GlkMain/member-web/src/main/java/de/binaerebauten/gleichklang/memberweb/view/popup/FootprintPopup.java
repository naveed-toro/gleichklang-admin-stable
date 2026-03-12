package de.binaerebauten.gleichklang.memberweb.view.popup;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.media.Footprint;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.core.view.popup.GenericPopup;
import de.binaerebauten.gleichklang.memberweb.view.I18N;

/**
 * Created by rgoerner on 07.10.16.
 */
public class FootprintPopup extends GenericPopup
{
    public interface FootprintClickListener
    {
        void onFootprintClicked(Footprint footprint);
    }

    private final FootprintClickListener listener;

    public FootprintPopup(FootprintClickListener listener)
    {
        this.listener = listener;

        final Component layout = createLayout();
        this.setPopupContent(layout);
        this.addStyleName(CssStyle.FOOTPRINT_POPUP.getStyleName());
        this.setCaption(I18N.FOOTPRINT_POPUP_CAPTION.msg());
        this.setDraggable(false);
        setBoxSize(BoxSize.LARGE);
    }

    private Component createLayout()
    {
        final VerticalLayout wrapper = new VerticalLayout();
        wrapper.setStyleName(CssStyle.FOOTPRINT_ITEMS_WRAPPER.getStyleName());

		for (Footprint fp : Footprint.values())
		{
		    final HorizontalLayout footprintWrapper = new HorizontalLayout();
            footprintWrapper.setWidth("280px");
            footprintWrapper.setHeight("65px");
            footprintWrapper.addStyleName(CssStyle.RIPPLE_ELEMENT.getStyleName());

            final Image image = new Image();
            image.setHeight(55, Unit.PIXELS);
            image.setSource(new ThemeResource(fp.getPath()));

            final Label text = new Label(fp.getName());

            footprintWrapper.addComponents(image, text);
            footprintWrapper.setComponentAlignment(image, Alignment.MIDDLE_CENTER);
            footprintWrapper.setComponentAlignment(text, Alignment.MIDDLE_CENTER);
            footprintWrapper.setExpandRatio(image, 0.25f);
            footprintWrapper.setExpandRatio(text, 0.75f);

            footprintWrapper.addLayoutClickListener(event -> {
                listener.onFootprintClicked(fp);
                close();
            });

            wrapper.addComponent(footprintWrapper);
		}

        return wrapper;
    }
}
