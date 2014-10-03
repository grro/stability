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



import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

import eu.redzoo.article.javaworld.stability.utils.circuitbreaker.CircuitBreakerRegistry;
import eu.redzoo.article.javaworld.stability.utils.circuitbreaker.CircuitOpenedException;
import eu.redzoo.article.javaworld.stability.utils.circuitbreaker.metrics.MetricsRegistry;
import eu.redzoo.article.javaworld.stability.utils.circuitbreaker.metrics.Transaction;



public class ClientCircutBreakerFilter implements ClientRequestFilter, ClientResponseFilter  {

    private static final String TRANSACTION = "transaction";
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final MetricsRegistry metricsRegistry;
    
    // ..
    
    
    public ClientCircutBreakerFilter() {
        metricsRegistry = new MetricsRegistry();
        circuitBreakerRegistry = new CircuitBreakerRegistry(new ErrorRateBasedHealthPolicy(metricsRegistry));
    }
    
    
    @Override
    public void filter(ClientRequestContext requestContext) throws IOException, CircuitOpenedException {
        String targetHost = requestContext.getUri().getHost();

        if (!circuitBreakerRegistry.get(targetHost).isRequestAllowed()) {
            throw new CircuitOpenedException("circuit is open");
        }

        Transaction transaction = metricsRegistry.transactions(targetHost).openTransaction();
        requestContext.setProperty(TRANSACTION, transaction);
    }
    
    
    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        boolean isFailed = (responseContext.getStatus() >= 500);
        Transaction.close(requestContext.getProperty(TRANSACTION), isFailed);
    }
}
