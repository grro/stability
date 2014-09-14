package eu.redzoo.article.javaworld.reliable.payment;


import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;




@Singleton
@Path("Payment")
public class PaymentResource {

    private final PaymentDao paymentDao;
    
    
    public PaymentResource() {
        this.paymentDao = new PaymentDaoImpl();
    }
    
    

    @Path("{id}")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Payment getPayment(@PathParam("id") String id) {
        return paymentDao.getPayment(id).orElseThrow(NotFoundException::new);
    }
}
