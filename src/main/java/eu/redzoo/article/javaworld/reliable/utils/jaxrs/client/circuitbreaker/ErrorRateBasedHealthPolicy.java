package eu.redzoo.article.javaworld.reliable.utils.jaxrs.client.circuitbreaker;

import java.time.Duration;

import eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.HealthPolicy;
import eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.metrics.MetricsRegistry;
import eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.metrics.Transactions;


public class ErrorRateBasedHealthPolicy implements HealthPolicy  {
  
    private final MetricsRegistry metricsRegistry;
    private final int thresholdMinRatePerMin;
    
    // ...
  
    public ErrorRateBasedHealthPolicy(MetricsRegistry metricsRegistry) {
        this(metricsRegistry, 30);
    }

    
    public ErrorRateBasedHealthPolicy(MetricsRegistry metricsRegistry, int thresholdMinRatePerMin) {
        this.metricsRegistry = metricsRegistry;
        this.thresholdMinRatePerMin = thresholdMinRatePerMin;
    }
  
    
    @Override
    public boolean isHealthy(String scope) {
        Transactions recorded =  metricsRegistry.transactions(scope)
                                                .recorded()
                                                .ofLast(Duration.ofSeconds(60));
        
        if ( (recorded.size() > thresholdMinRatePerMin) &&     // check threshold reached?
             (recorded.failed().size() == recorded.size()) &&  // every call failed?
             (true)                                    ) {     // client connection pool limit reached?
            return false;
        }
        
        return true;
    }
}

