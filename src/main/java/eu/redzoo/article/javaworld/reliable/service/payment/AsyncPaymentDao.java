package eu.redzoo.article.javaworld.reliable.service.payment;


import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.ImmutableList;





interface AsyncPaymentDao {

    CompletableFuture<ImmutableList<Payment>> getPaymentsAsync(String address, int max);
    
    CompletableFuture<Optional<Payment>> getPaymentAsync(String id);
}
