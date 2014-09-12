package eu.redzoo.article.javaworld.reliable.payment;


import java.util.Optional;

import com.google.common.collect.ImmutableList;





interface PaymentDao {

    ImmutableList<Payment> getPayments(String name, String dateOfBirth, String address, int max);
    
    Optional<Payment> getPayment(String id);
}
