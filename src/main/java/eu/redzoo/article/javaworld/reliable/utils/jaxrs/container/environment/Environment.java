package eu.redzoo.article.javaworld.reliable.utils.jaxrs.container.environment;

import com.google.common.collect.ImmutableSet;







public interface Environment  {

    
    ImmutableSet<Threadpool> getThreadpoolUsage();       

    
    
    public static class Threadpool {
        private final String name;
        private final int maxThreads;
        private final int currentThreadCount;
        private final int currentThreadsBusy;
        
        public Threadpool(String name, int maxThreads, int currentThreadCount, int currentThreadsBusy) {
            this.name = name;
            this.maxThreads = maxThreads;
            this.currentThreadCount = currentThreadCount;
            this.currentThreadsBusy = currentThreadsBusy;
        }
        
        public String getName() {
            return name;
        }
        
        public int getMaxThreads() {
            return maxThreads;
        }
        
        public int getCurrentThreadCount() {
            return currentThreadCount;
        }
        
        public int getCurrentThreadsBusy() {
            return currentThreadsBusy;
        }
        
        
        
    }
    
}