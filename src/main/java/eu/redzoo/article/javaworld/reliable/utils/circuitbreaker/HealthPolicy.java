package eu.redzoo.article.javaworld.reliable.utils.circuitbreaker;




public interface HealthPolicy  {
 
    boolean isHealthy(String scope);
}

