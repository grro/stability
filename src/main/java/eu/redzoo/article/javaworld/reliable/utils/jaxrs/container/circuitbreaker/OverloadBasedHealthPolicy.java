package eu.redzoo.article.javaworld.reliable.utils.jaxrs.container.circuitbreaker;


import java.time.Duration;

import eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.HealthPolicy;
import eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.metrics.MetricsRegistry;
import eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.metrics.TransactionMetrics;
import eu.redzoo.article.javaworld.reliable.utils.jaxrs.container.environment.Environment;
import eu.redzoo.article.javaworld.reliable.utils.jaxrs.container.environment.TomcatEnvironment;
import eu.redzoo.article.javaworld.reliable.utils.jaxrs.container.environment.Environment.Threadpool;


public class OverloadBasedHealthPolicy implements HealthPolicy  {
 
    private final Environment environment = new TomcatEnvironment();

    private final MetricsRegistry metricsRegistry;
    private final Duration thresholdSlowTransaction;

    
    //...
    
    public OverloadBasedHealthPolicy(MetricsRegistry metricsRegistry, Duration thresholdSlowTransaction) {
        this.metricsRegistry = metricsRegistry;
        this.thresholdSlowTransaction = thresholdSlowTransaction;
    }

    
    
    @Override
    public boolean isHealthy(String scope) {
       
        // [1] all servlet container threads taken?
        Threadpool pool = environment.getThreadpoolUsage();
        if (pool.getCurrentThreadsBusy() >= pool.getMaxThreads()) {
            TransactionMetrics metrics = metricsRegistry.transactions(scope);
            
            // [2] more than 90% currently consumed by this operation?
            if (metrics.recorded().running().size() > (pool.getMaxThreads() * 0.8)) {

                // [3] is 50percentile higher than slow threshold?
                Duration current50percentile = metrics.recorded().ofLast(Duration.ofMinutes(3)).percentile(50);
                if (thresholdSlowTransaction.minus(current50percentile).isNegative()) {
                    return false;
                }
            }
        }
        
        return true;
    } 
}

