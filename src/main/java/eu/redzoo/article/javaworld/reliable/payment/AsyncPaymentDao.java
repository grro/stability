package eu.redzoo.article.javaworld.reliable.payment;


import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.ImmutableList;





interface AsyncPaymentDao {

    CompletableFuture<ImmutableList<Payment>> getPaymentsAsync(String name, String dateOfBirth, String address, int max);
    
    CompletableFuture<Optional<Payment>> getPaymentAsync(String id);
}
