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
package eu.redzoo.article.javaworld.stability.utils.jaxrs.client.circuitbreaker;

import java.time.Duration;

import eu.redzoo.article.javaworld.stability.utils.circuitbreaker.HealthPolicy;
import eu.redzoo.article.javaworld.stability.utils.circuitbreaker.metrics.MetricsRegistry;
import eu.redzoo.article.javaworld.stability.utils.circuitbreaker.metrics.Transactions;


public class ErrorRateBasedHealthPolicy implements HealthPolicy  {
  
    private final MetricsRegistry metricsRegistry;
    private final int thresholdMinReqPerMin;
    
    // ...
  
    public ErrorRateBasedHealthPolicy(MetricsRegistry metricsRegistry) {
        this(metricsRegistry, 30);
    }

    
    public ErrorRateBasedHealthPolicy(MetricsRegistry metricsRegistry, int thresholdMinRatePerMin) {
        this.metricsRegistry = metricsRegistry;
        this.thresholdMinReqPerMin = thresholdMinRatePerMin;
    }
  
    
    @Override
    public boolean isHealthy(String scope) {
        Transactions transactions =  metricsRegistry.transactions(scope).ofLast(Duration.ofMinutes(1));

        return ! ((transactions.size() > thresholdMinReqPerMin) &&        // check threshold reached?
                  (transactions.failed().size() == transactions.size()) && // every call failed?
                  (true)                                    );             // client connection pool limit reached?
    }
}

