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

        EventEnvelope<String> expected =
                new EventEnvelope<>(eventId, "SubmissionCreated", 1, occurredAt, projectId, "payload");

        assertEquals(expected, event, "Event envelope should preserve its shared contract fields");
    }
}
