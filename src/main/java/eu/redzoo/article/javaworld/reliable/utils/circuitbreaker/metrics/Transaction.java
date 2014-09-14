package eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.metrics;

import java.time.Duration;
import java.time.Instant;



public class Transaction  {
    private final Instant startTime = Instant.now();

    private volatile Instant endTime = null;
    private volatile boolean isFailed = false;

    
    public Instant getStarttime() {
        return startTime;
    }
    
    public boolean isFailed() {
        return isFailed;
    }
    
    public boolean isRunning() {
        return (endTime == null);
    }
    
    public Duration getConsumedMillis() {
        if (endTime == null) {
            return Duration.between(startTime, Instant.now());
        } else {
            return Duration.between(startTime, endTime);
        }
    }
    
    public void close(boolean isFailed) {
        endTime = Instant.now();
        this.isFailed = isFailed;
    }
    
    
    public static void close(Transaction transaction, boolean isFailed) {
        if (transaction != null) {
            transaction.close(isFailed);
        }
    }
}