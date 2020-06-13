import java.time.Duration;
import java.time.Instant;

public class Tracker {
    private static Instant lastBatchTime = Instant.now();

    public static final Duration TIMEOUT = Duration.ofSeconds(10);

    public static void receivedBatch() {
        lastBatchTime = Instant.now();
    }

    public static boolean isFENodeDown() {
        Duration timeSinceLastBatch = Duration.between(lastBatchTime, Instant.now());
        return timeSinceLastBatch.compareTo(TIMEOUT) > 0;
    }
}