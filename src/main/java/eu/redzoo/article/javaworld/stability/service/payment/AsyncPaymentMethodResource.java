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
package eu.redzoo.article.javaworld.stability.service.payment;


import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

import org.apache.http.client.config.RequestConfig;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.grizzly.connector.GrizzlyConnectorProvider;

import com.google.common.collect.ImmutableSet;

import eu.redzoo.article.javaworld.stability.service.scoring.Score;
import eu.redzoo.article.javaworld.stability.utils.jaxrs.client.Java8Client;
import eu.redzoo.article.javaworld.stability.utils.jaxrs.client.ResultConsumer;
import eu.redzoo.article.javaworld.stability.utils.jaxrs.client.circuitbreaker.ClientCircutBreakerFilter;
import static eu.redzoo.article.javaworld.stability.service.payment.PaymentMethod.*;
import static eu.redzoo.article.javaworld.stability.service.scoring.Score.*;

  

@Singleton
@Path("Async/PaymentMethod")
public class AsyncPaymentMethodResource {

    private static final URI addrScoreURI = URI.create("http://localhost:9080/service/rest/AddressScore"); 

    
    private final Java8Client client;
    private final AsyncPaymentDao paymentDao; 
    
    // ...
    
    public AsyncPaymentMethodResource() {
        ClientConfig clientConfig = new ClientConfig();                    // jersey specific
        clientConfig.connectorProvider(new GrizzlyConnectorProvider());    // jersey specific
//        clientConfig.connectorProvider(new ApacheConnectorProvider());     // jersey specific
        
        RequestConfig reqConfig = RequestConfig.custom()                   // apache HttpClient specific
                                               .setConnectTimeout(1000)
                                               .setSocketTimeout(1000)
                                               .setConnectionRequestTimeout(100)
                                               .build();            
        
  //      clientConfig.property(ApacheClientProperties.REQUEST_CONFIG, reqConfig); // jersey specific

        
        // use extended client (JAX-RS 2.0 client does not support CompletableFutures)
        client = Java8Client.newClient(ClientBuilder.newClient(clientConfig)); 
        client.register(new ClientCircutBreakerFilter());
        
        
        paymentDao = new PaymentDaoImpl();
    }
    

    

    private final static Function<Score, ImmutableSet<PaymentMethod>> SCORE_TO_PAYMENTMETHOD = score ->  {
                            
                            switch (score) {
                            case POSITIVE:
                                return ImmutableSet.of(CREDITCARD, PAYPAL, PREPAYMENT, INVOCE);
                            case NEGATIVE:
                                return ImmutableSet.of(PREPAYMENT);
                            default:
                                return  ImmutableSet.of(CREDITCARD, PAYPAL, PREPAYMENT);
                            }
    };

    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public void getPaymentMethodsAsync(@QueryParam("addr") String address, @Suspended AsyncResponse resp) {
        paymentDao.getPaymentsAsync(address, 50)
           .thenCompose(pmts -> pmts.isEmpty() 
              ? client.target(addrScoreURI).queryParam("addr", address).request().async().get(Score.class) 
              : CompletableFuture.completedFuture((pmts.stream().filter(pmt -> pmt.isDelayed()).count() > 1) ? NEGATIVE : POSITIVE))
           .exceptionally(error -> 
           NEUTRAL)
           .thenApply(SCORE_TO_PAYMENTMETHOD)
           .whenComplete(ResultConsumer.write(resp));  // writes result/error into async response 
    }
}
