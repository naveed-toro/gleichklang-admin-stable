package de.binaerebauten.gleichklang.core.model;

import de.binaerebauten.gleichklang.core.model.user.User;

import java.io.Serializable;

public interface SourceUserEntity<ID extends Serializable>
{
	String SOURCE_USER = "sourceUser";

	User getSourceUser();

	void setSourceUser(User user);
}



