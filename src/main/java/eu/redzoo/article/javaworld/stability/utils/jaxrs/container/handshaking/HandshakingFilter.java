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
package eu.redzoo.article.javaworld.stability.utils.jaxrs.container.handshaking;



import java.io.IOException;


import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import eu.redzoo.article.javaworld.stability.utils.circuitbreaker.metrics.MetricsRegistry;
import eu.redzoo.article.javaworld.stability.utils.circuitbreaker.metrics.Transaction;



@Provider
public class HandshakingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String TRANSACTION = "handshaking_transaction";

    
    private final FlowController flowController;
    private final MetricsRegistry metricsRegistry;

    // ...

    public HandshakingFilter() {
        metricsRegistry = new MetricsRegistry();
        flowController = new FlowController(metricsRegistry);
    }
    
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String clientId = requestContext.getHeaderString("X-Client");
        requestContext.setProperty(TRANSACTION, metricsRegistry.transactions(clientId).newTransaction());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String clientId = requestContext.getHeaderString("X-Client");
        if (flowController.isTooManyRequests(clientId)) {
            responseContext.getHeaders().add("X-FlowControl", "slowdown");
        }
       
        Transaction.close(requestContext.getProperty(TRANSACTION), responseContext.getStatus() >= 500);
    }
}
