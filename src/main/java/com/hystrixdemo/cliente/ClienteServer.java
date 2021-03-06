package com.hystrixdemo.cliente;


import com.codahale.metrics.MetricRegistry;
import com.hystrixdemo.cliente.hystrix.PublisherConfig;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.hystrix.Hystrix;
import com.netflix.hystrix.contrib.codahalemetricspublisher.HystrixCodaHaleMetricsPublisher;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
//@EnableDiscoveryClient
@EnableCircuitBreaker
@EnableAutoConfiguration
@EnableHystrixDashboard
//@EnableTurbine
@ComponentScan(useDefaultFilters = false)
public class ClienteServer {


	/**
	 * URL registrado en eureka.
	 */
	public static final String SERVICE_URL = "http://API-SERVICE";


	public static void main(String[] args) {
		SpringApplication.run(ClienteServer.class, args);

		Hystrix.reset();
		HystrixPlugins.reset();

		PublisherConfig.getInstance().addFluxPublisher().addDataDogMetricObserver("demo").start(5000);
	}


	@LoadBalanced
	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}



	@Bean
	public ClienteService clienteService() {
		return new ClienteService(SERVICE_URL);
	}


	@Bean
	public ClienteController clienteController() {
		return new ClienteController(clienteService());
	}

	@Bean
	public ApiController apiController() {
		return new ApiController();
	}
	@Bean
	HystrixMetricsPublisher hystrixMetricsPublisher(MetricRegistry metricRegistry) {
		HystrixCodaHaleMetricsPublisher publisher = new HystrixCodaHaleMetricsPublisher(metricRegistry);
		HystrixPlugins.getInstance().registerMetricsPublisher(publisher);
		return publisher;
	}


//	@Bean
//	public GraphiteReporter graphiteReporter(MetricRegistry metricRegistry) {
//
//		InetSocketAddress address = new InetSocketAddress("localhost", 2003);
//		Graphite g = new Graphite(address);
//
//		final GraphiteReporter reporter = GraphiteReporter
//				.forRegistry(metricRegistry)
//				.build(g);
//		reporter.start(1, TimeUnit.SECONDS);
//		return reporter;
//	}



}
