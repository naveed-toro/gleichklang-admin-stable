package de.binaerebauten.gleichklang.core.model.systemconfig;

import de.binaerebauten.gleichklang.core.model.BaseEntity;

import de.binaerebauten.gleichklang.core.model.DeletableEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "email_domain_mapping")
public class EmailDomainMapping extends BaseEntity implements DeletableEntity<Long>
{

	@Column(name = "id", nullable = false , updatable = false, insertable = false)
	private Long  id;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "mapping_name", nullable = false)
	private String  mappingName;

	@Column(name = "deleted", nullable = true)
	private boolean deleted;

	@Column(name = "active", nullable = true)
	private boolean active;


	@Column(name = "mapping_value", nullable = false)
	private String  mappingValue;


	public String getMappingName() {
		return mappingName;
	}

	public void setMappingName(String mappingName) {
		this.mappingName = mappingName;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getMappingValue() {
		return mappingValue;
	}

	public void setMappingValue(String mappingValue) {
		this.mappingValue = mappingValue;
	}
}