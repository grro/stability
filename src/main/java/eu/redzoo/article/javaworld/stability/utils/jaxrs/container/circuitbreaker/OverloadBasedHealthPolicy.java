/*
 * Copyright (c) 2014, Gregor Roth, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
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
            
            // [2] more than 90% currently consumed by this operation?
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

