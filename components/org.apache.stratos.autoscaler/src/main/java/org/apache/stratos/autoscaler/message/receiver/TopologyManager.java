package org.apache.stratos.autoscaler.message.receiver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.autoscaler.message.receiver.health.HealthEventMessageReceiver;
import org.apache.stratos.autoscaler.message.receiver.topology.TopologyEventMessageReceiver;
import org.apache.stratos.messaging.broker.subscribe.TopicSubscriber;
import org.apache.stratos.messaging.domain.topology.Topology;
import org.apache.stratos.messaging.util.Constants;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class will initiate all the subscriber to topics
 */
public class TopologyManager {
    private static final Log log = LogFactory.getLog(TopologyManager.class);
    private static volatile Topology topology;
    private static volatile ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static volatile ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private static volatile ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    public static void acquireReadLock() {
        readLock.lock();
    }

    public static void releaseReadLock() {
        readLock.unlock();
    }

    public static void acquireWriteLock() {
        writeLock.lock();
    }

    public static void releaseWriteLock() {
        writeLock.unlock();
    }

    public static synchronized Topology getTopology() {
        if (topology == null) {
            synchronized (TopologyManager.class){
                if (topology == null) {
                    topology = new Topology();
                }
            }
        }
        return topology;
    }

    public void subscribeAllTopics(){

        TopicSubscriber topologyTopicSubscriber = new TopicSubscriber(Constants.TOPOLOGY_TOPIC);
        topologyTopicSubscriber.setMessageListener(new TopologyEventMessageReceiver());
        Thread topologyTopicSubscriberThread = new Thread(topologyTopicSubscriber);
        topologyTopicSubscriberThread.start();

        if (log.isDebugEnabled()) {
           log.debug("Topology event message receiver thread started");
        }

        TopicSubscriber healthStatTopicSubscriber = new TopicSubscriber(Constants.HEALTH_STAT_TOPIC);
        healthStatTopicSubscriber.setMessageListener(new HealthEventMessageReceiver());
        Thread healthStatTopicSubscriberThread = new Thread(healthStatTopicSubscriber);
        healthStatTopicSubscriberThread.start();

        if (log.isDebugEnabled()) {
           log.debug("Health Stat event message receiver thread started");
        }

    }
}
