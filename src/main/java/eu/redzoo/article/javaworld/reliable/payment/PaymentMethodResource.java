package eu.redzoo.article.javaworld.reliable.payment;

import java.net.URI;

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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import eu.redzoo.article.javaworld.reliable.scoring.Score;
import eu.redzoo.article.javaworld.reliable.utils.jaxrs.client.ClientCircutBreakerFilter;
import static eu.redzoo.article.javaworld.reliable.scoring.Score.*;
import static eu.redzoo.article.javaworld.reliable.payment.PaymentMethod.*;

  

@Singleton
@Path("PaymentMethod")
public class PaymentMethodResource {

    private static final Logger LOG = Logger.getLogger(PaymentMethodResource.class.getName());
    
    private static final URI addressScoreURI = URI.create("http://localhost:9080/service/rest/AddressScore"); 

    
    private final static ImmutableMap<Score, ImmutableSet<PaymentMethod>> SCORE_TO_PAYMENTMETHOD =
            ImmutableMap.of(POSITIVE, ImmutableSet.of(CREDITCARD, PAYPAL, PREPAYMENT, INVOCE),
                            NEGATIVE, ImmutableSet.of(PREPAYMENT),
                            NEUTRAL, ImmutableSet.of(CREDITCARD, PAYPAL, PREPAYMENT));
    
    
    private final Client client;
    private final PaymentDao paymentDao; 
    
    
    
    
    // ...
    
    public PaymentMethodResource() {
        
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
    

    
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public ImmutableSet<PaymentMethod> getPaymentMethods(@QueryParam("name") String name,
                                                         @QueryParam("dateOfBirth") String dateOfBirth,
                                                         @QueryParam("address") String address) {
      
        Score score = calculateScore(name, dateOfBirth, address);
        return SCORE_TO_PAYMENTMETHOD.get(score);
    }

    
        
    private Score calculateScore(String name, String dateOfBirth, String address) {
        
        try { 
            
            // try to get payment history from database (max 50 newest entries)
            ImmutableList<Payment> payments = paymentDao.getPayments(name, dateOfBirth, address, 50);
            
            // no history (new user)? -> query external address score service
            if (payments.isEmpty()) {
                return client.target(addressScoreURI)
                             .queryParam("lastName", name)
                             .queryParam("dateOfBirth", dateOfBirth)
                             .queryParam("address", address)
                             .request()
                             .get(Score.class);
    
            // no, it is already a known customer 
            } else {
                return (payments.stream().filter(payment -> payment.isDelayed()).count() > 1) ? NEGATIVE : POSITIVE;
            }
            
        } catch (RuntimeException rt) {
            LOG.fine("error occurred by calculating score. Fallback to NEUTRAL " + rt.toString());
            return NEUTRAL;
        }
    }
}
