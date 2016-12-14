package com.hystrixdemo.cliente;


import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("respuesta")
public class Respuesta {

  
    protected String content;
    
    /**
	 * Default constructor 
	 */
	protected Respuesta() {
		this.content = "";
	}

    public Respuesta(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
