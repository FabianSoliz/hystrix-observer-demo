package com.hystrixdemo.cliente.hystrix;

import com.netflix.servo.Metric;
import com.netflix.servo.publish.BaseMetricObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * MetricObserver que guarda las metricas en dataDog.
 */
public class DatadogMetricObserver extends BaseMetricObserver {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatadogMetricObserver.class);

    public DatadogMetricObserver(String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateImpl(List<Metric> metrics) {
        LOGGER.debug("sending data:"  + getName());
        System.out.println("****DatadogMetricObserver***");
        System.out.println("Metrics size:" + metrics.size());
        for (Metric metric : metrics) {

            if (metric.getConfig().getTags().getTag("commandKey") != null){
               String tag = metric.getConfig().getTags().getTag("commandKey").getValue();
                System.out.println("#####"+ tag +"######");
            }
            if (metric.getConfig().getTags().getTag("commandGroupKey") != null){
                String tag = metric.getConfig().getTags().getTag("commandGroupKey").getValue();
                System.out.println("#####"+ tag +"######");
            }
            if (metric.getConfig().getTags().getTag("type") != null){
                String tag = metric.getConfig().getTags().getTag("type").getValue();
                System.out.println("#####"+ tag +"######");
            }
            System.out.println(metric.toString());
        }

    }
}
