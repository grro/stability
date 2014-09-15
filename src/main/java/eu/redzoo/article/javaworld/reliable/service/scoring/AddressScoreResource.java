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
    public void getScoreAsync(@QueryParam("addr") String address, @Suspended AsyncResponse asyncResponse) {
        
        // simulate I/O
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (address.contains("5736 Richmond Ave ")) {
                    asyncResponse.resume(Score.POSITIVE);
                } if (address.contains("2434 Baltin Ave")) {
                    asyncResponse.resume(Score.NEGATIVE);
                } else {
                    asyncResponse.resume(Score.NEUTRAL);
                }
            }
        }).start();
    }
}
