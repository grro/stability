package eu.redzoo.article.javaworld.reliable.utils.jaxrs.container;



import java.util.concurrent.CompletionException;
import java.util.function.BiConsumer;

import javax.ws.rs.container.AsyncResponse;




public class ResultConsumer implements BiConsumer<Object, Throwable> {
    
    private final AsyncResponse asyncResponse;
    
    private ResultConsumer(AsyncResponse asyncResponse) {
        this.asyncResponse = asyncResponse;
    }

    public static final BiConsumer<Object, Throwable> write(AsyncResponse asyncResponse) {
        return new ResultConsumer(asyncResponse);
    }
    
    @Override
    public void accept(Object result, Throwable error) {
        if (error == null) {
            asyncResponse.resume(result);            
        } else {
            asyncResponse.resume(unwrapIfNecessary(error, 10));
        }
    }
    
    
    private static Throwable unwrapIfNecessary(Throwable ex, int maxDepth)  {
        if (CompletionException.class.isAssignableFrom(ex.getClass())) {
            Throwable e = ((CompletionException) ex).getCause();

            if (maxDepth > 1) {
                return unwrapIfNecessary(e, maxDepth - 1);
            } else {
                return e;
            }
        } else {
            return ex;
        }
    }
}
