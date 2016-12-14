package com.hystrixdemo.cliente;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;


@RestController
public class ApiController {
	
	protected Logger logger = Logger.getLogger(ApiController.class
			.getName());
	
	private static final String template = "El dt es: %s!";
	

	@RequestMapping(path = "/api/dt-seleccion/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Respuesta greeting(@PathVariable("name") String name) {
        return new Respuesta(String.format(template, name));
    }

}
