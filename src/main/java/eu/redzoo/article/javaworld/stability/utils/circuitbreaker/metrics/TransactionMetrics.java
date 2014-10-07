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
package eu.redzoo.article.javaworld.stability.utils.circuitbreaker.metrics;

import java.time.Duration;
import java.time.Instant;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

import eu.redzoo.article.javaworld.stability.utils.collection.Immutables;



public class TransactionMetrics implements Transactions {
  
    private final Ringbuffer ringbuffer;
    
    
    public TransactionMetrics(int bufferSize) {
        ringbuffer = new Ringbuffer(bufferSize);
    }
    
    
    public Transaction openTransaction() {
        Transaction transaction = new Transaction();
        ringbuffer.addEntry(transaction);
        
        return transaction;
    }
  
    
    @Override
    public Transactions failed() {
        return all().failed();
    }
    
    @Override
    public Transactions ofLast(Duration duration) {
        return all().ofLast(duration);
    }
    
    @Override
    public Duration percentile(int percent) {
        return all().percentile(percent);
    }
    
    @Override
    public Transactions running() {
        return all().running();
    }
    
    @Override
    public Transactions since(Instant fromTime) {
        return all().since(fromTime);
    }
    
    @Override
    public int size() {
        return all().size();
    }
    
    private Transactions all() {
        return new TransactionsImpl(ringbuffer.getTransactions());
    }
    
        
    
    private static final class Ringbuffer {
        private final Transaction[] transactions;
        private volatile int currentPos;
        
        public Ringbuffer(int numSlots) {
            this.transactions = new Transaction[numSlots];
            this.currentPos = 0;
        }


        public void addEntry(Transaction transaction) {
            transactions[incPos()] = transaction;
        }

        
        private int incPos() {
            int newPos = currentPos + 1;
            currentPos = (newPos < transactions.length) ? newPos : 0;
            return currentPos;
        }
        
           
        public ImmutableSet<Transaction> getTransactions() {
            Builder<Transaction> resultBuilder = new ImmutableSet.Builder<>();
            
            for (Transaction transaction : transactions) {
                if (transaction != null) {
                    resultBuilder.add(transaction);
                }
            }
            
            return resultBuilder.build();
        }
    }
    
    private class TransactionsImpl implements Transactions {
        private final ImmutableSet<Transaction> transactions;
        
        public TransactionsImpl(ImmutableSet<Transaction> transactions) {
            this.transactions = transactions;
        }
        
        public int size() {
            return all().size();
        }
        
        private ImmutableSet<Transaction> all() {
            return transactions;
        }

        
        public Transactions ofLast(Duration duration) {
            return since(Instant.now().minus(duration));
        }
                        
        public Transactions since(Instant fromTime) {
            return new TransactionsImpl(all().stream()
                                             .parallel()
                                             .filter(transaction -> transaction.getStarttime().isAfter(fromTime))
                                             .collect(Immutables.toSet()));
        }
        
        public Transactions failed() {
            return new TransactionsImpl(all().stream()
                                             .parallel()
                                             .filter(transaction -> transaction.isFailed())
                                              .collect(Immutables.toSet()));
        }    
        
        public Transactions running() {
            return new TransactionsImpl(all().stream()
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
}

