package eu.redzoo.article.javaworld.reliable.service.scoring;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;





@Path("/")
public class AddressScoreResource {

    @Path("AddressScore")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Score getScore(@QueryParam("firstName") String firstName,
                          @QueryParam("lastName") String lastName,
                          @QueryParam("dateOfBirth") String dateOfBirth,
                          @QueryParam("address") String address) {
        
        if (address.startsWith("3736")) {
            return Score.POSITIVE;
        } if (address.startsWith("1736")) {
            return Score.NEGATIVE;
        } else {
            return Score.NEUTRAL;
        }
    }
    
    
    @Path("AddressScore2")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public void getScoreAsync(@QueryParam("firstName") String firstName,
                              @QueryParam("lastName") String lastName,
                              @QueryParam("dateOfBirth") String dateOfBirth,
                              @QueryParam("address") String address,
                              @Suspended AsyncResponse asyncResponse) {
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                asyncResponse.resume(getScore(firstName, lastName, dateOfBirth, address));
            }
        }).start();
    }
}
