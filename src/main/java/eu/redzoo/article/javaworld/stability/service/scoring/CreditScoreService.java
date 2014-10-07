/*
 * Copyright (c) 2014 Gregor Roth
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
public class CreditScoreService {

    
    @Path("creditscores")
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
