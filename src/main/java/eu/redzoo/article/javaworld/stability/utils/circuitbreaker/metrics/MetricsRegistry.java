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
package eu.redzoo.article.javaworld.stability.utils.circuitbreaker.metrics;

import java.util.Map;

import jersey.repackaged.com.google.common.collect.Maps;



public class MetricsRegistry {
    
    private final Map<String, TransactionMetrics> metricsMap = Maps.newConcurrentMap();
    
    private final int maxEntries = 100;
    private final int bufferSize = 1000;

    public TransactionMetrics transactions(String scope) {  
        if (scope == null) {
            scope = "__<NULL>__";
        }
        
        TransactionMetrics metrics = metricsMap.get(scope);
        if ((metrics == null) && (metricsMap.size() < maxEntries)) {
            metrics = new TransactionMetrics(bufferSize);
            metricsMap.put(scope, metrics);
        }
        
        return metrics;
    }
}

