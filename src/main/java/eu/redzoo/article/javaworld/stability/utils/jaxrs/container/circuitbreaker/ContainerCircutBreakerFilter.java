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
        String scope = resourceInfo.getResourceClass().getName() + "#" + resourceInfo.getResourceClass().getName(); 
        
        if (!circuitBreakerRegistry.get(scope).isRequestAllowed()) {
            throw new CircuitOpenedException("circuit is open");
        }
        
        Transaction transaction = metricsRegistry.transactions(scope).openTransaction();
        requestContext.setProperty(TRANSACTION, transaction);
    }

       
     
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        boolean isFailed = responseContext.getStatus() >= 500;
        Transaction.close(requestContext.getProperty(TRANSACTION), isFailed);
    }
}
