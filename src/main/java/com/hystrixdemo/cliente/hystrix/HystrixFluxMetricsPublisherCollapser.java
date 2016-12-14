package com.hystrixdemo.cliente.hystrix;

import com.netflix.hystrix.HystrixCollapserKey;
import com.netflix.hystrix.HystrixCollapserMetrics;
import com.netflix.hystrix.HystrixCollapserProperties;
import com.netflix.hystrix.HystrixEventType;
import com.netflix.hystrix.metric.consumer.CumulativeCollapserEventCounterStream;
import com.netflix.hystrix.metric.consumer.RollingCollapserBatchSizeDistributionStream;
import com.netflix.hystrix.metric.consumer.RollingCollapserEventCounterStream;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherCollapser;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.annotations.DataSourceLevel;
import com.netflix.servo.monitor.BasicCompositeMonitor;
import com.netflix.servo.monitor.Monitor;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.tag.Tag;
import rx.functions.Func0;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación que recolecta las CollapserMetricas de hystrix y las almacena en un {@link com.netflix.servo.monitor.Monitor}.
 */
public class HystrixFluxMetricsPublisherCollapser extends HystrixFluxMetricsPublisherAbstract implements HystrixMetricsPublisherCollapser {

    private final HystrixCollapserKey key;
    private final HystrixCollapserMetrics metrics;
    private final HystrixCollapserProperties properties;
    private final Tag fluxInstanceTag;
    private final Tag fluxTypeTag;

    public HystrixFluxMetricsPublisherCollapser(HystrixCollapserKey threadPoolKey, HystrixCollapserMetrics metrics, HystrixCollapserProperties properties) {
        this.key = threadPoolKey;
        this.metrics = metrics;
        this.properties = properties;

        this.fluxInstanceTag = new Tag() {

            @Override
            public String getKey() {
                return "instance";
            }

            @Override
            public String getValue() {
                return key.name();
            }

            @Override
            public String tagString() {
                return key.name();
            }

        };
        this.fluxTypeTag = new Tag() {

            @Override
            public String getKey() {
                return "type";
            }

            @Override
            public String getValue() {
                return "HystrixCollapser";
            }

            @Override
            public String tagString() {
                return "HystrixCollapser";
            }

        };
    }

    @Override
    public void initialize() {
        /* list of monitors */
        List<Monitor<?>> monitors = getMonitors();

        // publish metrics together under a single composite (it seems this name is ignored)
        MonitorConfig commandMetricsConfig = MonitorConfig.builder("HystrixCollapser_" + key.name()).build();
        BasicCompositeMonitor commandMetricsMonitor = new BasicCompositeMonitor(commandMetricsConfig, monitors);

        DefaultMonitorRegistry.getInstance().register(commandMetricsMonitor);
        RollingCollapserBatchSizeDistributionStream.getInstance(key, properties).startCachingStreamValuesIfUnstarted();
        RollingCollapserEventCounterStream.getInstance(key, properties).startCachingStreamValuesIfUnstarted();
        CumulativeCollapserEventCounterStream.getInstance(key, properties).startCachingStreamValuesIfUnstarted();
    }

    @Override
    protected Tag getFluxTypeTag() {
        return fluxTypeTag;
    }

    @Override
    protected Tag getFluxInstanceTag() {
        return fluxInstanceTag;
    }


