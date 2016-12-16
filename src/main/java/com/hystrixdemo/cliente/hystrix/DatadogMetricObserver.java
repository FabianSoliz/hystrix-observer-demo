package com.hystrixdemo.cliente.hystrix;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.servo.Metric;
import com.netflix.servo.publish.BaseMetricObserver;
import javadog.MetricCollector;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * MetricObserver que guarda las métricas en dataDog.
 */
public class DatadogMetricObserver extends BaseMetricObserver {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatadogMetricObserver.class);
    private static final String PREFIX = "application.flux.";

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

                //TODO descomentar para validar si la métrica se debe almacenar en dataDog.
//                if (isSaved(metric)) {

                    String metricName = PREFIX + metric.getConfig().getName();

                    if (metric.hasNumberValue()) {

                        try {
                            MetricCollector.recordSimpleMetric(metricName,
                                    metric.getNumberValue().longValue(), getKeys(metric));
                        } catch (Exception e) {
                            LOGGER.error("No se pudo guardar la métrica. Métrica:" + metric.getConfig().getName(), e);
                        }

                        LOGGER.debug("Logged Datadog Metric name:" + metricName + "=" + metric.getNumberValue().toString());
                    }

//                }
            }


    }

    /**
     *
     * @param metric a evaluar.
     * @return true, si la métrica se debe guardar. Falso en caso contrario.
     */
    public boolean isSaved(Metric metric) {

        String property = "";

        if (metric.getConfig().getTags().getTag("commandGroupKey")!= null){
            property = metric.getConfig().getTags().getTag("commandGroupKey").getValue() +".";
        }
        if (metric.getConfig().getTags().getTag("commandKey")!= null){
            property = property + metric.getConfig().getTags().getTag("commandKey").getValue() +".";
        }

        property = property + metric.getConfig().getName();

        return DynamicPropertyFactory.getInstance().getBooleanProperty(property, false).getValue();
    }


    /**
     *
     * @param metric con las keys
     * @return array con las key de la métrica.
     */
    private String[] getKeys(Metric metric) {

        List<String> keys = new ArrayList<String>();

        if (metric.getConfig().getTags().getTag("commandGroupKey")!= null){
            keys.add("commandGroupKey:" +  metric.getConfig().getTags().getTag("commandGroupKey").getValue());
        }
        if (metric.getConfig().getTags().getTag("commandKey")!= null){
            keys.add("commandKey:" + metric.getConfig().getTags().getTag("commandKey").getValue());
        }

        keys.add("metricName:" + metric.getConfig().getName());

        return keys.toArray(new String[keys.size()]);
    }
}
