package eu.redzoo.article.javaworld.reliable.utils.circuitbreaker;


import java.util.Map;

import jersey.repackaged.com.google.common.collect.Maps;
import eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.metrics.MetricsRegistry;
import eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.policy.CircuitBreakerPolicy;




public class CircuitBreakerRegistry {

    private final Map<String, CircuitBreaker> circuitBreakerMap = Maps.newConcurrentMap();
    
    private final int maxEntries = 100;

    private final CircuitBreakerPolicy policy;
    private final MetricsRegistry metricsRegistry;
    
    
    public CircuitBreakerRegistry(MetricsRegistry metricsRegistry, CircuitBreakerPolicy policy) {
        this.metricsRegistry = metricsRegistry;
        this.policy = policy;
    }
    
    
    public CircuitBreaker get(String scope) {        
        CircuitBreaker circuitBreaker = circuitBreakerMap.get(scope);
        if ((circuitBreaker == null) && (circuitBreakerMap.size() < maxEntries)) {
            circuitBreaker = new CircuitBreaker(metricsRegistry.transactions(scope), policy);
            circuitBreakerMap.put(scope, circuitBreaker);
        }
        
        return circuitBreaker;
    }
}