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



import java.io.IOException;
import java.time.Duration;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import eu.redzoo.article.javaworld.stability.utils.circuitbreaker.CircuitBreakerRegistry;
import eu.redzoo.article.javaworld.stability.utils.circuitbreaker.CircuitOpenedException;
import eu.redzoo.article.javaworld.stability.utils.circuitbreaker.metrics.MetricsRegistry;
import eu.redzoo.article.javaworld.stability.utils.circuitbreaker.metrics.Transaction;



@Provider
public class ContainerCircutBreakerFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String TRANSACTION = "circuit_breaker_transaction";

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final MetricsRegistry metricsRegistry;


    @Context 
    private ResourceInfo resourceInfo; 

    //..

    public ContainerCircutBreakerFilter() {
        metricsRegistry = new MetricsRegistry();
        Duration thresholdSlowTransaction = Duration.ofSeconds(5); 
        circuitBreakerRegistry = new CircuitBreakerRegistry(new OverloadBasedHealthPolicy(metricsRegistry, thresholdSlowTransaction));
    }
    
    
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String targetOperation = resourceInfo.getResourceClass().getName() + "#" + resourceInfo.getResourceClass().getName(); 
        
        if (!circuitBreakerRegistry.get(targetOperation).isRequestAllowed()) {
            throw new CircuitOpenedException("circuit is open");
        }
        
        Transaction transaction = metricsRegistry.transactions(targetOperation).newTransaction();
        requestContext.setProperty(TRANSACTION, transaction);
    }

       
     
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        boolean isFailed = responseContext.getStatus() >= 500;
        Transaction.close(requestContext.getProperty(TRANSACTION), isFailed);
    }
}
