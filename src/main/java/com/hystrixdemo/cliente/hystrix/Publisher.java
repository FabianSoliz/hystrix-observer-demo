package com.hystrixdemo.cliente.hystrix;

import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.servo.publish.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 *
 *
 */
public class Publisher {

    private static Publisher instance = new Publisher();
    private List<MetricObserver> observers;

    private Publisher() {
        observers = new ArrayList<MetricObserver>();
    }

    public static Publisher getInstance() {
        return instance;
    }

    public Publisher addFluxPublisher() {
        HystrixPlugins.getInstance().registerMetricsPublisher(HystrixFluxMetricsPublisher.getInstance());
        return instance;
    }

    public Publisher addDataDogMetricObserver(String name) {
        observers.add(new DatadogMetricObserver(name));
        return instance;
    }

    public Publisher addMetricObserver(BaseMetricObserver observer) {
        observers.add(observer);
        return instance;
    }


    public void start(int milisegundos) {
        PollScheduler.getInstance().start();
        PollRunnable registeryTask = new PollRunnable(new MonitorRegistryMetricPoller(), BasicMetricFilter.MATCH_ALL, observers);
        PollScheduler.getInstance().addPoller(registeryTask, milisegundos, TimeUnit.MILLISECONDS);
    }


}
