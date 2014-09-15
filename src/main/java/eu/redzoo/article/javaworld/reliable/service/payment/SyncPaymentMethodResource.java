package eu.redzoo.article.javaworld.reliable.service.payment;

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

import eu.redzoo.article.javaworld.reliable.service.scoring.Score;
import eu.redzoo.article.javaworld.reliable.utils.jaxrs.client.circuitbreaker.ClientCircutBreakerFilter;
import static eu.redzoo.article.javaworld.reliable.service.payment.PaymentMethod.*;
import static eu.redzoo.article.javaworld.reliable.service.scoring.Score.*;

  

@Singleton
@Path("PaymentMethod")
public class SyncPaymentMethodResource {

    private static final Logger LOG = Logger.getLogger(SyncPaymentMethodResource.class.getName());
    
    private static final URI addrScoreURI = URI.create("http://localhost:9080/service/rest/AddressScore"); 

    private final Client client;
    private final PaymentDao paymentDao; 
    
    
    
    
    // ...
    
    public SyncPaymentMethodResource() {
        
        // uses HttpUrlConnector, internally
        
        
        ClientConfig clientConfig = new ClientConfig();                    // jersey specific
        clientConfig.connectorProvider(new ApacheConnectorProvider());     // jersey specific
        
        RequestConfig reqConfig = RequestConfig.custom()                   // apache HttpClient specific
                                               .setConnectTimeout(1000)
                                               .setSocketTimeout(1000)
                                               .setConnectionRequestTimeout(1000)
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

    
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public ImmutableSet<PaymentMethod> getPaymentMethods(@QueryParam("addr") String address) {
        Score score = NEUTRAL; 
        try { 
            ImmutableList<Payment> payments = paymentDao.getPayments(address, 50);
            score = payments.isEmpty() ? client.target(addrScoreURI).queryParam("addr", address).request().get(Score.class)
                                       : (payments.stream().filter(payment -> payment.isDelayed()).count() >= 1) ? NEGATIVE : POSITIVE;
        } catch (RuntimeException rt) {
            LOG.fine("error occurred by calculating score. Fallback to NEUTRAL " + rt.toString());
        }
        
        return SCORE_TO_PAYMENTMETHOD.apply(score);
    }
}
