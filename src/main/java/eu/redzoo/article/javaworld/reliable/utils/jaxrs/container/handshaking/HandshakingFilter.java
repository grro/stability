package eu.redzoo.article.javaworld.reliable.utils.jaxrs.container.handshaking;



import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.metrics.MetricsRegistry;
import eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.metrics.Transaction;



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

       
        Transaction transaction = (Transaction) requestContext.getProperty(TRANSACTION);
        if (transaction != null) {
            transaction.close(responseContext.getStatus() < 500);
        }
    }
}
