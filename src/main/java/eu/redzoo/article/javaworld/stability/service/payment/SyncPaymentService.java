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
package eu.redzoo.article.javaworld.stability.service.payment;

import java.net.URI;
import java.util.function.Function;
import java.util.logging.Logger;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

import org.apache.http.client.config.RequestConfig;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import eu.redzoo.article.javaworld.stability.service.scoring.Score;
import eu.redzoo.article.javaworld.stability.utils.jaxrs.client.circuitbreaker.ClientCircutBreakerFilter;
import static eu.redzoo.article.javaworld.stability.service.payment.PaymentMethod.*;
import static eu.redzoo.article.javaworld.stability.service.scoring.Score.*;

  

@Singleton
@Path("sync")
public class SyncPaymentService {

    private static final Logger LOG = Logger.getLogger(SyncPaymentService.class.getName());
    
    private static final URI creditScoreURI = URI.create("http://localhost:9080/service/rest/creditscores"); 

    private final Client client;
    private final PaymentDao paymentDao; 
    
    
    
    
    // ...
    
    public SyncPaymentService() {
        ClientConfig clientConfig = new ClientConfig();                    // jersey specific
        clientConfig.connectorProvider(new ApacheConnectorProvider());     // jersey specific
        
        RequestConfig reqConfig = RequestConfig.custom()                   // apache HttpClient specific
                                               .setConnectTimeout(1000)
                                               .setSocketTimeout(1000)
                                               .setConnectionRequestTimeout(100)
                                               .build();            
        
        clientConfig.property(ApacheClientProperties.REQUEST_CONFIG, reqConfig); // jersey specific

        client = ClientBuilder.newClient(clientConfig);
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

    
    @Path("paymentmethods")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ImmutableSet<PaymentMethod> getPaymentMethods(@QueryParam("addr") String address) {
        Score score = NEUTRAL; 
        try { 
            ImmutableList<Payment> pmts = paymentDao.getPayments(address, 50);
            score = pmts.isEmpty()
                       ? client.target(creditScoreURI).queryParam("addr", address).request().get(Score.class)
                       : (pmts.stream().filter(pmt -> pmt.isDelayed()).count() >= 1) ? NEGATIVE : POSITIVE;
        } catch (RuntimeException rt) {
            LOG.fine("error occurred by calculating score. Fallback to NEUTRAL " + rt.toString());
        }
        
        return SCORE_TO_PAYMENTMETHOD.apply(score);
    }
}
