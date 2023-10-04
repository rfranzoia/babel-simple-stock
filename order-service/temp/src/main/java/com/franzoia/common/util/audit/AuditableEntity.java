package com.franzoia.common.util.audit;


import com.franzoia.common.util.DefaultEntity;

public interface AuditableEntity extends DefaultEntity {

	Audit getAudit();
	void setAudit(Audit audit);
	
}
