package com.hystrixdemo.cliente.hystrix;

import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.monitor.AbstractMonitor;
import com.netflix.servo.monitor.Counter;
import com.netflix.servo.monitor.Gauge;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.tag.Tag;

/**
 * Abstract Publisher Flux.
 */
public abstract class HystrixFluxMetricsPublisherAbstract {

    protected abstract Tag getFluxTypeTag();

    protected abstract Tag getFluxInstanceTag();

    protected abstract class InformationalMetric<K> extends AbstractMonitor<K> {
        public InformationalMetric(MonitorConfig config) {
            super(config.withAdditionalTag(DataSourceType.INFORMATIONAL).withAdditionalTag(getFluxTypeTag()).withAdditionalTag(getFluxInstanceTag()));
        }

        @Override
        public K getValue(int n) {
            return getValue();
        }

        @Override
        public abstract K getValue();
    }

    protected abstract class CounterMetric extends AbstractMonitor<Number> implements Counter {

        public CounterMetric(MonitorConfig config) {
            super(config.withAdditionalTag(DataSourceType.COUNTER).withAdditionalTag(getFluxTypeTag()).withAdditionalTag(getFluxInstanceTag()));
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

    protected abstract class GaugeMetric extends AbstractMonitor<Number> implements Gauge<Number> {
        public GaugeMetric(MonitorConfig config) {
            super(config.withAdditionalTag(DataSourceType.GAUGE).withAdditionalTag(getFluxTypeTag()).withAdditionalTag(getFluxInstanceTag()));
        }

        @Override
        public Number getValue(int n) {
            return getValue();
        }

        @Override
        public abstract Number getValue();
    }
}
