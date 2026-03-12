package de.binaerebauten.gleichklang.core.view.component;

import com.google.common.base.Strings;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;
import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.utils.PropertyPathBuilder;
import de.binaerebauten.gleichklang.core.utils.StringUtils;
import de.binaerebauten.gleichklang.core.view.component.converter.PagingTextFieldConverter;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import javax.persistence.metamodel.SingularAttribute;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.vaadin.server.FontAwesome.*;
import static de.binaerebauten.gleichklang.core.view.component.I18N.*;

public class CustomLazyBeanPagingComponent<BEANTYPE extends BaseEntity> extends LazyBeanPagingComponent<BEANTYPE>
{

	public enum PageSize
	{
		_2(2),
		_5(5),
		_10(10);

		private final int pages;

		PageSize(int pages)
		{
			this.pages = pages;
		}

		public int getPageSize()
		{
			return pages;
		}

		@Override
		public String toString()
		{
			return "" + pages;
		}
	}

	private class Column
	{
		private final ColumnGenerator<BEANTYPE> columnGenerator;
		private final Object id;
		private final String header;

		public Column(ColumnGenerator<BEANTYPE> columnGenerator, Object id, String header)
		{
			this.columnGenerator = columnGenerator;
			this.id = id;
			this.header = Strings.nullToEmpty(header);
		}

		public ColumnGenerator<BEANTYPE> getColumnGenerator()
		{
			return columnGenerator;
		}

		public Object getId()
		{
			return id;
		}

		public String getHeader()
		{
			return header;
		}
	}

	private final List<Column> columns = new ArrayList<>();

	private final Map<BEANTYPE, CheckBox> selectedMap = new HashMap<>();
	private final Map<BEANTYPE, Component> rowEntryMap = new HashMap<>();

	private final Button addNewButton;
	private final TextField pageTextField;
	private final PagingTextFieldConverter pagingTextFieldConverter;

	private final Button firstPageButton;
	private final Button prevPageButton;
	private final Button lastPageButton;
	private final Button nextPageButton;

	private final ComponentContainer contentContainer;
	private final Component paginationLayout;
	private final HorizontalLayout spacerLayout = new HorizontalLayout();

	private CellStyleGenerator<BEANTYPE> cellStyleGenerator = null;
	private int page = 0;
	private int totalPages = 0;
	private PageSize pageSize = PageSize._2;
	private Sort sort = null;
	private boolean selectable = false;
	private boolean multiSelect = false;
	private boolean columnHeaderVisible = true;
	private int maxResults = -1;
	boolean hasAddButton = true;
	boolean arebuttonsOnTop = false;

	public Button getAddNewButton() {
		return addNewButton;
	}

	public CustomLazyBeanPagingComponent()
	{
		super();
		addNewButton = createAddNewButton();
		pagingTextFieldConverter = new PagingTextFieldConverter(totalPages);
		pageTextField = createPageTextField();

		firstPageButton = createNavigationButton(ANGLE_DOUBLE_LEFT, PAGING_CONTROL_FIRST_PAGE.msg(), event -> gotoPage(0));
		prevPageButton = createNavigationButton(ANGLE_LEFT, PAGING_CONTROL_PREV_PAGE.msg(), event -> gotoPage(page - 1));
		nextPageButton = createNavigationButton(ANGLE_RIGHT, PAGING_CONTROL_NEXT_PAGE.msg(), event -> gotoPage(page + 1));
		lastPageButton = createNavigationButton(ANGLE_DOUBLE_RIGHT, PAGING_CONTROL_LAST_PAGE.msg(), event -> gotoPage(getMaxPage()));

		contentContainer = createContentLayout();
		paginationLayout = createPaginationLayout();

		setCompositionRoot(createLayout());
	}


