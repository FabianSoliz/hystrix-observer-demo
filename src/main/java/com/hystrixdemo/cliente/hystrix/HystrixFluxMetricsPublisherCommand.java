package com.hystrixdemo.cliente.hystrix;

import com.netflix.hystrix.*;
import com.netflix.hystrix.metric.consumer.*;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherCommand;
import com.netflix.hystrix.util.HystrixRollingNumberEvent;
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
 * Implementación que recolecta las CommandMetricas de hystrix y las almacena en un {@link com.netflix.servo.monitor.Monitor}.
 */
public class HystrixFluxMetricsPublisherCommand extends HystrixFluxMetricsPublisherAbstract implements HystrixMetricsPublisherCommand {

    private final HystrixCommandKey key;
    private final HystrixCommandGroupKey commandGroupKey;
    private final HystrixCommandMetrics metrics;
    private final HystrixCircuitBreaker circuitBreaker;
    private final HystrixCommandProperties properties;


    public HystrixFluxMetricsPublisherCommand(HystrixCommandKey commandKey, final HystrixCommandGroupKey commandGroupKey,
                                              HystrixCommandMetrics metrics, HystrixCircuitBreaker circuitBreaker,
                                              HystrixCommandProperties properties) {
        super();
        this.key = commandKey;
        this.commandGroupKey = commandGroupKey;
        this.metrics = metrics;
        this.circuitBreaker = circuitBreaker;
        this.properties = properties;

        getTagList().add(new Tag() {

            @Override
            public String getKey() {
                return "commandKey";
            }

            @Override
            public String getValue() {
                return key.name();
            }

            @Override
            public String tagString() {
                return key.name();
            }

        });

        getTagList().add(new Tag() {

            @Override
            public String getKey() {
                return "commandGroupKey";
            }

            @Override
            public String getValue() {
                return commandGroupKey.name();
            }

            @Override
            public String tagString() {
                return commandGroupKey.name();
            }

        });

        getTagList().add(new Tag() {

            @Override
            public String getKey() {
                return "type";
            }

            @Override
            public String getValue() {
                return "HystrixMetricsPublisherCommand";
            }

            @Override
            public String tagString() {
                return "HystrixMetricsPublisherCommand";
            }

        });
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        List<Monitor<?>> monitors = getMonitors();

        // publish metrics together under a single composite (it seems this name is ignored)
        MonitorConfig commandMetricsConfig = MonitorConfig.builder("HystrixCommand_" + key.name()).build();
        BasicCompositeMonitor commandMetricsMonitor = new BasicCompositeMonitor(commandMetricsConfig, monitors);

        DefaultMonitorRegistry.getInstance().register(commandMetricsMonitor);
        RollingCommandEventCounterStream.getInstance(key, properties).startCachingStreamValuesIfUnstarted();
        CumulativeCommandEventCounterStream.getInstance(key, properties).startCachingStreamValuesIfUnstarted();
        RollingCommandLatencyDistributionStream.getInstance(key, properties).startCachingStreamValuesIfUnstarted();
        RollingCommandUserLatencyDistributionStream.getInstance(key, properties).startCachingStreamValuesIfUnstarted();
        RollingCommandMaxConcurrencyStream.getInstance(key, properties).startCachingStreamValuesIfUnstarted();
    }


