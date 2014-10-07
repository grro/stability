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