	public CustomLazyBeanPagingComponent(boolean hasAddButton, boolean arebuttonsOnTop, PageSize pageSize)
	{
		super();


		this.pageSize = pageSize;
		this.hasAddButton = hasAddButton;
		this.arebuttonsOnTop = arebuttonsOnTop;

		addNewButton = createAddNewButton();

		pagingTextFieldConverter = new PagingTextFieldConverter(totalPages);
		pageTextField = createPageTextField();

		firstPageButton = createNavigationButton(ANGLE_DOUBLE_LEFT, PAGING_CONTROL_FIRST_PAGE.msg(), event -> gotoPage(0));
		prevPageButton = createNavigationButton(ANGLE_LEFT, PAGING_CONTROL_PREV_PAGE.msg(), event -> gotoPage(page - 1));
		nextPageButton = createNavigationButton(ANGLE_RIGHT, PAGING_CONTROL_NEXT_PAGE.msg(), event -> gotoPage(page + 1));
		lastPageButton = createNavigationButton(ANGLE_DOUBLE_RIGHT, PAGING_CONTROL_LAST_PAGE.msg(), event -> gotoPage(getMaxPage()));

		contentContainer = createContentLayout();
		paginationLayout = createPaginationLayout();

		setCompositionRoot(createLayout());
	}

	private ComponentContainer createContentLayout()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.addStyleName(CssStyle.LAZYBEAN_PAGING_LIST.getStyleName());

