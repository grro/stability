/*
 * Copyright (c) 2014 Gregor Roth
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.redzoo.article.javaworld.stability.utils.jaxrs.container.circuitbreaker;


import java.time.Duration;


import eu.redzoo.article.javaworld.stability.utils.circuitbreaker.HealthPolicy;
import eu.redzoo.article.javaworld.stability.utils.circuitbreaker.metrics.MetricsRegistry;
import eu.redzoo.article.javaworld.stability.utils.circuitbreaker.metrics.TransactionMetrics;
import eu.redzoo.article.javaworld.stability.utils.jaxrs.container.Environment;
import eu.redzoo.article.javaworld.stability.utils.jaxrs.container.Environment.Threadpool;
import eu.redzoo.article.javaworld.stability.utils.jaxrs.container.tomcat.TomcatEnvironment;



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
            
            // [2] more than 80% currently consumed by this operation?
            if (metrics.running().size() > (pool.getMaxThreads() * 0.8)) {

                // [3] is 50percentile higher than slow threshold?
                Duration current50percentile = metrics.ofLast(Duration.ofMinutes(3)).percentile(50);
                if (thresholdSlowTransaction.minus(current50percentile).isNegative()) {
                    return false;
                }
            }
        }
        
        return true;
    } 
}

