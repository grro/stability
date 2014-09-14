package eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.policy;

import java.time.Duration;
import java.time.Instant;

import eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.metrics.TransactionMetrics;



public class CachedCircutBreakerPolicy implements CircuitBreakerPolicy  {
 
    private final CircuitBreakerPolicy policy;
    
    private volatile boolean cachedIsErroneous = false;
    private volatile Instant cacheTime = Instant.ofEpochMilli(0);
    
    
    public CachedCircutBreakerPolicy(CircuitBreakerPolicy policy) {
        this.policy = policy;
    }
    
    
    @Override
    public boolean isClosed(TransactionMetrics metrics) {
        
        // cache expired?
        if (!Duration.between(cacheTime, Instant.now()).minusSeconds(1).isNegative()) {
            cachedIsErroneous = policy.isClosed(metrics);
            cacheTime = Instant.now();
        }
        
        return cachedIsErroneous;
    }
   
}