    /**
     *
     * @return List<Monitor> con la estadística de hystrix.
     */
    private List<Monitor<?>> getMonitors() {

        List<Monitor<?>> monitors = new ArrayList<Monitor<?>>();

        monitors.add(new InformationalMetric<String>(MonitorConfig.builder("name").build()) {
            @Override
            public String getValue() {
                return key.name();
            }
        });

        monitors.add(new GaugeMetric(MonitorConfig.builder("currentTime").withTag(DataSourceLevel.DEBUG).build()) {
            @Override
            public Number getValue() {
                return System.currentTimeMillis();
            }
        });

        //collapser event cumulative metrics
        monitors.add(safelyGetCumulativeMonitor("countRequestsBatched", new Func0<HystrixEventType.Collapser>() {
            @Override
            public HystrixEventType.Collapser call() {
                return HystrixEventType.Collapser.ADDED_TO_BATCH;
            }
        }));
        monitors.add(safelyGetCumulativeMonitor("countBatches", new Func0<HystrixEventType.Collapser>() {
            @Override
            public HystrixEventType.Collapser call() {
                return HystrixEventType.Collapser.BATCH_EXECUTED;
            }
        }));
        monitors.add(safelyGetCumulativeMonitor("countResponsesFromCache", new Func0<HystrixEventType.Collapser>() {
            @Override
            public HystrixEventType.Collapser call() {
                return HystrixEventType.Collapser.RESPONSE_FROM_CACHE;
            }
        }));

        //batch size distribution metrics
        monitors.add(getBatchSizeMeanMonitor("batchSize_mean"));
        monitors.add(getBatchSizePercentileMonitor("batchSize_percentile_75", 75));
        monitors.add(getBatchSizePercentileMonitor("batchSize_percentile_95", 95));
        monitors.add(getBatchSizePercentileMonitor("batchSize_percentile_99", 99));
        monitors.add(getBatchSizePercentileMonitor("batchSize_percentile_99_5", 99.5));
        monitors.add(getBatchSizePercentileMonitor("batchSize_percentile_100", 100));

        //shard size distribution metrics
        monitors.add(getShardSizeMeanMonitor("shardSize_mean"));
        monitors.add(getShardSizePercentileMonitor("shardSize_percentile_75", 75));
        monitors.add(getShardSizePercentileMonitor("shardSize_percentile_95", 95));
        monitors.add(getShardSizePercentileMonitor("shardSize_percentile_99", 99));
        monitors.add(getShardSizePercentileMonitor("shardSize_percentile_99_5", 99.5));
        monitors.add(getShardSizePercentileMonitor("shardSize_percentile_100", 100));

        // properties (so the values can be inspected and monitored)
        monitors.add(new InformationalMetric<Number>(MonitorConfig.builder("propertyValue_rollingStatisticalWindowInMilliseconds").build()) {
            @Override
            public Number getValue() {
                return properties.metricsRollingStatisticalWindowInMilliseconds().get();
            }
        });

        monitors.add(new InformationalMetric<Boolean>(MonitorConfig.builder("propertyValue_requestCacheEnabled").build()) {
            @Override
            public Boolean getValue() {
                return properties.requestCacheEnabled().get();
            }
        });

        monitors.add(new InformationalMetric<Number>(MonitorConfig.builder("propertyValue_maxRequestsInBatch").build()) {
            @Override
            public Number getValue() {
                return properties.maxRequestsInBatch().get();
            }
        });

        monitors.add(new InformationalMetric<Number>(MonitorConfig.builder("propertyValue_timerDelayInMilliseconds").build()) {
            @Override
            public Number getValue() {
                return properties.timerDelayInMilliseconds().get();
            }
        });

        return monitors;
    }



    protected Monitor<Number> getCumulativeMonitor(final String name, final HystrixEventType.Collapser event) {
        return new CounterMetric(MonitorConfig.builder(name).withTag(getFluxTypeTag()).withTag(getFluxInstanceTag()).build()) {
            @Override
            public Long getValue() {
                return metrics.getCumulativeCount(event);
            }
        };
    }

    protected Monitor<Number> safelyGetCumulativeMonitor(final String name, final Func0<HystrixEventType.Collapser> eventThunk) {
        return new CounterMetric(MonitorConfig.builder(name).withTag(getFluxTypeTag()).withTag(getFluxInstanceTag()).build()) {
            @Override
            public Long getValue() {
                return metrics.getCumulativeCount(eventThunk.call());
            }
        };
    }

    protected Monitor<Number> getRollingMonitor(final String name, final HystrixEventType.Collapser event) {
        return new GaugeMetric(MonitorConfig.builder(name).withTag(DataSourceLevel.DEBUG).withTag(getFluxTypeTag()).withTag(getFluxInstanceTag()).build()) {
            @Override
            public Long getValue() {
                return metrics.getRollingCount(event);
            }
        };
    }

    protected Monitor<Number> safelyGetRollingMonitor(final String name, final Func0<HystrixEventType.Collapser> eventThunk) {
        return new GaugeMetric(MonitorConfig.builder(name).withTag(DataSourceLevel.DEBUG).withTag(getFluxTypeTag()).withTag(getFluxInstanceTag()).build()) {
            @Override
            public Long getValue() {
                return metrics.getRollingCount(eventThunk.call());
            }
        };
    }

    protected Monitor<Number> getBatchSizeMeanMonitor(final String name) {
        return new GaugeMetric(MonitorConfig.builder(name).build()) {
            @Override
            public Number getValue() {
                return metrics.getBatchSizeMean();
            }
        };
    }

    protected Monitor<Number> getBatchSizePercentileMonitor(final String name, final double percentile) {
        return new GaugeMetric(MonitorConfig.builder(name).build()) {
            @Override
            public Number getValue() {
                return metrics.getBatchSizePercentile(percentile);
            }
        };
    }

    protected Monitor<Number> getShardSizeMeanMonitor(final String name) {
        return new GaugeMetric(MonitorConfig.builder(name).build()) {
            @Override
            public Number getValue() {
                return metrics.getShardSizeMean();
            }
        };
    }

    protected Monitor<Number> getShardSizePercentileMonitor(final String name, final double percentile) {
        return new GaugeMetric(MonitorConfig.builder(name).build()) {
            @Override
            public Number getValue() {
                return metrics.getShardSizePercentile(percentile);
            }
        };
    }

}
