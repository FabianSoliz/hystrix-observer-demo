package com.hystrixdemo.cliente;

import java.util.concurrent.ExecutionException;

import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ClienteController {

	@Autowired
	protected ClienteService clienteService;

	protected Logger logger = Logger.getLogger(ClienteController.class.getName());

	// constructor
	public ClienteController(ClienteService helloWorldService) {
		this.clienteService = helloWorldService;
	}


	@RequestMapping("/dt-seleccion/{name}")
	public Respuesta getDt(Model model, @PathVariable("name") String name) {
		Respuesta respuesta = null;

		logger.info("getDt() invoked: " + name);

		respuesta = clienteService.getDt(name);

		logger.info("getDt() found: " + respuesta.getContent());

		return respuesta;

	}

	@RequestMapping("/dt-seleccion-async/{name}")
	public String getDtAsync(Model model, @PathVariable("name") String name)
			throws InterruptedException, ExecutionException {

		logger.info("getAsync() invoked: " + name);

		Future<Respuesta> respuesta = clienteService.getAsync(name);

		logger.info("getAsync() found: " + respuesta.get().getContent());

		return respuesta.get().getContent();

	}


	@RequestMapping("/dt-seleccion2/{name}")
	public String getDt2(Model model, @PathVariable("name") String name) {
		Respuesta resp = null;

		logger.info("getDt2() invoked: " + name);

		resp = clienteService.getDt2(name);

		logger.info("getDt2() found: " + resp.getContent());


		return resp.getContent();

	}

	@RequestMapping("/dt-seleccion-async2/{name}")
	public String getDtAsync2(Model model, @PathVariable("name") String name)
			throws InterruptedException, ExecutionException {

		logger.info("getAsync2() invoked: " + name);

		Future<Respuesta> respuesta = clienteService.getAsync2(name);

		logger.info("getAsync2() found: " + respuesta.get().getContent());

		return respuesta.get().getContent();

	}


}
