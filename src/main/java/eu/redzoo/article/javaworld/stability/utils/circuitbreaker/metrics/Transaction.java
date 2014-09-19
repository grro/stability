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

import java.time.Duration;
import java.time.Instant;



public class Transaction {
    private final Instant startTime = Instant.now();

    private volatile Instant endTime = null;
    private volatile boolean isFailed = false;

    
    public Instant getStarttime() {
        return startTime;
    }
    
    public boolean isFailed() {
        return isFailed;
    }
    
    public boolean isRunning() {
        return (endTime == null);
    }
    
    public Duration getConsumedMillis() {
        if (endTime == null) {
            return Duration.between(startTime, Instant.now());
        } else {
            return Duration.between(startTime, endTime);
        }
    }
    
    public void close(boolean isFailed) {
        endTime = Instant.now();
        this.isFailed = isFailed;
    }
    
    
    public static void close(Object transaction, boolean isFailed) {
        if ((transaction != null) && (transaction instanceof Transaction)) {
            ((Transaction) transaction).close(isFailed);
        }
    }
}