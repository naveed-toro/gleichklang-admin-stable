package de.binaerebauten.gleichklang.core.model;


import java.io.Serializable;
import java.util.Comparator;

public interface SortableEntity<ID extends Serializable> extends IdEntity<ID>
{
	String SORT_ORDER = "sortOrder";

	/**
	 * Compares two sortable entities.
	 */
	Comparator<SortableEntity> COMPARATOR = Comparator.comparing(SortableEntity::getSortOrder);

	int getSortOrder();

	void setSortOrder(int sortOrder);
}
