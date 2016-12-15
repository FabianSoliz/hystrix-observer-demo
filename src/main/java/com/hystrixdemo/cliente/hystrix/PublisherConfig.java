package com.hystrixdemo.cliente.hystrix;

import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.servo.publish.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * Utilitario para configurar el publisher y los observers.
 */
public class PublisherConfig {

    private static PublisherConfig instance = new PublisherConfig();
    private List<MetricObserver> observers;

    private PublisherConfig() {
        observers = new ArrayList<MetricObserver>();
    }

    /**
     *
     * @return una instancia de PublisherConfig.
     */
    public static PublisherConfig getInstance() {
        return instance;
    }

    /**
     * Agrega un flux publisher a hystrix.
     * @return la instancia de publisher.
     */
    public PublisherConfig addFluxPublisher() {
        HystrixPlugins.getInstance().registerMetricsPublisher(HystrixFluxMetricsPublisher.getInstance());
        return instance;
    }

    /**
     * Agrega un dataDogMeticObserver a hystrix.
     * @param name del observer.
     * @return la instancia de publisher.
     */
    public PublisherConfig addDataDogMetricObserver(String name) {
        observers.add(new DatadogMetricObserver(name));
        return instance;
    }

    /**
     * Agrega un observerMetric a la lista de observers.
     * @param observer a agregar.
     * @return la instancia de publisher.
     */
    public PublisherConfig addMetricObserver(BaseMetricObserver observer) {
        observers.add(observer);
        return instance;
    }


    /**
     * Configura cada cuantos milisegundos se obtendrán las métricas e inicia el observer.
     * @param milisegundos
     */
    public void start(int milisegundos) {
        PollScheduler.getInstance().start();
        PollRunnable registeryTask = new PollRunnable(new MonitorRegistryMetricPoller(), BasicMetricFilter.MATCH_ALL, observers);
        PollScheduler.getInstance().addPoller(registeryTask, milisegundos, TimeUnit.MILLISECONDS);
    }


}
