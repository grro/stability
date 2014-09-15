package eu.redzoo.article.javaworld.reliable.utils.jaxrs.client.circuitbreaker;



import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

import eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.CircuitBreakerRegistry;
import eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.CircuitOpenedException;
import eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.metrics.MetricsRegistry;
import eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.metrics.Transaction;



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

        Transaction transaction = metricsRegistry.transactions(targetHost).newTransaction();
        requestContext.setProperty(TRANSACTION, transaction);
    }
    
    
    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        Transaction transaction = (Transaction) requestContext.getProperty(TRANSACTION);
        if (transaction != null) {
            boolean isSuccess = (responseContext.getStatus() < 500);
            transaction.close(isSuccess);
        }
    }
}
