package eu.redzoo.article.javaworld.reliable;






import java.io.File;
import java.util.Set;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;

import org.apache.catalina.startup.Tomcat;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.redzoo.article.javaworld.reliable.payment.Payment;
import eu.redzoo.article.javaworld.reliable.payment.PaymentMethod;
import static eu.redzoo.article.javaworld.reliable.payment.PaymentMethod.*;


public class RestServicesTest {

    private static Client client;
    private static Tomcat server;
  
    
    @BeforeClass
    public static void setUp() throws Exception {
        
        
        /*
        server = new Server(9080);
 
        WebAppContext webapp = new WebAppContext(new File("src/main/resources/webapp").getAbsolutePath(), "/service");
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setWelcomeFiles(new String[]{ "index.html" });
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resource_handler, webapp});
        server.setHandler(handlers);

        server.start();
        */
        
        
        server = new Tomcat();
        server.setPort(9080);
        server.addWebapp("/service", new File("src/main/resources/webapp").getAbsolutePath());

        server.start();
        

        
        
        
        client = ClientBuilder.newClient();
    }

    

    @AfterClass
    public static void tearDown() throws Exception {
        server.stop();
        client.close();
    }

    

    @Test
    public void testRetrievePayment() throws Exception {
        Payment payment = client.target("http://localhost:9080/service/rest/Payment/123443")
                                .request()
                                .get(Payment.class);
        
        System.out.println(payment);
    }
    
    
    @Test
    public void testRetrievePaymentNotFound() throws Exception {
        
        try {
            client.target("http://localhost:9080/service/rest/Payment/823443")
                  .request()
                  .get(Payment.class);

            Assert.fail("NotFoundException expected");
        } catch (NotFoundException expected) { }
    }
    
    

    @Test
    public void testRetrievePaymentAsync() throws Exception {
        Payment payment = client.target("http://localhost:9080/service/rest/Async/Payment/123443")
                                .request()
                                .get(Payment.class);
        
        System.out.println(payment);
    }
    
    
    @Test
    public void testRetrievePaymentNotFoundAsync() throws Exception {
        
        try {
            client.target("http://localhost:9080/service/rest/Async/Payment/823443")
                  .request()
                  .get(Payment.class);

            Assert.fail("NotFoundException expected");
        } catch (NotFoundException expected) { }
    }

    


