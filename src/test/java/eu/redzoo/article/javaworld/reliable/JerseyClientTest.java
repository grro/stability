package eu.redzoo.article.javaworld.reliable;






import java.io.File;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.InvocationCallback;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class JerseyClientTest {

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
    public void testSync() throws Exception {
        
        Client client = ClientBuilder.newClient();
        String score = client.target("http://localhost:9080/service/rest/AddressScore")
              .queryParam("firstName", "Michael")
              .queryParam("lastName", "Smith")
              .queryParam("address", "1736 Richmond Ave 2 Wappingers FL, NY 12990-9103")
              .request()
              .get(String.class);
        
        System.out.println(score);
    }
    


    @Test
    public void testAsync() throws Exception {
        
        Client client = ClientBuilder.newClient();
        
        InvocationCallback<String> callback = new InvocationCallback<String>() {
            
            @Override
            public void completed(String response) {
                System.out.println(response);
                
            }
            
            @Override
            public void failed(Throwable throwable) {
                System.out.println(throwable);                
            }
        };
        
        
        client.target("http://localhost:9080/service/rest/AddressScore")
              .queryParam("firstName", "Michael")
              .queryParam("lastName", "Smith")
              .queryParam("address", "1736 Richmond Ave 2 Wappingers FL, NY 12990-9103")
              .request()
              .async()
              .get(callback);
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignore) {} 
    }
    
    
    
   
    @Test
    public void testSyncApache() throws Exception {
        
        ClientConfig clientConfig = new ClientConfig();
        ApacheConnectorProvider provider = new ApacheConnectorProvider();
        clientConfig.connectorProvider(provider);
        Client client = ClientBuilder.newClient(clientConfig);
        
        String score = client.target("http://localhost:9080/service/rest/AddressScore")
              .queryParam("firstName", "Michael")
              .queryParam("lastName", "Smith")
              .queryParam("address", "1736 Richmond Ave 2 Wappingers FL, NY 12990-9103")
              .request()
              .get(String.class);
        
        System.out.println(score);
    }
    
}
