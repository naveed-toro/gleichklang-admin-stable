package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnHeaderMode;
import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.SortableEntity;
import de.binaerebauten.gleichklang.core.utils.PropertyPathBuilder;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import org.vaadin.addons.lazyquerycontainer.LazyQueryView;
import org.vaadin.addons.lazyquerycontainer.QueryItemStatus;

import javax.persistence.metamodel.SingularAttribute;
import java.util.*;

/**
 * This is a {@link Table} with the {@link LazyBeanItemContainer} as data
 * source.
 *
 * @param <BEANTYPE>
 * @author fhessel
 */
public class LazyBeanTable<BEANTYPE extends BaseEntity> extends LazyBeanItemComponent<BEANTYPE>
{
	private static final String MOVE_HANDLER_EXCEPTION = "Not possible with MoveHandler";
	
	private final Table table;
	private final LazyBeanItemContainer<BEANTYPE> container;
	
	private int maxDepth = 0;
	private boolean sortByMoveHandler = false;
	private boolean beforeMoveHandlerSortable;
	private Object beforeMoveHandlerSortProperty;
	
	public LazyBeanTable()
	{
		container = new LazyBeanItemContainer<>((pageable, specification) -> getHandler() == null ? null : getHandler().getItems(pageable, specification), specifications, this::onSizeChanged);
		table = new Table();
		table.setContainerDataSource(container);
		table.addValueChangeListener(event -> fireValueChangeEvent());
		table.addItemClickListener(event -> fireItemClickedEvent(container.getBean(event.getItemId()), event.isDoubleClick()));
//		table.setSizeFull();
//		table.setCacheRate(100);
//    	table.setPageLength(100);
//		table.setImmediate(true);


		setCompositionRoot(table);
	}
	
	private void updateMaxDepth(SingularAttribute<?, ?>... propertyId)
	{
		if (maxDepth < propertyId.length - 1)
		{
			maxDepth = propertyId.length - 1;
			container.setMaxNestedPropertyDepth(maxDepth);
		}
	}
	
	/**
	 * Adds a new property to the definition. The depth of the property would be
	 * concatenate automatically.
	 *
	 * @param propertyId
	 */
	public void addContainerProperty(SingularAttribute<?, ?>... propertyId)
	{
		final String propertyIdPath = PropertyPathBuilder.getFieldName(propertyId);
		container.addContainerProperty(propertyIdPath, propertyId[propertyId.length - 1].getJavaType(), null, true, true);
		updateMaxDepth(propertyId);
		final List<Object> visibleColumns = new ArrayList<>(Arrays.asList(table.getVisibleColumns()));
		visibleColumns.add(propertyIdPath);
		table.setVisibleColumns(visibleColumns.toArray());
		//				table.setContainerDataSource(container);
	}

	/**
	 * get all rows
	 * @return
	 */
	public final Collection<?> getItemIds() {
		return container.getItemIds();
	}

	/**
	 * Adds a new property to the definition. The depth of the property would be
	 * concatenate automatically.
	 *
	 * @param columnHeader The column header of the property
	 * @param propertyId
	 */
	public void addContainerProperty(String columnHeader, SingularAttribute<?, ?>... propertyId)
	{
		addContainerProperty(propertyId);
		setColumnHeader(columnHeader, propertyId);
	}
	
	/**
	 * Sets the headers of the columns. The container properties must be added
	 * first.
	 *
	 * @param columnHeaders
	 */
	public void setColumnHeaders(String... columnHeaders)
	{
		table.setColumnHeaders(columnHeaders);
	}
	
	/**
	 * Sets the header of a concrete property.
	 *
	 * @param columnHeader
	 * @param propertyId
	 */
	public void setColumnHeader(String columnHeader, SingularAttribute<?, ?>... propertyId)
	{
		final String propertyIdPath = PropertyPathBuilder.getFieldName(propertyId);
		table.setColumnHeader(propertyIdPath, columnHeader);
	}
	
	/**
	 *
	 * @return
	 */
	public Set<BEANTYPE> getRows()
	{
		return container.getBeans();
	}

	/**
	 * Gets the selected beans
	 *
	 * @return
	 */
	@Override
	public Set<BEANTYPE> getValues()
	{
		return container.getBeans(table.getValue());
	}
	
	/**
	 * Gets the selected bean (in multiselect mode this function fail)
	 *
	 * @return
	 */
	@Override
	public BEANTYPE getValue()
	{
		return container.getBean(table.getValue());
	}
	
	/**
	 * Set the index of the table (position in the table).
	 *
	 * @param index
	 */
	public void setValue(int index)
	{
		setValue(Collections.singleton(index));
	}
	
	/**
	 * Set the indexes of the table (position in the table).
	 *
	 * @param indexes
	 */
	public void setValue(Collection<Integer> indexes)
	{
		final Set<Object> items = new HashSet<>();
		for (int i : indexes)
		{
			items.add(container.getIdByIndex(i));
		}
		
		if (items.isEmpty())
		{
			table.setValue(null);
		}
		else if (!table.isMultiSelect())
		{
			table.setValue(items.iterator().next());
		}
		else
		{
			table.setValue(items);
		}
	}
	
	@Override
	public void setValue(Map<Integer, BEANTYPE> valuesWithIndex)
	{
		setValue(valuesWithIndex.keySet());
	}
	
	/**
	 * Returns the values as map with the index in the table as key
	 *
	 * @return
	 */
	@Override
	public Map<Integer, BEANTYPE> getValuesWithIndex()
	{
		Map<Integer, BEANTYPE> result = new HashMap<>();
		for (int index : container.indexOfIds(table.getValue()))
		{
			result.put(index, container.getBean(container.getIdByIndex(index)));
		}
		return result;
	}
	