    @Test
    public void testRetrievePaymentMethodNewUserBadAddress() throws Exception {
        Set<PaymentMethod> paymentMethods = client.target("http://localhost:9080/service/rest/PaymentMethod")
                                                  .queryParam("name", "Michael Smith")
                                                  .queryParam("dateOfBirth", "05.11.1995")
                                                  .queryParam("address", "1736 Richmond Ave 2 Wappingers FL, NY 12990-9103")
                                                  .request()
                                                  .get(new GenericType<Set<PaymentMethod>>() { });    
         
        Assert.assertEquals(1, paymentMethods.size());
        Assert.assertTrue(paymentMethods.contains(PREPAYMENT));
    }
    
    
    @Test
    public void testRetrievePaymentMethodNewUserGoodAddress() throws Exception {
        Set<PaymentMethod> paymentMethods = client.target("http://localhost:9080/service/rest/PaymentMethod")
                                                  .queryParam("name", "Michael Smith")
                                                  .queryParam("dateOfBirth", "05.11.1995")
                                                  .queryParam("address", "3736 Richmond Ave 2 Wappingers FL, NY 12990-9103")
                                                  .request()
                                                  .get(new GenericType<Set<PaymentMethod>>() { });    
        
        Assert.assertEquals(4, paymentMethods.size());
        Assert.assertTrue(paymentMethods.contains(PREPAYMENT));
        Assert.assertTrue(paymentMethods.contains(CREDITCARD));
        Assert.assertTrue(paymentMethods.contains(PAYPAL));
        Assert.assertTrue(paymentMethods.contains(INVOCE));
    }
    
    
    @Test
    public void testRetrievePaymentMethodNewUserGoodAddressBulk() throws Exception {
        for (int i = 0; i < 100; i++) {
            testRetrievePaymentMethodNewUserGoodAddress();
        }
    }
    
    
    
    
    @Test
    public void testRetrievePaymentMethodNewUserUnknownAddress() throws Exception {
        Set<PaymentMethod> paymentMethods = client.target("http://localhost:9080/service/rest/PaymentMethod")
                                                  .queryParam("name", "Michael Smith")
                                                  .queryParam("dateOfBirth", "05.11.1995")
                                                  .queryParam("address", "5736 Richmond Ave 2 Wappingers FL, NY 12990-9103")
                                                  .request()
                                                  .get(new GenericType<Set<PaymentMethod>>() { });    
        
        Assert.assertEquals(3, paymentMethods.size());
        Assert.assertTrue(paymentMethods.contains(PREPAYMENT));
        Assert.assertTrue(paymentMethods.contains(CREDITCARD));
        Assert.assertTrue(paymentMethods.contains(PAYPAL));
        
        client.target("http://localhost:9080/service/rest/PaymentMethod")
                .queryParam("name", "Michael Smith")
                .queryParam("dateOfBirth", "05.11.1995")
                .queryParam("address", "5736 Richmond Ave 2 Wappingers FL, NY 12990-9103")
                .request()
                .get(new GenericType<Set<PaymentMethod>>() { });    

    }
    
    
    
/*
    @Test
    public void testRetrievePaymentMethodNewUserBadAddressAsync() throws Exception {
        Set<PaymentMethod> paymentMethods = client.target("http://localhost:9080/service/rest/Async/PaymentMethod")
                                                  .queryParam("firstName", "Michael")
                                                  .queryParam("lastName", "Smith")
                                                  .queryParam("dateOfBirth", "05.11.1995")
                                                  .queryParam("address", "1736 Richmond Ave 2 Wappingers FL, NY 12990-9103")
                                                  .request()
                                                  .get(new GenericType<Set<PaymentMethod>>() { });    
         
        Assert.assertEquals(1, paymentMethods.size());
        Assert.assertTrue(paymentMethods.contains(PREPAYMENT));
    }
    
    
    @Test
    public void testRetrievePaymentMethodNewUserGoodAddressAsync() throws Exception {
        Set<PaymentMethod> paymentMethods = client.target("http://localhost:9080/service/rest/Async/PaymentMethod")
                                                  .queryParam("firstName", "Michael")
                                                  .queryParam("lastName", "Smith")
                                                  .queryParam("dateOfBirth", "05.11.1995")
                                                  .queryParam("address", "3736 Richmond Ave 2 Wappingers FL, NY 12990-9103")
                                                  .request()
                                                  .get(new GenericType<Set<PaymentMethod>>() { });    
        
        Assert.assertEquals(4, paymentMethods.size());
        Assert.assertTrue(paymentMethods.contains(PREPAYMENT));
        Assert.assertTrue(paymentMethods.contains(CREDITCARD));
        Assert.assertTrue(paymentMethods.contains(PAYPAL));
        Assert.assertTrue(paymentMethods.contains(INVOCE));
    }
    
    
    @Test
    public void testRetrievePaymentMethodNewUserUnknownAddressAsync() throws Exception {
        Set<PaymentMethod> paymentMethods = client.target("http://localhost:9080/service/rest/Async/PaymentMethod")
                                                  .queryParam("firstName", "Michael")
                                                  .queryParam("lastName", "Smith")
                                                  .queryParam("dateOfBirth", "05.11.1995")
                                                  .queryParam("address", "5736 Richmond Ave 2 Wappingers FL, NY 12990-9103")
                                                  .request()
                                                  .get(new GenericType<Set<PaymentMethod>>() { });    
        
        Assert.assertEquals(3, paymentMethods.size());
        Assert.assertTrue(paymentMethods.contains(PREPAYMENT));
        Assert.assertTrue(paymentMethods.contains(CREDITCARD));
        Assert.assertTrue(paymentMethods.contains(PAYPAL));
    }
*/
}
