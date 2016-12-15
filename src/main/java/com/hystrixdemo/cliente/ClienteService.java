package com.hystrixdemo.cliente;

import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.command.AsyncResult;


@Service
public class ClienteService {

	@Autowired
	@LoadBalanced
	protected RestTemplate restTemplate;

	protected Respuesta greeting;

	protected String serviceUrl;

	protected Logger logger = Logger.getLogger(ClienteService.class.getName());

	public ClienteService(String serviceUrl) {
		this.serviceUrl = serviceUrl;
		restTemplate = new RestTemplate();
	}


	@HystrixCommand(fallbackMethod = "getDtDefault", groupKey = "ungroupkey", commandKey = "uncommandey")
	public Respuesta getDt(String name) {
		return restTemplate.getForObject(serviceUrl + "/dt-seleccion/{name}", Respuesta.class, name);
	}


	public Respuesta getDtDefault(String name) {
		return new Respuesta("Circuit Breaker (Hystrix) - SYNCHRONOUS: El dt es Caruso!!");
	}


	@HystrixCommand(fallbackMethod = "getAsyncDefault")
	public Future<Respuesta> getAsync(final String name) {
		return new AsyncResult<Respuesta>() {
			public Respuesta invoke() {
				return restTemplate.getForObject(serviceUrl + "/dt-seleccion/{name}", Respuesta.class, name);
			}
		}; 		
	}

	public Respuesta getAsyncDefault(String name) {
		return new Respuesta("Circuit Breaker (Hystrix) - (ASYNCHRONOUS) : El dt es caruso!!");
	}


	@HystrixCommand(fallbackMethod = "getDtDefault2", groupKey = "ungroupkey2", commandKey = "uncommandey2")
	public Respuesta getDt2(String name) {
		return new RestTemplate().getForObject("http://localhost:8082/api/dt-seleccion/{name}", Respuesta.class, name);
	}


	public Respuesta getDtDefault2(String name) {
		return new Respuesta("Circuit Breaker (Hystrix) - SYNCHRONOUS: El dt es Caruso!!");
	}


	@HystrixCommand(fallbackMethod = "getAsyncDefault2")
	public Future<Respuesta> getAsync2(final String name) {
		return new AsyncResult<Respuesta>() {
			public Respuesta invoke() {
				return new RestTemplate().getForObject("http://localhost:8082/api/dt-seleccion/{name}", Respuesta.class, name);
			}
		};
	}

	public Respuesta getAsyncDefault2(String name) {
		return new Respuesta("Circuit Breaker (Hystrix) - ASYNCHRONOUS : El dt es caruso!!");
	}
}