    /**
     *
     * @return List<Monitor> con las estadísticas de hystrix.
     */
    private List<Monitor<?>> getMonitors() {

        List<Monitor<?>> monitors = new ArrayList<Monitor<?>>();

        // group
        monitors.add(new InformationalMetric<String>(MonitorConfig.builder("commandGroup").build()) {
            @Override
            public String getValue() {
                return commandGroupKey != null ? commandGroupKey.name() : null;
            }
        });

        // commandkey
        monitors.add(new InformationalMetric<String>(MonitorConfig.builder("commandKey").build()) {
            @Override
            public String getValue() {
                return key != null ? key.name() : null;
            }
        });

        monitors.add(new InformationalMetric<Boolean>(MonitorConfig.builder("isCircuitBreakerOpen").build()) {
            @Override
            public Boolean getValue() {
                return circuitBreaker.isOpen();
            }
        });

        monitors.add(getCurrentValueMonitor("currentTime", currentTimeThunk, DataSourceLevel.DEBUG));

        // cumulative counts
        monitors.add(safelyGetCumulativeMonitor("countBadRequests", new Func0<HystrixEventType>() {
            @Override
            public HystrixEventType call() {
                return HystrixEventType.BAD_REQUEST;
            }
        }));
        monitors.add(safelyGetCumulativeMonitor("countCollapsedRequests", new Func0<HystrixEventType>() {
            @Override
            public HystrixEventType call() {
                return HystrixEventType.COLLAPSED;
            }
        }));
        monitors.add(safelyGetCumulativeMonitor("countExceptionsThrown", new Func0<HystrixEventType>() {
            @Override
            public HystrixEventType call() {
                return HystrixEventType.EXCEPTION_THROWN;
            }
        }));
        monitors.add(safelyGetCumulativeMonitor("countFailure", new Func0<HystrixEventType>() {
            @Override
            public HystrixEventType call() {
                return HystrixEventType.FAILURE;
            }
        }));
        monitors.add(safelyGetCumulativeMonitor("countFallbackFailure", new Func0<HystrixEventType>() {
            @Override
            public HystrixEventType call() {
                return HystrixEventType.FALLBACK_FAILURE;
            }
        }));
        monitors.add(safelyGetCumulativeMonitor("countFallbackMissing", new Func0<HystrixEventType>() {
            @Override
            public HystrixEventType call() {
                return HystrixEventType.FALLBACK_MISSING;
            }
        }));
        monitors.add(safelyGetCumulativeMonitor("countFallbackRejection", new Func0<HystrixEventType>() {
            @Override
            public HystrixEventType call() {
                return HystrixEventType.FALLBACK_REJECTION;
            }
        }));
        monitors.add(safelyGetCumulativeMonitor("countFallbackSuccess", new Func0<HystrixEventType>() {
            @Override
            public HystrixEventType call() {
                return HystrixEventType.FALLBACK_SUCCESS;
            }
        }));

        monitors.add(safelyGetCumulativeMonitor("countShortCircuited", new Func0<HystrixEventType>() {
            @Override
            public HystrixEventType call() {
                return HystrixEventType.SHORT_CIRCUITED;
            }
        }));
        monitors.add(safelyGetCumulativeMonitor("countSuccess", new Func0<HystrixEventType>() {
            @Override
            public HystrixEventType call() {
                return HystrixEventType.SUCCESS;
            }
        }));
        monitors.add(safelyGetCumulativeMonitor("countThreadPoolRejected", new Func0<HystrixEventType>() {
            @Override
            public HystrixEventType call() {
                return HystrixEventType.THREAD_POOL_REJECTED;
            }
        }));
        monitors.add(safelyGetCumulativeMonitor("countTimeout", new Func0<HystrixEventType>() {
            @Override
            public HystrixEventType call() {
                return HystrixEventType.TIMEOUT;
            }
        }));

        // rolling counts
        monitors.add(safelyGetRollingMonitor("rollingCountBadRequests", new Func0<HystrixEventType>() {
            @Override
            public HystrixEventType call() {
                return HystrixEventType.BAD_REQUEST;
            }
        }));
        monitors.add(safelyGetRollingMonitor("rollingCountCollapsedRequests", new Func0<HystrixEventType>() {
            @Override
            public HystrixEventType call() {
                return HystrixEventType.COLLAPSED;
            }
        }));
        monitors.add(safelyGetRollingMonitor("rollingCountExceptionsThrown", new Func0<HystrixEventType>() {
            @Override
            public HystrixEventType call() {
                return HystrixEventType.EXCEPTION_THROWN;
            }
        }));
        monitors.add(safelyGetRollingMonitor("rollingCountFailure", new Func0<HystrixEventType>() {
            @Override
            public HystrixEventType call() {
                return HystrixEventType.FAILURE;
            }
        }));
        monitors.add(safelyGetRollingMonitor("rollingCountFallbackEmit", new Func0<HystrixEventType>() {
            @Override
            public HystrixEventType call() {
                return HystrixEventType.FALLBACK_EMIT;
            }
        }));
        monitors.add(safelyGetRollingMonitor("rollingCountFallbackFailure", new Func0<HystrixEventType>() {
            @Override
            public HystrixEventType call() {
                return HystrixEventType.FALLBACK_FAILURE;
            }
        }));
        monitors.add(safelyGetRollingMonitor("rollingCountFallbackMissing", new Func0<HystrixEventType>() {
            @Override
            public HystrixEventType call() {
                return HystrixEventType.FALLBACK_MISSING;
            }
        }));
        monitors.add(safelyGetRollingMonitor("rollingCountFallbackRejection", new Func0<HystrixEventType>() {
            @Override
            public HystrixEventType call() {
                return HystrixEventType.FALLBACK_REJECTION;
            }
        }));
        monitors.add(safelyGetRollingMonitor("rollingCountFallbackSuccess", new Func0<HystrixEventType>() {
            @Override
            public HystrixEventType call() {
                return HystrixEventType.FALLBACK_SUCCESS;
            }
        }));
        monitors.add(safelyGetRollingMonitor("rollingCountResponsesFromCache", new Func0<HystrixEventType>() {
            @Override
            public HystrixEventType call() {
                return HystrixEventType.RESPONSE_FROM_CACHE;
            }
        }));
        monitors.add(safelyGetRollingMonitor("rollingCountSemaphoreRejected", new Func0<HystrixEventType>() {
            @Override
            public HystrixEventType call() {
                return HystrixEventType.SEMAPHORE_REJECTED;
            }
        }));
        monitors.add(safelyGetRollingMonitor("rollingCountShortCircuited", new Func0<HystrixEventType>() {
            @Override
            public HystrixEventType call() {
                return HystrixEventType.SHORT_CIRCUITED;
            }
        }));
        monitors.add(safelyGetRollingMonitor("rollingCountSuccess", new Func0<HystrixEventType>() {
            @Override
            public HystrixEventType call() {
                return HystrixEventType.SUCCESS;
            }
        }));
        monitors.add(safelyGetRollingMonitor("rollingCountThreadPoolRejected", new Func0<HystrixEventType>() {
            @Override
            public HystrixEventType call() {
                return HystrixEventType.THREAD_POOL_REJECTED;
            }
        }));
        monitors.add(safelyGetRollingMonitor("rollingCountTimeout", new Func0<HystrixEventType>() {
            @Override
            public HystrixEventType call() {
                return HystrixEventType.TIMEOUT;
            }
        }));

        // the number of executionSemaphorePermits in use right now
        monitors.add(getCurrentValueMonitor("executionSemaphorePermitsInUse", currentConcurrentExecutionCountThunk));

        // error percentage derived from current metrics
        monitors.add(getCurrentValueMonitor("errorPercentage", errorPercentageThunk));

        // execution latency metrics
        monitors.add(getExecutionLatencyMeanMonitor("latencyExecute_mean"));
        monitors.add(getExecutionLatencyPercentileMonitor("latencyExecute_percentile_75", 75));
        monitors.add(getExecutionLatencyPercentileMonitor("latencyExecute_percentile_90", 90));
        monitors.add(getExecutionLatencyPercentileMonitor("latencyExecute_percentile_99", 99));
        monitors.add(getExecutionLatencyPercentileMonitor("latencyExecute_percentile_995", 99.5));

        // total latency metrics
        monitors.add(getTotalLatencyMeanMonitor("latencyTotal_mean"));
        monitors.add(getTotalLatencyPercentileMonitor("latencyTotal_percentile_75", 75));
        monitors.add(getTotalLatencyPercentileMonitor("latencyTotal_percentile_90", 90));
        monitors.add(getTotalLatencyPercentileMonitor("latencyTotal_percentile_99", 99));
        monitors.add(getTotalLatencyPercentileMonitor("latencyTotal_percentile_995", 99.5));



        // properties (so the values can be inspected and monitored)
        monitors.add(new InformationalMetric<Number>(MonitorConfig.builder("propertyValue_rollingStatisticalWindowInMilliseconds").build()) {
            @Override
            public Number getValue() {
                return properties.metricsRollingStatisticalWindowInMilliseconds().get();
            }
        });
        monitors.add(new InformationalMetric<Number>(MonitorConfig.builder("propertyValue_circuitBreakerRequestVolumeThreshold").build()) {
            @Override
            public Number getValue() {
                return properties.circuitBreakerRequestVolumeThreshold().get();
            }
        });
        monitors.add(new InformationalMetric<Number>(MonitorConfig.builder("propertyValue_circuitBreakerSleepWindowInMilliseconds").build()) {
            @Override
            public Number getValue() {
                return properties.circuitBreakerSleepWindowInMilliseconds().get();
            }
        });
        monitors.add(new InformationalMetric<Number>(MonitorConfig.builder("propertyValue_circuitBreakerErrorThresholdPercentage").build()) {
            @Override
            public Number getValue() {
                return properties.circuitBreakerErrorThresholdPercentage().get();
            }
        });
        monitors.add(new InformationalMetric<Boolean>(MonitorConfig.builder("propertyValue_circuitBreakerForceOpen").build()) {
            @Override
            public Boolean getValue() {
                return properties.circuitBreakerForceOpen().get();
            }
        });
        monitors.add(new InformationalMetric<Boolean>(MonitorConfig.builder("propertyValue_circuitBreakerForceClosed").build()) {
            @Override
            public Boolean getValue() {
                return properties.circuitBreakerForceClosed().get();
            }
        });
        monitors.add(new InformationalMetric<Number>(MonitorConfig.builder("propertyValue_executionIsolationThreadTimeoutInMilliseconds").build()) {
            @Override
            public Number getValue() {
                return properties.executionTimeoutInMilliseconds().get();
            }
        });
        monitors.add(new InformationalMetric<Number>(MonitorConfig.builder("propertyValue_executionTimeoutInMilliseconds").build()) {
            @Override
            public Number getValue() {
                return properties.executionTimeoutInMilliseconds().get();
            }
        });
        monitors.add(new InformationalMetric<String>(MonitorConfig.builder("propertyValue_executionIsolationStrategy").build()) {
            @Override
            public String getValue() {
                return properties.executionIsolationStrategy().get().name();
            }
        });
        monitors.add(new InformationalMetric<Boolean>(MonitorConfig.builder("propertyValue_metricsRollingPercentileEnabled").build()) {
            @Override
            public Boolean getValue() {
                return properties.metricsRollingPercentileEnabled().get();
            }
        });
        monitors.add(new InformationalMetric<Boolean>(MonitorConfig.builder("propertyValue_requestCacheEnabled").build()) {
            @Override
            public Boolean getValue() {
                return properties.requestCacheEnabled().get();
            }
        });
        monitors.add(new InformationalMetric<Boolean>(MonitorConfig.builder("propertyValue_requestLogEnabled").build()) {
            @Override
            public Boolean getValue() {
                return properties.requestLogEnabled().get();
            }
        });
        monitors.add(new InformationalMetric<Number>(MonitorConfig.builder("propertyValue_executionIsolationSemaphoreMaxConcurrentRequests").build()) {
            @Override
            public Number getValue() {
                return properties.executionIsolationSemaphoreMaxConcurrentRequests().get();
            }
        });
        monitors.add(new InformationalMetric<Number>(MonitorConfig.builder("propertyValue_fallbackIsolationSemaphoreMaxConcurrentRequests").build()) {
            @Override
            public Number getValue() {
                return properties.fallbackIsolationSemaphoreMaxConcurrentRequests().get();
            }
        });

        return monitors;
    }


