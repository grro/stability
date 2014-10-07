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
package eu.redzoo.article.javaworld.stability.service.payment;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.ImmutableList;






class PaymentDaoImpl implements PaymentDao, AsyncPaymentDao {

    @Override
    public ImmutableList<Payment> getPayments(String address, int max) {
        
        if (address.startsWith("Tom")) {
            return ImmutableList.of(new Payment(UUID.randomUUID().toString(), false), 
                                    new Payment(UUID.randomUUID().toString(), false));
        } else if (address.startsWith("John")) {
            return ImmutableList.of(new Payment(UUID.randomUUID().toString(), true), 
                                    new Payment(UUID.randomUUID().toString(), false));

        } else {
            return ImmutableList.of();
        }
    }
    
    
    @Override
    public Optional<Payment> getPayment(String id) {
        if (id.startsWith("1")) {
            return Optional.of(new Payment(id, false));
        } else {
            return Optional.empty();
        }
    }

    
    @Override
    public CompletableFuture<ImmutableList<Payment>> getPaymentsAsync(String address, int max) {
        return CompletableFuture.completedFuture(getPayments(address, max));
    }
    
    
    @Override
    public CompletableFuture<Optional<Payment>> getPaymentAsync(String id) {
        return CompletableFuture.supplyAsync(() -> getPayment(id));
    }
}
