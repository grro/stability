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
package eu.redzoo.article.javaworld.stability.utils.jaxrs.container.tomcat;



import java.lang.management.ManagementFactory;
import java.util.Set;
import java.util.logging.Logger;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import eu.redzoo.article.javaworld.stability.utils.jaxrs.container.Environment;





public class TomcatEnvironment implements Environment {
    
    private static final Logger LOG = Logger.getLogger(TomcatEnvironment.class.getName());
    
    
    @Override
    public Threadpool getThreadpoolUsage() {
        
        Threadpool pool = null;
        
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        if (mbeanServer != null) {
            try {
                Set<ObjectInstance> threadPoolMBeans = mbeanServer.queryMBeans(new ObjectName("*:type=*,*"), null);

                threadPoolMBeans = mbeanServer.queryMBeans(new ObjectName("*:type=ThreadPool,*"), null);

                for (ObjectInstance threadPoolMBean : threadPoolMBeans) {
                    ObjectName objectName = threadPoolMBean.getObjectName();
                    Integer maxThreads = (Integer) mbeanServer.getAttribute(objectName, "maxThreads");
                    Integer currentThreadCount = (Integer) mbeanServer.getAttribute(objectName, "currentThreadCount");
                    Integer currentThreadsBusy = (Integer) mbeanServer.getAttribute(objectName, "currentThreadsBusy");

                    String name = (objectName.getKeyProperty("name") == null) ? "unknown" : objectName.getKeyProperty("name");

                    if ((pool == null) || (maxThreads > pool.getMaxThreads())) {
                        pool = new Threadpool(name, maxThreads, currentThreadCount, currentThreadsBusy);
                    }
                }

            } catch (JMException | RuntimeException e) {
                LOG.fine("could not get tomcat thread info" + e);
            }
        }

        return pool;
    }
}