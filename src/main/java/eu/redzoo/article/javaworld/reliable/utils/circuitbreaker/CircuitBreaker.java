package eu.redzoo.article.javaworld.reliable.utils.circuitbreaker;

import java.time.Duration;
import java.time.Instant;

import eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.metrics.TransactionMetrics;
import eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.policy.CircuitBreakerPolicy;




public class CircuitBreaker {
    
    private final TransactionMetrics metrics;
    private final CircuitBreakerControl closedStateControl;
    
    private CircuitBreakerControl control; 
    
    
    
    public CircuitBreaker(TransactionMetrics metrics, CircuitBreakerPolicy policy) {
        closedStateControl = new ClosedStateControl(policy);
        control = closedStateControl;
        this.metrics = metrics;
    }
    
    
    public boolean isClosed() {
        return control.isClosed();
    }
    
    
    
    private static interface CircuitBreakerControl {        
        boolean isClosed();       
    }
    
    
    
    private final class ClosedStateControl implements CircuitBreakerControl {
        
        private final CircuitBreakerPolicy policy; 

        public ClosedStateControl(CircuitBreakerPolicy policy) {
            this.policy = new CachedCircuitBreakerPolicy(policy, Duration.ofSeconds(3));
        }
        
        @Override
        public boolean isClosed() {
            
            // still closed?
            if (policy.isClosed(metrics)) {
                return true;
                
            // .. no, enter (half) open state    
            } else {
                control = new OpenStateControl();
                return control.isClosed();
            }
        }
    }
    
    
   
    
    private final class OpenStateControl implements CircuitBreakerControl {
        private final Instant enterDate = Instant.now();
        private Instant minTimeNextProbe = Instant.now().plus(Duration.ofSeconds(5));   // next 10 seconds every call will be rejected 
        

        @Override
        public boolean isClosed() {
            
            // probe OK??
            if (Instant.now().isAfter(minTimeNextProbe)) {
                minTimeNextProbe = computeNextProbeTime();
                return true;
            } else {
                return false;
            }
        }
        
                
        private Instant computeNextProbeTime() {
            Instant now = Instant.now();
            Duration inStateSince = Duration.between(enterDate, now);
            
            // [< +5 sec] every call will be rejected
            if (inStateSince.minusSeconds(5).isNegative()) {
                return enterDate.plusSeconds(5);
                
            // [< +10 sec] pause of 1 sec between calls                  
            } if (inStateSince.minusSeconds(10).isNegative()) {
                return now.plusMillis(1);
                
            // [< +15 sec] pause of 200 millis between calls                
            } if (inStateSince.minusSeconds(15).isNegative()) {
                return now.plusMillis(500);
                
            // [> +15 sec] switch to closed state                
            } else {
                control = closedStateControl;
                return now;
            }
        }
    }

    

    
    private class CachedCircuitBreakerPolicy implements CircuitBreakerPolicy  {
        
        private final CircuitBreakerPolicy policy;
        private final Duration maxCachedTime;
        
        private volatile boolean cachedIsErroneous = false;
        private volatile Instant cacheTime = Instant.ofEpochMilli(0);
        
        
        public CachedCircuitBreakerPolicy(CircuitBreakerPolicy policy, Duration maxCachedTime) {
            this.policy = policy;
            this.maxCachedTime = maxCachedTime;
        }
        
        
        @Override
        public boolean isClosed(TransactionMetrics metrics) {
            
            // cache expired?
            if (!Duration.between(cacheTime, Instant.now()).minus(maxCachedTime).isNegative()) {
                cachedIsErroneous = policy.isClosed(metrics);
                cacheTime = Instant.now();
            }
            
            return cachedIsErroneous;
        }       
    }
}