    protected final Func0<Number> currentConcurrentExecutionCountThunk = new Func0<Number>() {
        @Override
        public Integer call() {
            return metrics.getCurrentConcurrentExecutionCount();
        }
    };

    protected final Func0<Number> rollingMaxConcurrentExecutionCountThunk = new Func0<Number>() {
        @Override
        public Long call() {
            return metrics.getRollingMaxConcurrentExecutions();
        }
    };

    protected final Func0<Number> errorPercentageThunk = new Func0<Number>() {
        @Override
        public Integer call() {
            return metrics.getHealthCounts().getErrorPercentage();
        }
    };

    protected final Func0<Number> currentTimeThunk = new Func0<Number>() {
        @Override
        public Number call() {
            return System.currentTimeMillis();
        }
    };


    protected Monitor<Number> safelyGetCumulativeMonitor(final String name, final Func0<HystrixEventType> eventThunk) {
        return new CounterMetric(MonitorConfig.builder(name).build()) {
            @Override
            public Long getValue() {
                    HystrixEventType eventType = eventThunk.call();
                    return metrics.getCumulativeCount(HystrixRollingNumberEvent.from(eventType));
            }
        };
    }

    protected Monitor<Number> safelyGetRollingMonitor(final String name, final Func0<HystrixEventType> eventThunk) {
        return new GaugeMetric(MonitorConfig.builder(name).withTag(DataSourceLevel.DEBUG).build()) {
            @Override
            public Long getValue() {
                    HystrixEventType eventType = eventThunk.call();
                    return metrics.getRollingCount(HystrixRollingNumberEvent.from(eventType));
            }
        };
    }

