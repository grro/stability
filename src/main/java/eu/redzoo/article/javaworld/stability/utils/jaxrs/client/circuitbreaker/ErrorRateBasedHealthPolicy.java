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
package eu.redzoo.article.javaworld.stability.utils.jaxrs.client.circuitbreaker;

import java.time.Duration;

import eu.redzoo.article.javaworld.stability.utils.circuitbreaker.HealthPolicy;
import eu.redzoo.article.javaworld.stability.utils.circuitbreaker.metrics.MetricsRegistry;
import eu.redzoo.article.javaworld.stability.utils.circuitbreaker.metrics.Transactions;


public class ErrorRateBasedHealthPolicy implements HealthPolicy  {
  
    private final MetricsRegistry metricsRegistry;
    private final int thresholdMinReqPerMin;
    
    // ...
  
    public ErrorRateBasedHealthPolicy(MetricsRegistry metricsRegistry) {
        this(metricsRegistry, 30);
    }

    
    public ErrorRateBasedHealthPolicy(MetricsRegistry metricsRegistry, int thresholdMinRatePerMin) {
        this.metricsRegistry = metricsRegistry;
        this.thresholdMinReqPerMin = thresholdMinRatePerMin;
    }
  
    
    @Override
    public boolean isHealthy(String scope) {
        Transactions transactions =  metricsRegistry.transactions(scope).ofLast(Duration.ofMinutes(1));

        return ! ((transactions.size() > thresholdMinReqPerMin) &&        // check threshold reached?
                  (transactions.failed().size() == transactions.size()) && // every call failed?
                  (true)                                    );             // client connection pool limit reached?
    }
}

