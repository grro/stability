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
        requestContext.setProperty(TRANSACTION, metricsRegistry.transactions(clientId).openTransaction());
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
