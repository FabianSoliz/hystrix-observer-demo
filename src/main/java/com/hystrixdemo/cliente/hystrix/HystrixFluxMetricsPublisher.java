package com.hystrixdemo.cliente.hystrix;

import com.netflix.hystrix.*;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisher;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherCollapser;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherCommand;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherThreadPool;

/**
 * Implementación de publisher para obtener las metricas de flux.
 */
public class HystrixFluxMetricsPublisher extends HystrixMetricsPublisher {

    private static HystrixFluxMetricsPublisher instance = null;


    private HystrixFluxMetricsPublisher() {
    }

    /**
     *
     * @return la instancia de HystrixFluxMetricsPublisher.
     */
    public static HystrixFluxMetricsPublisher getInstance() {
        if (instance == null) {
            instance = new HystrixFluxMetricsPublisher();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public HystrixMetricsPublisherCommand getMetricsPublisherForCommand(HystrixCommandKey commandKey,
                                                                        HystrixCommandGroupKey commandGroupKey,
                                                                        HystrixCommandMetrics metrics,
                                                                        HystrixCircuitBreaker circuitBreaker,
                                                                        HystrixCommandProperties properties) {
        return new HystrixFluxMetricsPublisherCommand(commandKey, commandGroupKey, metrics, circuitBreaker, properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HystrixMetricsPublisherThreadPool getMetricsPublisherForThreadPool(HystrixThreadPoolKey threadPoolKey,
                                                                              HystrixThreadPoolMetrics metrics,
                                                                              HystrixThreadPoolProperties properties) {
        return new HystrixFluxMetricsPublisherThreadPool(threadPoolKey, metrics, properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HystrixMetricsPublisherCollapser getMetricsPublisherForCollapser(HystrixCollapserKey collapserKey,
                                                                            HystrixCollapserMetrics metrics,
                                                                            HystrixCollapserProperties properties) {
        return new HystrixFluxMetricsPublisherCollapser(collapserKey, metrics, properties);
    }
}
