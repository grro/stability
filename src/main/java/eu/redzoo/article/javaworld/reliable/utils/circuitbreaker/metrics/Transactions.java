package eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.metrics;

import java.time.Duration;
import java.time.Instant;

import com.google.common.collect.ImmutableSet;

import eu.redzoo.article.javaworld.reliable.utils.collection.Immutables;



public class Transactions  {
    private final ImmutableSet<Transaction> transactions;
    
    public Transactions(ImmutableSet<Transaction> transactions) {
        this.transactions = transactions;
    }
    
    public int size() {
        return all().size();
    }
    
    public ImmutableSet<Transaction> all() {
        return transactions;
    }

    
    public Transactions ofLast(Duration duration) {
        return since(Instant.now().minus(duration));
    }
                    
    public Transactions since(Instant fromTime) {
        return new Transactions(all().stream()
                                             .parallel()
                                             .filter(transaction -> transaction.getStarttime().isAfter(fromTime))
                                             .collect(Immutables.toSet()));
    }
    
    public Transactions failed() {
        return new Transactions(all().stream()
                                             .parallel()
                                             .filter(transaction -> transaction.isFailed())
                                             .collect(Immutables.toSet()));
    }    
    
    public Transactions running() {
        return new Transactions(all().stream()
                                             .parallel()
                                             .filter(transaction -> transaction.isRunning())
                                             .collect(Immutables.toSet()));
    }    
    public Duration percentile(int percent) {
        if (transactions.isEmpty()) {
            return Duration.ZERO;
        } else {
            int[] sortedValues = all().stream()
                                      .mapToInt(transaction -> (int) transaction.getConsumedMillis().toMillis())
                                      .sorted()
                                      .toArray();    

            if (percent == 0) {
                return Duration.ofMillis(sortedValues[0]);
            } else {
                return Duration.ofMillis(sortedValues[(percent * sortedValues.length) / 100]);
            }
        }
    }
    
    public String toString() {
        return transactions.toString();
    }
}
  