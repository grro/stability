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
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;




public class CircuitBreaker {
    // ..
    private final String scope;
    private final Random random = new Random(); 
    private final Duration openStateTimeout;
    private final HealthPolicy policy;

    private AtomicReference<CircuitBreakerState> state = new AtomicReference<>(); 


    
    CircuitBreaker(String scope, HealthPolicy healthPolicy, Duration openStateTimeout) {
        this.scope = scope;
        this.policy = new CachedCircuitBreakerPolicy(healthPolicy, Duration.ofSeconds(3));
        this.openStateTimeout = openStateTimeout;

        state.set(new ClosedState());
    }
    
    
    public boolean isRequestAllowed() {
        return state.get().isRequestAllowed();
    }
    

    
    private final class ClosedState implements CircuitBreakerState {
        
        @Override
        public boolean isRequestAllowed() {
            return (policy.isHealthy(scope)) ? true
                                             : changeState(new OpenState()).isRequestAllowed();
        }
    }
    
    
     
    private final class OpenState implements CircuitBreakerState {
        private final Instant exitDate = Instant.now().plus(openStateTimeout);  
        
        @Override
        public boolean isRequestAllowed() {
            return (Instant.now().isAfter(exitDate)) ? changeState(new HalfOpenState()).isRequestAllowed()
                                                     : false;
        }
    }

    
    
    private final class HalfOpenState implements CircuitBreakerState {
        private double chance = 0.02;  // 2% will be passed through

        @Override
        public boolean isRequestAllowed() {
            return (policy.isHealthy(scope)) ? changeState(new ClosedState()).isRequestAllowed()
                                             : (random.nextDouble() <= chance);
        }
    }

    
    
    
    private CircuitBreakerState changeState(CircuitBreakerState newState) {
        state.set(newState);
        return newState;
    }
    
    private static interface CircuitBreakerState {        
        boolean isRequestAllowed();       
    }
    
    

    
    private class CachedCircuitBreakerPolicy implements HealthPolicy  {
        
        private final HealthPolicy healthPolicy;
        private final Duration cacheTtl;
        
        private final Cache<String, CachedResult> cache = CacheBuilder.newBuilder().maximumSize(1000).build();
        
        
        public CachedCircuitBreakerPolicy(HealthPolicy healthPolicy, Duration cacheTtl) {
            this.healthPolicy = healthPolicy;
            this.cacheTtl = cacheTtl;
        }
        
        
        @Override
        public boolean isHealthy(String scope) {
            
            CachedResult cachedResult = cache.getIfPresent(scope);
            if ((cachedResult == null) || (cachedResult.isExpired())) {
                cachedResult = new CachedResult(healthPolicy.isHealthy(scope), cacheTtl);
                cache.put(scope, cachedResult);
            }

            return cachedResult.isHealthy();
        }  
        
        
        private class CachedResult {
            private final boolean isHealthy;
            private Instant validTo;
            
            
            CachedResult(boolean isHealthy, Duration ttl) {
                this.isHealthy = isHealthy;
                validTo = Instant.now().plus(ttl);
            }
            
            
            public boolean isExpired() {
                return Instant.now().isAfter(validTo);
            }
            
            public boolean isHealthy() {
                return isHealthy;
            }
        }
    }
    
    // ..
}

