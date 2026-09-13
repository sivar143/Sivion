package com.sivion.api.integration;

import java.time.Instant;
import java.util.UUID;

public record DomainEvent(String eventId,String type,Instant occurredAt,Object payload){
    public static DomainEvent of(String type,Object payload){return new DomainEvent(UUID.randomUUID().toString(),type,Instant.now(),payload);}
}
