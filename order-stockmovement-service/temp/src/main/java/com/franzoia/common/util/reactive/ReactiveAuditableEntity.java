package com.franzoia.common.util.reactive;

import com.franzoia.common.util.DefaultEntity;

import java.time.ZonedDateTime;

public interface ReactiveAuditableEntity extends DefaultEntity {

    ZonedDateTime getDateCreated();
    void setDateCreated(ZonedDateTime dateCreated);

    ZonedDateTime getDateUpdated();
    void setDateUpdated(ZonedDateTime dateUpdated);

}
