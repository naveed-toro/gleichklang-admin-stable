package de.binaerebauten.gleichklang.memberweb.view.component;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.media.Footprint;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.view.I18N;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class NewFootprintsComponent extends CustomComponent
{
	public interface NewFootprintsHandler
	{
		void onFootprintClicked(Relationship relationship);
	}

	private final NewFootprintsHandler newFootprintsHandler;
	private final ClientInformation.Device device;
	private HorizontalLayout layout;
	private VerticalLayout footprintWrapper;
	private List<Relationship> relationships;
	private int footprintCounter;
	private List<VerticalLayout> components;

	private Button moveBack;
	private Button moveForward;

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");


	public NewFootprintsComponent(ClientInformation.Device device, NewFootprintsHandler newFootprintsHandler)
	{
		Objects.requireNonNull(newFootprintsHandler);

		this.newFootprintsHandler = newFootprintsHandler;
		this.device = device;

		layout = createFootprintCarousel();
		layout.setStyleName(CssStyle.FOOTPRINTS_WRAPPER.getStyleName());

		relationships = new ArrayList<>();
		components = new ArrayList<>();

		setCompositionRoot(layout);
	}

	private HorizontalLayout createFootprintCarousel()
	{
		footprintWrapper = new VerticalLayout();
		footprintWrapper.setStyleName(CssStyle.FOOTPRINT_USER.getStyleName());

		final HorizontalLayout wrapper = new HorizontalLayout();
		wrapper.setSizeFull();

		final VerticalLayout leftButtonWrapper = new VerticalLayout();
		final VerticalLayout rightButtonWrapper = new VerticalLayout();

		moveBack = new Button();
		moveForward = new Button();
		moveBack.setIcon(FontAwesome.CHEVRON_LEFT);
		moveForward.setIcon(FontAwesome.CHEVRON_RIGHT);

		moveBack.addClickListener(event -> moveCarouselBackwards());
		moveForward.addClickListener(event -> moveCarouselForwards());
		leftButtonWrapper.addComponent(moveBack);
		rightButtonWrapper.addComponent(moveForward);

		wrapper.addComponents(leftButtonWrapper, footprintWrapper, rightButtonWrapper);
		wrapper.setComponentAlignment(leftButtonWrapper, Alignment.MIDDLE_CENTER);
		wrapper.setComponentAlignment(rightButtonWrapper, Alignment.MIDDLE_CENTER);
		wrapper.setExpandRatio(leftButtonWrapper, 0.15f);
		wrapper.setExpandRatio(footprintWrapper, 0.7f);
		wrapper.setExpandRatio(rightButtonWrapper, 0.15f);

		return wrapper;
	}

	private void moveCarouselForwards()
	{
		components.get(footprintCounter-1).setVisible(false);
		components.get(footprintCounter).setVisible(true);
		footprintCounter++;
		initButtons();
	}

	private void  moveCarouselBackwards()
	{
		components.get(footprintCounter-1).setVisible(false);
		components.get(footprintCounter-2).setVisible(true);
		footprintCounter--;
		initButtons();
	}


	public void setFootprints(List<Relationship> relationships)
	{
		this.relationships.clear();
		this.relationships = relationships;

		initFootprints();
	}

	public  Stream<Relationship> getReverseStream(List<Relationship> list) {
		final ListIterator<Relationship> listIt = list.listIterator(list.size());
		final Iterator<Relationship> reverseIterator = new Iterator<Relationship>() {
			@Override
			public boolean hasNext() {
				return listIt.hasPrevious();
			}

			@Override
			public Relationship next() {
				return listIt.previous();
			}
		};
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
				reverseIterator,
				Spliterator.ORDERED | Spliterator.IMMUTABLE), false);
	}

	private void initFootprints()
	{
		footprintWrapper.removeAllComponents();
		components.clear();

		if (relationships.size() >= 1)
		{
			footprintCounter = 0;
			List<Relationship> relationshipsList=relationships.stream().sorted(Comparator.comparing(Relationship::getFootprintDate)).collect(Collectors.toList());
			List<Relationship> relationshipsListReverse=getReverseStream(relationshipsList).collect(Collectors.toList());

			for (Relationship r : relationshipsListReverse)
			{
				VerticalLayout layout = createFootprint(r);
				layout.setSizeFull();
				footprintWrapper.addComponent(layout);
				footprintWrapper.setComponentAlignment(layout, Alignment.MIDDLE_CENTER);
				components.add(layout);

				if (footprintCounter == 0)
				{
					footprintCounter++;
					layout.setVisible(true);
				}
				else
				{
					layout.setVisible(false);
				}
			}
		}
		else
		{
			VerticalLayout layout = createFootprint(null);
			footprintWrapper.addComponent(layout);
		}
		initButtons();
	}

	private void initButtons()
	{
		if (relationships.isEmpty() || relationships.size() < 2 )
		{
			moveBack.setVisible(false);
			moveForward.setVisible(false);
		}
		else if (footprintCounter ==  relationships.size())
		{
			moveBack.setVisible(true);
			moveForward.setVisible(false);
		}
		else if (footprintCounter < relationships.size() && footprintCounter > 1)
		{
			moveBack.setVisible(true);
			moveForward.setVisible(true);
		}
		else if (footprintCounter < relationships.size() && footprintCounter == 1 )
		{
			moveBack.setVisible(false);
			moveForward.setVisible(true);
		}
	}
	private VerticalLayout createFootprint(Relationship relationship)
	{

		final VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setStyleName(CssStyle.FOOTPRINTS_INNER_WRAPPER.getStyleName());

		final HorizontalLayout footprintImage = new HorizontalLayout();
		final VerticalLayout footprintText = new VerticalLayout();


		if (relationship != null && !relationship.getSourceUser().isBlocked()) //can be null if no new footprints
		{
			Image image = createImage(relationship.getFootprint());
			footprintImage.addComponent(image);
			image.addStyleName(CssStyle.RIPPLE_ELEMENT.getStyleName());
			final String dateString = relationship.getFootprintDate() == null ? I18N.RELATIONSHIP_VIEW_FOOTPRINT_NODATE.msg() : relationship.getFootprintDate().format(FORMATTER);
			footprintImage.setDescription(I18N.RELATIONSHIP_VIEW_FOOTPRINT_RECEIVED.msg(dateString, relationship.getFootprint().getName()));

			final Label label = new Label(relationship.getFootprint().getName());
			label.setStyleName(CssStyle.FOOTPRINT_NAME.getStyleName());
			footprintText.addComponent(label);

			layout.addComponent(footprintImage);
			layout.setComponentAlignment(footprintImage, Alignment.MIDDLE_CENTER);
			layout.addComponent(footprintText);
			final Label user = new Label(I18N.NEWFOOTPRINTS_COMPONENT_LABEL.msg(relationship.getSourceUser().getAlias()));
			if (device == ClientInformation.Device.MOBILE)
			{
				if (relationship.getSourceUser().getAlias().length() > 15)
					user.setValue(relationship.getSourceUser().getAlias().substring(0, 14).concat("..."));
			}
			else
				user.setValue(I18N.NEWFOOTPRINTS_COMPONENT_LABEL.msg(relationship.getSourceUser().getAlias()));

			user.addStyleName(CssStyle.FOOTPRINT_USER.getStyleName());

			layout.addComponent(user);
			layout.addLayoutClickListener(event -> newFootprintsHandler.onFootprintClicked(relationship));

		}

		return layout;

	}

	private Image createImage(Footprint footprint)
	{
		Objects.requireNonNull(footprint);

		final Image image = new Image();
		image.setHeight(85, Unit.PIXELS);
		image.setSource(new ThemeResource(footprint.getPath()));

		return image;
	}


}
