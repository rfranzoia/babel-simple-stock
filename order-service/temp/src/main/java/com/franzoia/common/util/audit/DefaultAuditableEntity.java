package com.franzoia.common.util.audit;

import jakarta.persistence.Embedded;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class DefaultAuditableEntity implements AuditableEntity {

	@Embedded
	protected Audit audit = new Audit();

	@Override
	public Audit getAudit() {
		return audit;
	}

	@Override
	public void setAudit(Audit audit) {
		this.audit = audit;
	}
	
}
