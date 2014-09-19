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
package eu.redzoo.article.javaworld.stability.utils.circuitbreaker;


import java.time.Duration;
import java.util.Map;

import jersey.repackaged.com.google.common.collect.Maps;



public class CircuitBreakerRegistry {

    private final Map<String, CircuitBreaker> circuitBreakerMap = Maps.newConcurrentMap();
    
    private final int maxEntries = 100;

    private final HealthPolicy healthPolicy;

    
    public CircuitBreakerRegistry(HealthPolicy healthPolicy) {
        this.healthPolicy = healthPolicy;
    }
    
    
    public CircuitBreaker get(String scope) {        
        if (scope == null) {
            scope = "__<NULL>__";
        }
        
        CircuitBreaker circuitBreaker = circuitBreakerMap.get(scope);
        if ((circuitBreaker == null) && (circuitBreakerMap.size() < maxEntries)) {
            circuitBreaker = new CircuitBreaker(scope, healthPolicy, Duration.ofSeconds(3));
            circuitBreakerMap.put(scope, circuitBreaker);
        }
        
        return circuitBreaker;
    }
}