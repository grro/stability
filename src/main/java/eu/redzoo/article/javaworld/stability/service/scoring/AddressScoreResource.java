/*
 * Copyright (c) 2014, Gregor Roth, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 */
package eu.redzoo.article.javaworld.stability.service.scoring;

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
    @Produces(MediaType.APPLICATION_JSON)
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
