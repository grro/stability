package eu.redzoo.article.javaworld.reliable.utils.circuitbreaker;


import java.time.Duration;
import java.util.Map;

import jersey.repackaged.com.google.common.collect.Maps;



public class CircuitBreakerRegistry {

    private final Map<String, CircuitBreaker> circuitBreakerMap = Maps.newConcurrentMap();
    
    private final int maxEntries = 100;

    private final HealthPolicy healthPolicy;

    
    public CircuitBreakerRegistry(HealthPolicy healthPolicy) {
        this.healthPolicy = healthPolicy;
    }
    
    
    public CircuitBreaker get(String scope) {        
        if (scope == null) {
            scope = "__<NULL>__";
        }
        
        CircuitBreaker circuitBreaker = circuitBreakerMap.get(scope);
        if ((circuitBreaker == null) && (circuitBreakerMap.size() < maxEntries)) {
            circuitBreaker = new CircuitBreaker(scope, healthPolicy, Duration.ofSeconds(3));
            circuitBreakerMap.put(scope, circuitBreaker);
        }
        
        return circuitBreaker;
    }
}