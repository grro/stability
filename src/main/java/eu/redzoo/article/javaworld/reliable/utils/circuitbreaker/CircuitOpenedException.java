package eu.redzoo.article.javaworld.reliable.utils.circuitbreaker;



public class CircuitOpenedException extends RuntimeException {

    private static final long serialVersionUID = 2223053777879009788L;    
    
    public CircuitOpenedException(String message) {
        super(message);
    }
}

