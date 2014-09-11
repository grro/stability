package eu.redzoo.article.javaworld.reliable;






import java.io.File;

import javax.ws.rs.QueryParam;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class SimpleTest {

    private static Server server;
  
    
    @BeforeClass
    public static void setUp() throws Exception {
        
        server = new Server(9080);
        WebAppContext webapp = new WebAppContext(new File("src/main/resources/webapp").getAbsolutePath(), "/service");
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setWelcomeFiles(new String[]{ "index.html" });
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resource_handler, webapp});
        server.setHandler(handlers);

        server.start();
    }

    

    @AfterClass
    public static void tearDown() throws Exception {
        server.stop();
    }

    

    @Test
    public void testSimple() throws Exception {
        System.out.println("http://localhost:9080/service/rest/AddressScore?firstName=Michael&lastName=Smith&address=1736+Richmond+Ave+Str+2+Wappingers+FL,+NY+12990-9103");
        
        
        
    }
}
