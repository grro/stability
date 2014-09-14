package eu.redzoo.article.javaworld.reliable.utils.jaxrs.container;


import java.time.Duration;


import eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.metrics.TransactionMetrics;
import eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.policy.CircuitBreakerPolicy;
import eu.redzoo.article.javaworld.reliable.utils.jaxrs.container.environment.Environment;
import eu.redzoo.article.javaworld.reliable.utils.jaxrs.container.environment.TomcatEnvironment;
import eu.redzoo.article.javaworld.reliable.utils.jaxrs.container.environment.Environment.Threadpool;


class OverloadBasedCircutBreakerPolicy implements CircuitBreakerPolicy  {
 
    private final Environment environment = new TomcatEnvironment();

    private final Duration thresholdSlowTransaction;

    
    public OverloadBasedCircutBreakerPolicy(Duration thresholdSlowTransaction) {
        this.thresholdSlowTransaction = thresholdSlowTransaction;
    }

    
    
    @Override
    public boolean isClosed(TransactionMetrics metrics) {
       
        // [1] all servlet container threads taken (of largest pool)?
        Threadpool largestPool = environment.getThreadpoolUsage().stream().sorted((p1, p2) -> (p2.getMaxThreads() - p1.getMaxThreads())).findFirst().get();
        if (largestPool.getCurrentThreadsBusy() >= largestPool.getMaxThreads()) {

            // [2] more than 90% currently consumend by this?
            if (metrics.getRecordedTransactions().running().size() > (largestPool.getMaxThreads() * 0.9)) {

                // [3] 50percentile is higher than slow threshold
                Duration currentp50percentile = metrics.getRecordedTransactions().ofLast(Duration.ofMinutes(3)).percentile(50);
                if (thresholdSlowTransaction.minus(currentp50percentile).isNegative()) {
                    return false;
                }
            }
        }
        
        return true;
    } 
}

