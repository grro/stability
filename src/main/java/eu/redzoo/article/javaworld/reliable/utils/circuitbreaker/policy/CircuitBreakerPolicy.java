package eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.policy;

import eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.metrics.TransactionMetrics;



public interface CircuitBreakerPolicy  {
 
    boolean isClosed(TransactionMetrics metrics);
}

