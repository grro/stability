package eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.metrics;

import java.util.Map;

import jersey.repackaged.com.google.common.collect.Maps;



public class MetricsRegistry {
    
    private final Map<String, TransactionMetrics> metricsMap = Maps.newConcurrentMap();
    
    private final int maxEntries = 100;
    private final int bufferSize = 1000;

    public TransactionMetrics transactions(String scope) {  
        if (scope == null) {
            scope = "__<NULL>__";
        }
        
        TransactionMetrics metrics = metricsMap.get(scope);
        if ((metrics == null) && (metricsMap.size() < maxEntries)) {
            metrics = new TransactionMetrics(bufferSize);
            metricsMap.put(scope, metrics);
        }
        
        return metrics;
    }
}

