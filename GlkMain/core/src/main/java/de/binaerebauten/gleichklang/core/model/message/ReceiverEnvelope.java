package de.binaerebauten.gleichklang.core.model.message;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@Entity
public class ReceiverEnvelope extends Envelope
{
	@NotNull
	@Column(name = "read_")
	private boolean read = false;
	
	public boolean isRead()
	{
		return read;
	}
	
	public void setRead(boolean read)
	{
		this.read = read;
	}
}
