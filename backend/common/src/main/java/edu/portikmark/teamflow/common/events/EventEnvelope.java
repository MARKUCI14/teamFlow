package edu.portikmark.teamflow.common.events;

import java.time.Instant;
import java.util.UUID;

/** Shared envelope metadata used by versioned domain events. */
public record EventEnvelope<T>(
        UUID eventId,
        String type,
        int version,
        Instant occurredAt,
        UUID projectId,
        T payload) {}
