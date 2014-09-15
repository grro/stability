package eu.redzoo.article.javaworld.reliable.service.payment;

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
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;

import com.google.common.collect.ImmutableSet;

import eu.redzoo.article.javaworld.reliable.service.scoring.Score;
import eu.redzoo.article.javaworld.reliable.utils.jaxrs.client.CompletableClient;
import eu.redzoo.article.javaworld.reliable.utils.jaxrs.client.circuitbreaker.ClientCircutBreakerFilter;
import eu.redzoo.article.javaworld.reliable.utils.jaxrs.container.ResultConsumer;
import static eu.redzoo.article.javaworld.reliable.service.payment.PaymentMethod.*;
import static eu.redzoo.article.javaworld.reliable.service.scoring.Score.*;

  

@Singleton
@Path("Async/PaymentMethod")
public class AsyncPaymentMethodResource {

    private static final URI addressScoreURI = URI.create("http://localhost:9080/service/rest/AddressScore"); 

    
    private final CompletableClient client;
    private final AsyncPaymentDao dao; 
    
    // ...
    
    public AsyncPaymentMethodResource() {
        
        // uses HttpUrlConnector, internally
        
        
        ClientConfig clientConfig = new ClientConfig();                    // jersey specific
        clientConfig.connectorProvider(new ApacheConnectorProvider());     // jersey specific
        
        RequestConfig reqConfig = RequestConfig.custom()                   // apache HttpClient specific
                                               .setConnectTimeout(1000)
                                               .setSocketTimeout(1000)
                                               .setConnectionRequestTimeout(1000)
                                               .build();            
        
        clientConfig.property(ApacheClientProperties.REQUEST_CONFIG, reqConfig); // jersey specific

        client = new CompletableClient(ClientBuilder.newClient(clientConfig));
        client.register(new ClientCircutBreakerFilter());
        
        
        dao = new PaymentDaoImpl();
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
    public void getPaymentMethodsAsync(@QueryParam("addr") String address, @Suspended AsyncResponse asyncResponse) {
        
        dao.getPaymentsAsync(address, 50)
           .thenCompose(payments -> payments.isEmpty() ? 
              client.target(addressScoreURI).queryParam("addr", address).request().async().get(Score.class) :
              CompletableFuture.completedFuture((payments.stream().filter(payment -> payment.isDelayed()).count() > 1) ? NEGATIVE : POSITIVE))
           .exceptionally(error -> NEUTRAL)
           .thenApply(SCORE_TO_PAYMENTMETHOD)
           .whenComplete(ResultConsumer.write(asyncResponse));;
    }
}
