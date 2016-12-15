package com.hystrixdemo.cliente.hystrix;

import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.monitor.AbstractMonitor;
import com.netflix.servo.monitor.Counter;
import com.netflix.servo.monitor.Gauge;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.tag.BasicTagList;
import com.netflix.servo.tag.Tag;
import com.netflix.servo.tag.TagList;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract PublisherConfig Flux.
 */
public abstract class HystrixFluxMetricsPublisherAbstract {

    /**
     * Listado de tags que se utilizarán para marcar las métricas recolectadas.
     */
    protected List<Tag> tagList;

    public HystrixFluxMetricsPublisherAbstract() {
        tagList = new ArrayList<Tag>();
    }

    public List<Tag> getTagList() {
        return tagList;
    }

    public void setTagList(List<Tag> tagList) {
        this.tagList = tagList;
    }

    /**
     * Métrica con datos informativos que se marca con la tagList.
     * @param <K> tipo de información que almacena la métrica.
     */
    protected abstract class InformationalMetric<K> extends AbstractMonitor<K> {
        public InformationalMetric(MonitorConfig config) {
            super(config.withAdditionalTag(DataSourceType.INFORMATIONAL).withAdditionalTags(new BasicTagList(getTagList())));
        }

        @Override
        public K getValue(int n) {
            return getValue();
        }

        @Override
        public abstract K getValue();
    }

    /**
     * Counter Métrica que se marca con la tagList.
     */
    protected abstract class CounterMetric extends AbstractMonitor<Number> implements Counter {

        public CounterMetric(MonitorConfig config) {
            super(config.withAdditionalTag(DataSourceType.COUNTER).withAdditionalTags(new BasicTagList(getTagList())));
        }

        @Override
        public Number getValue(int n) {
            return getValue();
        }

        @Override
        public abstract Number getValue();

        @Override
        public void increment() {
            throw new IllegalStateException("We are wrapping a value instead.");
        }

        @Override
        public void increment(long arg0) {
            throw new IllegalStateException("We are wrapping a value instead.");
        }
    }

    /**
     * Gauge Métrica que se marca con la tagList.
     */
    protected abstract class GaugeMetric extends AbstractMonitor<Number> implements Gauge<Number> {
        public GaugeMetric(MonitorConfig config) {
            super(config.withAdditionalTag(DataSourceType.GAUGE).withAdditionalTags(new BasicTagList(getTagList())));
        }

        @Override
        public Number getValue(int n) {
            return getValue();
        }

        @Override
        public abstract Number getValue();
    }
}