		return layout;
	}

	private Component createLayout()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.addStyleName(CssStyle.LAZYBEAN_PAGING_COMPONENT.getStyleName());

		spacerLayout.setWidth(100, Unit.PIXELS);
		if(addNewButton != null)
		addNewButton.setWidth(191, Unit.PIXELS);
		if(arebuttonsOnTop)
		layout.addComponents(paginationLayout, contentContainer);
		else
			layout.addComponents(contentContainer, paginationLayout);

		return layout;
	}

	private HorizontalLayout createPaginationLayout()
	{
		final HorizontalLayout paginationLayout = new HorizontalLayout();
		paginationLayout.setSpacing(true);
		paginationLayout.addStyleName(CssStyle.LAZYBEAN_PAGING_LIST_FOOTER_CONTOLS.getStyleName());
		paginationLayout.setWidth(100, Unit.PERCENTAGE);

		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);

		buttonLayout.addComponents(firstPageButton, prevPageButton, pageTextField, nextPageButton, lastPageButton);

		if (addNewButton != null) {
			paginationLayout.addComponents(addNewButton);
			paginationLayout.setComponentAlignment(addNewButton, Alignment.MIDDLE_CENTER);
//			paginationLayout.setExpandRatio(addNewButton, 0.0f);
//			paginationLayout.setExpandRatio(spacerLayout, 0.0f);
		}
		else
		{
			paginationLayout.addComponents(buttonLayout, spacerLayout);
			paginationLayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_CENTER);
			paginationLayout.setComponentAlignment(spacerLayout, Alignment.MIDDLE_RIGHT);
			paginationLayout.setExpandRatio(buttonLayout, 1.0f);
			paginationLayout.setExpandRatio(spacerLayout, 0.0f);
		}
		return paginationLayout;

	}

	private Button createNavigationButton(Resource icon, String description, ClickListener listener)
	{
		final Button button = new Button();
		button.setIcon(icon);
		button.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
		button.setDescription(description);
		button.addClickListener(listener);

		return button;
	}

	private int getMaxPage()
	{
		return Math.max(0, totalPages - 1);
	}

	private Button createAddNewButton()
	{
		final Button addNew = new Button("Audiogruß hinzufügen");
//		addNew.setWidth("100%");
		//addNew.setIcon(FontAwesome.PLUS_CIRCLE);

		return addNew;
	}

	private TextField createPageTextField()
	{
		final TextField pageTextField = ComponentFactory.getInstance().createField(TextField.class);
		final BlurListener blurListener = event -> gotoPage((int) pageTextField.getConvertedValue());

		pageTextField.setConverter(pagingTextFieldConverter);
		pageTextField.addShortcutListener(new ShortcutListener(null, KeyCode.ENTER, null)
		{
			@Override
			public void handleAction(Object sender, Object target)
			{
				pageTextField.removeBlurListener(blurListener);
				gotoPage((int) pageTextField.getConvertedValue());
				pageTextField.addBlurListener(blurListener);
			}
		});
		pageTextField.addFocusListener(event ->
		{
			pageTextField.selectAll();
		});
		pageTextField.addBlurListener(blurListener);

		pageTextField.setStyleName(CssStyle.LAZYBEAN_PAGING_LIST_PAGECOUNTER.getStyleName());
		return pageTextField;
	}

	private void gotoPage(int page)
	{
		this.page = Math.min(getMaxPage(), Math.max(0, page));
		buildContent();
	}

	@Override
	public void refresh()
	{
		page = 0;
		totalPages = 0;

		buildContent();
	}

	public void refreshItem(BEANTYPE itemId)
	{
		final CheckBox newSelectedCheckBox = new CheckBox();
		final Component newRowEntry = createRowEntry(itemId, newSelectedCheckBox);
		final Component oldRowEntry = rowEntryMap.get(itemId);

		rowEntryMap.put(itemId, newRowEntry);
		selectedMap.put(itemId, newSelectedCheckBox);

		contentContainer.replaceComponent(oldRowEntry, newRowEntry);
	}

	private void buildContent()
	{
		selectedMap.clear();
		contentContainer.removeAllComponents();
		rowEntryMap.clear();

		if (getHandler() != null)
		{
			contentContainer.addComponent(createColumnHeader());

			final Page<BEANTYPE> page = getHandler().getItems(getSpecification(), createPageable());
			totalPages = page.getTotalPages();
			onSizeChanged((int) page.getTotalElements());
			for (BEANTYPE itemId : page)
			{
				final CheckBox selectCheckBox = new CheckBox();
				final Component rowEntry = createRowEntry(itemId, selectCheckBox);

				rowEntryMap.put(itemId, rowEntry);
				selectedMap.put(itemId, selectCheckBox);

				contentContainer.addComponent(rowEntry);
			}
		}

		updateNavigationComponent();
		UI.getCurrent().setScrollTop(0);
	}

	private PageRequest createPageable()
	{
		if (maxResults < 0)
			return new PageRequest(this.page, this.pageSize.getPageSize(), sort);

		return new PageRequest(0, maxResults, sort);
	}

	private void updateNavigationComponent()
	{
		final boolean general = totalPages > 1;
		final boolean left = general && page > 0;
		final boolean right = general && page < getMaxPage();

		firstPageButton.setEnabled(left);
		prevPageButton.setEnabled(left);
		nextPageButton.setEnabled(right);
		lastPageButton.setEnabled(right);

		pagingTextFieldConverter.setTotalPages(totalPages);
		pageTextField.setConvertedValue(page);
	}

	// TODO do we need a header?
	private Component createColumnHeader()
	{
		final CssLayout columnHeader = new CssLayout();

		// add empty field if table is selectable
		if (selectable) {
			final Label selectableColumnHeaderLabel = new Label();
			selectableColumnHeaderLabel.setSizeUndefined();
			selectableColumnHeaderLabel.addStyleName(CssStyle.LAZYBEAN_PAGING_LIST_COLUMN_SELECT.getStyleName());
			columnHeader.addComponent(selectableColumnHeaderLabel);
		}

		columns.stream().map(Column::getHeader).forEach(s -> {
			final Label headerLabel = new Label(s);
			headerLabel.setSizeUndefined();
			headerLabel.addStyleName(CssStyle.LAZYBEAN_PAGING_LIST_COLUMN.getStyleName());
			columnHeader.addComponent(headerLabel);
		});
		columnHeader.setVisible(columnHeaderVisible);
		columnHeader.addStyleName(CssStyle.LAZYBEAN_PAGING_LIST_HEADER.getStyleName());

		return columnHeader;
	}

	private Component createRowEntry(BEANTYPE itemId, CheckBox selectCheckBox)
	{
		selectCheckBox.setVisible(selectable);
		selectCheckBox.addStyleName(CssStyle.LAZYBEAN_PAGING_LIST_COLUMN_SELECT.getStyleName());
		selectCheckBox.addValueChangeListener(l ->
		{
			if (!multiSelect && selectCheckBox.getValue())
			{
				for (CheckBox c : selectedMap.values())
				{
					if (!c.equals(selectCheckBox)) c.setValue(false);
				}
			}
			fireValueChangeEvent();
		});

		final CssLayout entryLayout = new CssLayout();
		entryLayout.addStyleName(CssStyle.LAZYBEAN_PAGING_LIST_ROW.getStyleName());
		entryLayout.setWidth(100, Unit.PERCENTAGE);
		entryLayout.addLayoutClickListener(e ->
		{
			if (!selectCheckBox.equals(e.getClickedComponent()))
				fireItemClickedEvent(itemId, e.isDoubleClick());
		});

		entryLayout.addComponent(selectCheckBox);
		columns.forEach(c -> entryLayout.addComponent(createCell(itemId, c)));

		if (cellStyleGenerator != null)
		{
			final CssStyle cssStyle = cellStyleGenerator.getStyle(this, itemId, null);
			if (cssStyle != null)
				entryLayout.addStyleName(cssStyle.getStyleName());
		}

		selectCheckBox.addValueChangeListener(event ->
		{
			if (selectCheckBox.getValue())
			{
				entryLayout.addStyleName(CssStyle.LAZYBEAN_PAGING_LIST_ROW_SELECTED.getStyleName());
			}
			else
			{
				entryLayout.removeStyleName(CssStyle.LAZYBEAN_PAGING_LIST_ROW_SELECTED.getStyleName());
			}
		});
		return entryLayout;
	}

	private Component createCell(BEANTYPE itemId, Column column)
	{
		final Object cellObj = column.getColumnGenerator().generateCell(this, itemId, column.getId());

		Component cellComponent = null;

		if (cellObj instanceof Component)
			cellComponent = (Component) cellObj;
		if (cellObj instanceof String)
			cellComponent = new Label((String) cellObj, ContentMode.HTML);
		if (cellObj instanceof LocalDateTime)
			cellComponent = new Label(StringUtils.timeToString((LocalDateTime) cellObj));
		if (cellObj instanceof Duration)
			cellComponent = new Label(StringUtils.durationToString((Duration) cellObj));
		if (cellObj instanceof Enum)
			cellComponent = new Label(cellObj.toString());
		if (cellObj instanceof Number)
			cellComponent = new Label(cellObj.toString());
		if (cellObj == null)
			cellComponent = new Label();

		if (cellComponent == null)
			throw new UnsupportedOperationException("not supported generateColumn type");

		cellComponent.addStyleName(CssStyle.LAZYBEAN_PAGING_LIST_COLUMN.getStyleName());
		if (cellStyleGenerator != null)
		{
			final CssStyle cssStyle = cellStyleGenerator.getStyle(this, itemId, column.getId());
			if (cssStyle != null)
				cellComponent.addStyleName(cssStyle.getStyleName());
		}
		cellComponent.setWidthUndefined();
		return cellComponent;
	}

	/**
	 * Adds a generated column to the Table. <p> A generated column is a column
	 * that exists only in the Table, not as a property in the underlying
	 * Container. It shows up just as a regular column. </p> <p> A generated
	 * column will override a property with the same id, so that the generated
	 * column is shown instead of the column representing the property. Note
	 * that getContainerProperty() will still get the real property. </p> <p>
	 * Table will not listen to value change events from properties overridden
	 * by generated columns. If the content of your generated column depends on
	 * properties that are not directly visible in the table, attach value
	 * change listener to update the content on all depended properties.
	 * Otherwise your UI might not get updated as expected. </p> <p> Also note
	 * that getVisibleColumns() will return the generated columns, while
	 * getContainerPropertyIds() will not. </p>
	 *
	 * @param generatedColumn the {@link Function <BEANTYPE, ?>}  to generate
	 *                        the column from the bean.
	 */
	public void addGeneratedColumn(Function<BEANTYPE, Component> generatedColumn)
	{
		addGeneratedColumn(null, null, (source, itemId, columnId) -> generatedColumn.apply(itemId));
	}

	@Override
	public void addGeneratedColumn(String header, Object id, ColumnGenerator<BEANTYPE> generatedColumn)
	{
		columns.add(new Column(generatedColumn, id, header));
		buildContent();
	}

	/**
	 * Sets the currently sorted property id.
	 *
	 * @param ascending  <code>true</code> if ascending, <code>false</code> if
	 *                   descending.
	 * @param propertyId the currently sorted property id
	 */
	@Override
	public void setSortPropertyId(boolean ascending, SingularAttribute<?, ?>... propertyId)
	{
		if (propertyId == null || propertyId.length == 0)
		{
			sort = null;
		}
		else
		{
			sort = new Sort(ascending ? Direction.ASC : Direction.DESC, PropertyPathBuilder.getFieldName(propertyId));
		}
	}

	/**
	 * Sets the currently sorted column property id.
	 *
	 * @param propertyId the currently sorted property id
	 */
	@Override
	public void setSortPropertyId(SingularAttribute<?, ?>... propertyId)
	{
		setSortPropertyId(true, propertyId);
	}

	public void initView(Device device)
	{
		switch (device)
		{
			case MOBILE:
				initMobileView();
				break;
			case TABLET:
				initTabletView();
				break;
			case DESKTOP:
				initDesktopView();
				break;
		}
	}

	private void initDesktopView()
	{
		addNewButton.setVisible(true);
		spacerLayout.setVisible(true);
		pageTextField.setVisible(true);
		pageTextField.setEnabled(true);
	}

	private void initTabletView()
	{
		addNewButton.setVisible(false);
		spacerLayout.setVisible(false);
		pageTextField.setVisible(true);
		pageTextField.setEnabled(false);
	}

	private void initMobileView()
	{
		addNewButton.setVisible(false);
		spacerLayout.setVisible(false);
		pageTextField.setVisible(true);
		pageTextField.setEnabled(false);
	}

	public void setValue(BEANTYPE value)
	{
		setValue(Collections.singleton(value));
	}

	public void setValue(Collection<BEANTYPE> values)
	{
		clearValue();
		values.stream().map(selectedMap::get).filter(Objects::nonNull).forEach(c -> c.setValue(true));
	}

	@Override
	public Set<BEANTYPE> getValues()
	{
		return selectedMap.entrySet().stream().filter(e -> e.getValue().getValue()).map(Entry::getKey).collect(Collectors.toSet());
	}

	@Override
	public BEANTYPE getValue()
	{
		final Set<BEANTYPE> values = getValues();
		return values.size() == 1 ? values.iterator().next() : null;
	}

	@Override
	Map<Integer, BEANTYPE> getValuesWithIndex()
	{
		return getValues().stream().collect(Collectors.toMap(v -> v.getId().intValue(), e -> e));
	}

	@Override
	public void setSelectable(boolean selectable)
	{
		this.selectable = selectable;

		selectedMap.values().forEach(c -> c.setVisible(selectable));
	}

	@Override
	public void setMultiSelect(boolean multiSelect)
	{
		this.multiSelect = multiSelect;

		selectedMap.values().forEach(c -> c.setValue(false));
	}

	@Override
	public void setColumnHeaderVisible(boolean visible)
	{
		this.columnHeaderVisible = visible;
		buildContent();
	}

	@Override
	public void clearValue()
	{
		selectedMap.values().forEach(c -> c.setValue(false));
	}

	@Override
	public void setCellStyleGenerator(CellStyleGenerator<BEANTYPE> cellStyleGenerator)
	{
		this.cellStyleGenerator = cellStyleGenerator;
	}

	public void setMaxResults(int maxResults)
	{
		this.maxResults = maxResults;

		paginationLayout.setVisible(maxResults < 0);
	}

	@Override
	void setValue(Map<Integer, BEANTYPE> valuesWithIndex)
	{
		setValue(valuesWithIndex.values());
	}

	@Override
	void setSortableByMoveHandler(boolean enabled)
	{
		// nothing to do, because the PagingComponent has no sortable header yet
	}
}