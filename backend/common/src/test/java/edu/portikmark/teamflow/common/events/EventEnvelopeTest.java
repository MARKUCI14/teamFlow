package edu.portikmark.teamflow.common.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EventEnvelopeTest {

    @Test
    void storesSharedEventMetadataAndPayload() {
        UUID eventId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-11-24T10:00:00Z");
        EventEnvelope<String> event =
                new EventEnvelope<>(eventId, "SubmissionCreated", 1, occurredAt, projectId, "payload");

        assertEquals(eventId, event.eventId());
        assertEquals("SubmissionCreated", event.type());
        assertEquals(1, event.version());
        assertEquals(occurredAt, event.occurredAt());
        assertEquals(projectId, event.projectId());
        assertEquals("payload", event.payload());
    }
}
