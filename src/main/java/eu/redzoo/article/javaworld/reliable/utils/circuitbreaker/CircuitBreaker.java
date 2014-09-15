package eu.redzoo.article.javaworld.reliable.utils.circuitbreaker;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;




public class CircuitBreaker {
    
    private final String scope;
    private final Duration openStateTimeout;
    private final HealthPolicy policy;

    private AtomicReference<CircuitBreakerControl> control = new AtomicReference<>(); 

    // ..

    
    CircuitBreaker(String scope, HealthPolicy healthPolicy, Duration openStateTimeout) {
        this.scope = scope;
        this.policy = new CachedCircuitBreakerPolicy(healthPolicy, Duration.ofSeconds(3));
        this.openStateTimeout = openStateTimeout;

        control.set(new ClosedStateControl());
    }
    
    
    public boolean isRequestAllowed() {
        return control.get().isRequestAllowed();
    }
    

    
    private final class ClosedStateControl implements CircuitBreakerControl {
        
        @Override
        public boolean isRequestAllowed() {
            return (policy.isHealthy(scope)) ? true
                                             : changeState(new OpenStateControl()).isRequestAllowed();
        }
    }
    
    
     
    private final class OpenStateControl implements CircuitBreakerControl {
        private final Instant exitDate = Instant.now().plus(openStateTimeout);  
        
        @Override
        public boolean isRequestAllowed() {
            return (Instant.now().isAfter(exitDate)) ? changeState(new HalfOpenStateControl()).isRequestAllowed()
                                                     : false;
        }
    }

    
    
    private final class HalfOpenStateControl implements CircuitBreakerControl {
        private double chance = 0.05;  // 5% will be pass through

        @Override
        public boolean isRequestAllowed() {
            return (policy.isHealthy(scope)) ? changeState(new ClosedStateControl()).isRequestAllowed()
                                             : (new Random().nextDouble() <= chance);
        }
    }

    
    
    
    private CircuitBreakerControl changeState(CircuitBreakerControl newState) {
        control.set(newState);
        return newState;
    }
    
    private static interface CircuitBreakerControl {        
        boolean isRequestAllowed();       
    }
    
    

    
    private class CachedCircuitBreakerPolicy implements HealthPolicy  {
        
        private final HealthPolicy healthPolicy;
        private final Duration cacheTtl;
        
        private final Cache<String, CachedResult> cache = CacheBuilder.newBuilder().maximumSize(1000).build();
        
        
        public CachedCircuitBreakerPolicy(HealthPolicy healthPolicy, Duration cacheTtl) {
            this.healthPolicy = healthPolicy;
            this.cacheTtl = cacheTtl;
        }
        
        
        @Override
        public boolean isHealthy(String scope) {
            
            CachedResult cachedResult = cache.getIfPresent(scope);
            if ((cachedResult == null) || (cachedResult.isExpired())) {
                cachedResult = new CachedResult(healthPolicy.isHealthy(scope), cacheTtl);
                cache.put(scope, cachedResult);
            }

            return cachedResult.isHealthy();
        }  
        
        
        private class CachedResult {
            private final boolean isHealthy;
            private Instant validTo;
            
            
            CachedResult(boolean isHealthy, Duration ttl) {
                this.isHealthy = isHealthy;
                validTo = Instant.now().plus(ttl);
            }
            
            
            public boolean isExpired() {
                return Instant.now().isAfter(validTo);
            }
            
            public boolean isHealthy() {
                return isHealthy;
            }
        }
    }
}

