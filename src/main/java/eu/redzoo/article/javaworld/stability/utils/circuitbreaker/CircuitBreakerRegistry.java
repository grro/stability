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