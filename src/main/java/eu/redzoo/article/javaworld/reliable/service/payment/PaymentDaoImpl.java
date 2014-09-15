package eu.redzoo.article.javaworld.reliable.service.payment;

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
