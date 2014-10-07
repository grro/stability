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