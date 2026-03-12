package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;

public interface Committable
{
	void commit() throws CommitException;
}
