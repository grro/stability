package eu.redzoo.article.javaworld.reliable.utils.circuitbreaker.metrics;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;



public class TransactionMetrics  {
  
    private final Ringbuffer ringbuffer;
    
    
    public TransactionMetrics(int bufferSize) {
        ringbuffer = new Ringbuffer(bufferSize);
    }
    
    
    public Transaction newTransaction() {
        Transaction transaction = new Transaction();
        ringbuffer.addEntry(transaction);
        
        return transaction;
    }
  
    
    public Transactions recorded() {
        return new Transactions(ringbuffer.getTransactions());
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
}

