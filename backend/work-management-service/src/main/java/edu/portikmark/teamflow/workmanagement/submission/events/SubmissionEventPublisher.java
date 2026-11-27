package edu.portikmark.teamflow.workmanagement.submission.events;

import java.time.Instant;
import java.util.UUID;
import edu.portikmark.teamflow.common.events.EventEnvelope;
import edu.portikmark.teamflow.workmanagement.submission.domain.Submission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@ConditionalOnProperty(name = "teamflow.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class SubmissionEventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubmissionEventPublisher.class);

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper objectMapper;
    private final String topic;

    public SubmissionEventPublisher(KafkaTemplate<String, String> kafka, ObjectMapper objectMapper,
                                    @Value("${teamflow.kafka.submissions-topic:submissions.events}") String topic) {
        this.kafka = kafka;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    @TransactionalEventListener
    public void publish(Submission submission) {
        EventEnvelope<SubmissionCreated> event = new EventEnvelope<>(UUID.randomUUID(), "SubmissionCreated", 1,
                Instant.now(), submission.getProjectId(), new SubmissionCreated(submission.getId(),
                submission.getAuthorId(), submission.getTitle(), submission.getType(), submission.getStatus()));
        try {
            String json = objectMapper.writeValueAsString(event);
            kafka.send(topic, submission.getProjectId().toString(), json).whenComplete((result, error) -> {
                if (error != null) {
                    LOGGER.error("Could not publish SubmissionCreated for {}", submission.getId(), error);
                }
            });
        } catch (JsonProcessingException exception) {
            LOGGER.error("Could not serialize SubmissionCreated for {}", submission.getId(), exception);
        } catch (RuntimeException exception) {
            LOGGER.error("Could not enqueue SubmissionCreated for {}", submission.getId(), exception);
        }
    }
}
