package de.binaerebauten.gleichklang.core.model;

import java.io.Serializable;

public interface IdEntity<ID extends Serializable>
{
	String ID = "id";

	ID getId();

	void setId(ID id);
}