	@Override
	public void clearValue()
	{
		table.setValue(null);
	}
	
	/**
	 * Setter for property selectable. <p> <p> The table is not selectable by
	 * default. </p>
	 *
	 * @param selectable the New value of property selectable.
	 */
	@Override
	public void setSelectable(boolean selectable)
	{
		table.setSelectable(selectable);
	}
	
	/**
	 * Sets the multiselect mode. Setting multiselect mode false may lose
	 * selection information: if selected items set contains one or more
	 * selected items, only one of the selected items is kept as selected.
	 * <p>
	 * Subclasses of AbstractSelect can choose not to support changing the
	 * multiselect mode, and may throw {@link UnsupportedOperationException}.
	 *
	 * @param multiSelect the New value of property multiSelect.
	 */
	@Override
	public void setMultiSelect(boolean multiSelect)
	{
		table.setMultiSelect(multiSelect);
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
	 * @param id              the id of the column to be added
	 * @param generatedColumn the {@link ColumnGenerator} to use for this
	 *                        column
	 */
	@Override
	public void addGeneratedColumn(String header, Object id, ColumnGenerator<BEANTYPE> generatedColumn)
	{
		if (generatedColumn == null)
			throw new IllegalArgumentException("Can not add null as a GeneratedColumn");
		table.addGeneratedColumn(id, (source, itemId, columnId) -> generatedColumn.generateCell(this, container.getBean(itemId), columnId));
		table.setColumnHeader(id, header);
	}
	
	public void addDebugProperties()
	{
		table.addContainerProperty(LazyQueryView.DEBUG_PROPERTY_ID_QUERY_INDEX, Integer.class, 0);
		table.addContainerProperty(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_INDEX, Integer.class, 0);
		table.addContainerProperty(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_QUERY_TIME, Integer.class, 0);
		table.addContainerProperty(LazyQueryView.PROPERTY_ID_ITEM_STATUS, QueryItemStatus.class, QueryItemStatus.None);
	}
	
	/**
	 * Set cell style generator for Table.
	 *
	 * @param cellStyleGenerator New cell style generator or null to remove
	 *                           generator.
	 */
	@Override
	public void setCellStyleGenerator(CellStyleGenerator<BEANTYPE> cellStyleGenerator)
	{
		if (cellStyleGenerator == null)
		{
			table.setCellStyleGenerator(null);
		}
		else
		{
			table.setCellStyleGenerator((source, itemId, propertyId) ->
			{
				CssStyle cssStyle = cellStyleGenerator.getStyle(this, container.getBean(itemId), (String) propertyId);
				return cssStyle == null ? null : cssStyle.getStyleName();
			});
		}
	}
	
	public Table getNativeTable()
	{
		return table;
	}
	
	@Override
	public void refresh()
	{
		container.refresh();
		fireValueChangeEvent();
	}
	
	@Override
	public void setSizeFull()
	{
		table.setSizeFull();
	}
	
	/**
	 * Sets the currently sorted column property id.
	 *
	 * @param ascending  <code>true</code> if ascending, <code>false</code> if
	 *                   descending.
	 * @param propertyId the Container property id of the currently sorted
	 *                   column.
	 */
	@Override
	public void setSortPropertyId(boolean ascending, SingularAttribute<?, ?>... propertyId)
	{
		table.setSortContainerPropertyId(PropertyPathBuilder.getFieldName(propertyId));
		table.setSortAscending(ascending);
	}
	
	/**
	 * Sets the currently sorted column property id.
	 *
	 * @param propertyId the Container property id of the currently sorted
	 *                   column.
	 */
	@Override
	public void setSortPropertyId(SingularAttribute<?, ?>... propertyId)
	{
		if (isSortableByMoveHandler())
			throw new IllegalStateException(MOVE_HANDLER_EXCEPTION);
		super.setSortPropertyId(propertyId);
	}
	
	/**
	 * Enables or disables sorting. <p> Setting this to false disallows sorting
	 * by the user. It is still possible to call {@link #setSortPropertyId(SingularAttribute[])}
	 * ()}. </p>
	 *
	 * @param sortEnabled true to allow the user to sort the table, false to
	 *                    disallow it
	 */
	public void setSortEnabled(boolean sortEnabled)
	{
		if (isSortableByMoveHandler())
			throw new IllegalStateException(MOVE_HANDLER_EXCEPTION);
		table.setSortEnabled(sortEnabled);
	}
	
	public boolean isSortableByMoveHandler()
	{
		return sortByMoveHandler;
	}
	
	@Override
	public void setSortableByMoveHandler(boolean enabled)
	{
		sortByMoveHandler = enabled;
		if (enabled)
		{
			beforeMoveHandlerSortable = table.isSortEnabled();
			beforeMoveHandlerSortProperty = table.getSortContainerPropertyId();
			table.setSortEnabled(false);
			table.setSortContainerPropertyId(SortableEntity.SORT_ORDER);
		}
		else
		{
			table.setSortEnabled(beforeMoveHandlerSortable);
			table.setSortContainerPropertyId(beforeMoveHandlerSortProperty);
		}
	}
	
	@Override
	public void setColumnHeaderVisible(boolean visible)
	{
		table.setColumnHeaderMode(visible ? ColumnHeaderMode.EXPLICIT_DEFAULTS_ID : ColumnHeaderMode.HIDDEN);
	}
}
