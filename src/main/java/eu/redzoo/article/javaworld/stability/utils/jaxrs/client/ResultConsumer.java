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
package eu.redzoo.article.javaworld.stability.utils.jaxrs.client;



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