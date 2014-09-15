package eu.redzoo.article.javaworld.reliable.utils.jaxrs.container.handshaking;





import eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.metrics.MetricsRegistry;



public class FlowController {

    private final MetricsRegistry metricsRegistry;

    
    public FlowController(MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;
    }
    
    
    public boolean isTooManyRequests(String scope) {
        return true;
    }
}
