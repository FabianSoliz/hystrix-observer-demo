package com.hystrixdemo.cliente.hystrix;

import com.netflix.hystrix.HystrixEventType;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolMetrics;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.metric.consumer.CumulativeThreadPoolEventCounterStream;
import com.netflix.hystrix.metric.consumer.RollingThreadPoolEventCounterStream;
import com.netflix.hystrix.metric.consumer.RollingThreadPoolMaxConcurrencyStream;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherThreadPool;
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
 * Implementación que recolecta las ThreadMetricas de hystrix y las almacena en un {@link com.netflix.servo.monitor.Monitor}.
 */
public class HystrixFluxMetricsPublisherThreadPool extends HystrixFluxMetricsPublisherAbstract implements HystrixMetricsPublisherThreadPool {

    private final HystrixThreadPoolKey key;
    private final HystrixThreadPoolMetrics metrics;
    private final HystrixThreadPoolProperties properties;
    private final Tag fluxInstanceTag;
    private final Tag fluxTypeTag;

    public HystrixFluxMetricsPublisherThreadPool(HystrixThreadPoolKey threadPoolKey, HystrixThreadPoolMetrics metrics, HystrixThreadPoolProperties properties) {
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
                return "HystrixThreadPool";
            }

            @Override
            public String tagString() {
                return "HystrixThreadPool";
            }

        };
    }

    @Override
    public void initialize() {
        /* list of monitors */
        List<Monitor<?>> monitors = getMonitors();

        // publish metrics together under a single composite (it seems this name is ignored)
        MonitorConfig commandMetricsConfig = MonitorConfig.builder("HystrixThreadPool_" + key.name()).build();
        BasicCompositeMonitor commandMetricsMonitor = new BasicCompositeMonitor(commandMetricsConfig, monitors);

        DefaultMonitorRegistry.getInstance().register(commandMetricsMonitor);
        RollingThreadPoolEventCounterStream.getInstance(key, properties).startCachingStreamValuesIfUnstarted();
        CumulativeThreadPoolEventCounterStream.getInstance(key, properties).startCachingStreamValuesIfUnstarted();
        RollingThreadPoolMaxConcurrencyStream.getInstance(key, properties).startCachingStreamValuesIfUnstarted();
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

        monitors.add(new GaugeMetric(MonitorConfig.builder("threadActiveCount").build()) {
            @Override
            public Number getValue() {
                return metrics.getCurrentActiveCount();
            }
        });

        monitors.add(new GaugeMetric(MonitorConfig.builder("completedTaskCount").build()) {
            @Override
            public Number getValue() {
                return metrics.getCurrentCompletedTaskCount();
            }
        });

        monitors.add(new GaugeMetric(MonitorConfig.builder("largestPoolSize").build()) {
            @Override
            public Number getValue() {
                return metrics.getCurrentLargestPoolSize();
            }
        });

        monitors.add(new GaugeMetric(MonitorConfig.builder("totalTaskCount").build()) {
            @Override
            public Number getValue() {
                return metrics.getCurrentTaskCount();
            }
        });

        monitors.add(new GaugeMetric(MonitorConfig.builder("queueSize").build()) {
            @Override
            public Number getValue() {
                return metrics.getCurrentQueueSize();
            }
        });

        monitors.add(new GaugeMetric(MonitorConfig.builder("rollingMaxActiveThreads").withTag(DataSourceLevel.DEBUG).build()) {
            @Override
            public Number getValue() {
                return metrics.getRollingMaxActiveThreads();
            }
        });

        //thread pool event monitors
        monitors.add(safelyGetCumulativeMonitor("countThreadsExecuted", new Func0<HystrixEventType.ThreadPool>() {
            @Override
            public HystrixEventType.ThreadPool call() {
                return HystrixEventType.ThreadPool.EXECUTED;
            }
        }));
        monitors.add(safelyGetCumulativeMonitor("countThreadsRejected", new Func0<HystrixEventType.ThreadPool>() {
            @Override
            public HystrixEventType.ThreadPool call() {
                return HystrixEventType.ThreadPool.REJECTED;
            }
        }));
        monitors.add(safelyGetRollingMonitor("rollingCountThreadsExecuted", new Func0<HystrixEventType.ThreadPool>() {
            @Override
            public HystrixEventType.ThreadPool call() {
                return HystrixEventType.ThreadPool.EXECUTED;
            }
        }));
        monitors.add(safelyGetRollingMonitor("rollingCountCommandsRejected", new Func0<HystrixEventType.ThreadPool>() {
            @Override
            public HystrixEventType.ThreadPool call() {
                return HystrixEventType.ThreadPool.REJECTED;
            }
        }));

        // properties
        monitors.add(new InformationalMetric<Number>(MonitorConfig.builder("propertyValue_corePoolSize").build()) {
            @Override
            public Number getValue() {
                return properties.coreSize().get();
            }
        });

        monitors.add(new InformationalMetric<Number>(MonitorConfig.builder("propertyValue_keepAliveTimeInMinutes").build()) {
            @Override
            public Number getValue() {
                return properties.keepAliveTimeMinutes().get();
            }
        });

        monitors.add(new InformationalMetric<Number>(MonitorConfig.builder("propertyValue_queueSizeRejectionThreshold").build()) {
            @Override
            public Number getValue() {
                return properties.queueSizeRejectionThreshold().get();
            }
        });

        monitors.add(new InformationalMetric<Number>(MonitorConfig.builder("propertyValue_maxQueueSize").build()) {
            @Override
            public Number getValue() {
                return properties.maxQueueSize().get();
            }
        });

        return monitors;
    }

    protected Monitor<Number> safelyGetCumulativeMonitor(final String name, final Func0<HystrixEventType.ThreadPool> eventThunk) {
        return new CounterMetric(MonitorConfig.builder(name).withTag(getFluxTypeTag()).withTag(getFluxInstanceTag()).build()) {
            @Override
            public Long getValue() {
                return metrics.getCumulativeCount(eventThunk.call());
            }
        };
    }


    protected Monitor<Number> safelyGetRollingMonitor(final String name, final Func0<HystrixEventType.ThreadPool> eventThunk) {
        return new GaugeMetric(MonitorConfig.builder(name).withTag(DataSourceLevel.DEBUG).withTag(getFluxTypeTag()).withTag(getFluxInstanceTag()).build()) {
            @Override
            public Long getValue() {
                return metrics.getRollingCount(eventThunk.call());
            }
        };
    }
}
