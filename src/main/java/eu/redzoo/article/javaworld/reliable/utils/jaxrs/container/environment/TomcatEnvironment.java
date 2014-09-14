package eu.redzoo.article.javaworld.reliable.utils.jaxrs.container.environment;



import java.lang.management.ManagementFactory;
import java.util.Set;
import java.util.logging.Logger;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;




public class TomcatEnvironment implements Environment {
    
    private static final Logger LOG = Logger.getLogger(TomcatEnvironment.class.getName());
    
    
    @Override
    public ImmutableSet<Threadpool> getThreadpoolUsage() {
        Builder<Threadpool> builder = new ImmutableSet.Builder<>();
        
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
                    
                    builder.add(new Threadpool(name, maxThreads, currentThreadCount, currentThreadsBusy));
                }

            } catch (JMException | RuntimeException e) {
                LOG.fine("could not get tomcat thread info" + e);
            }
        }

        return builder.build();
    }
}