    protected Monitor<Number> getExecutionLatencyMeanMonitor(final String name) {
        return new GaugeMetric(MonitorConfig.builder(name).build()) {
            @Override
            public Number getValue() {
                return metrics.getExecutionTimeMean();
            }
        };
    }

    protected Monitor<Number> getExecutionLatencyPercentileMonitor(final String name, final double percentile) {
        return new GaugeMetric(MonitorConfig.builder(name).build()) {
            @Override
            public Number getValue() {
                return metrics.getExecutionTimePercentile(percentile);
            }
        };
    }

    protected Monitor<Number> getTotalLatencyMeanMonitor(final String name) {
        return new GaugeMetric(MonitorConfig.builder(name).build()) {
            @Override
            public Number getValue() {
                return metrics.getTotalTimeMean();
            }
        };
    }

    protected Monitor<Number> getTotalLatencyPercentileMonitor(final String name, final double percentile) {
        return new GaugeMetric(MonitorConfig.builder(name).build()) {
            @Override
            public Number getValue() {
                return metrics.getTotalTimePercentile(percentile);
            }
        };
    }

    protected Monitor<Number> getCurrentValueMonitor(final String name, final Func0<Number> metricToEvaluate) {
        return new GaugeMetric(MonitorConfig.builder(name).build()) {
            @Override
            public Number getValue() {
                return metricToEvaluate.call();
            }
        };
    }

    protected Monitor<Number> getCurrentValueMonitor(final String name, final Func0<Number> metricToEvaluate, final Tag tag) {
        return new GaugeMetric(MonitorConfig.builder(name).withTag(tag).build()) {
            @Override
            public Number getValue() {
                return metricToEvaluate.call();
            }
        };
    }

}
