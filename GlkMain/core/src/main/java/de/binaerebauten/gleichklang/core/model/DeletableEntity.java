package de.binaerebauten.gleichklang.core.model;

import java.io.Serializable;

public interface DeletableEntity<ID extends Serializable> extends IdEntity<ID>
{
	String DELETED = "deleted";

	boolean isDeleted();

	void setDeleted(boolean deleted);
}
