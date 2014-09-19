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
package eu.redzoo.article.javaworld.stability.utils.jaxrs.container.handshaking;





import eu.redzoo.article.javaworld.stability.utils.circuitbreaker.metrics.MetricsRegistry;



public class FlowController {

    private final MetricsRegistry metricsRegistry;

    
    public FlowController(MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;
    }
    
    
    public boolean isTooManyRequests(String scope) {
        return true;
    }
}
