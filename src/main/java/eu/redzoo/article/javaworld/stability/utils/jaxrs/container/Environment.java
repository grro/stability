/*
 * Copyright (c) 2014, Gregor Roth, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 */
package eu.redzoo.article.javaworld.stability.utils.jaxrs.container;







public interface Environment  {

    
    Threadpool getThreadpoolUsage();       

    
    
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