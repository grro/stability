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
package eu.redzoo.article.javaworld.stability.service.payment;


import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

import eu.redzoo.article.javaworld.stability.utils.jaxrs.client.ResultConsumer;





@Singleton
@Path("payments")
public class AsyncPaymentResource {

    private final AsyncPaymentDao paymentDao;
    
    
    public AsyncPaymentResource() {
        this.paymentDao = new PaymentDaoImpl();
    }
    

    @Path("{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public void getPaymentAsync(@PathParam("id") String id, @Suspended AsyncResponse resp) {
        
        paymentDao.getPaymentAsync(id)
                  .thenApply(optionalPayment -> optionalPayment.<NotFoundException>orElseThrow(NotFoundException::new))
                  .whenComplete(ResultConsumer.write(resp));
    }
}
