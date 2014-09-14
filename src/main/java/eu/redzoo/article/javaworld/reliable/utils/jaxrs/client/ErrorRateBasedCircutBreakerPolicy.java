package eu.redzoo.article.javaworld.reliable.utils.jaxrs.client;

import java.time.Duration;


import eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.metrics.TransactionMetrics;
import eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.metrics.Transactions;
import eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.policy.CircuitBreakerPolicy;


public class ErrorRateBasedCircutBreakerPolicy implements CircuitBreakerPolicy  {
  
    private final int thresholdMinRatePerSec;
  
    public ErrorRateBasedCircutBreakerPolicy() {
        this(30);
    }

    
    public ErrorRateBasedCircutBreakerPolicy(int thresholdMinRatePerSec) {
        this.thresholdMinRatePerSec = thresholdMinRatePerSec;
    }
  
    
    @Override
    public boolean isClosed(TransactionMetrics metrics) {
        Transactions recorded = metrics.getRecordedTransactions().ofLast(Duration.ofSeconds(60));    
        if (recorded.size() > thresholdMinRatePerSec) {
            return recorded.failed().size() < recorded.size();
        }
        
        return true;
    }
}

