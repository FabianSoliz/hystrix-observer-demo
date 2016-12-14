package com.hystrixdemo;

import com.hystrixdemo.cliente.Respuesta;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

/**
 * Created by fsoliz on 7/12/16.
 */
public class ClienteTest {

    @Test
    public void test() throws Exception {

        while (true) {
            switch (new Random().nextInt(5)) {
                case 0: new RestTemplate().getForObject("http://localhost:8082/dt-seleccion/{name}", String.class, "343");
                        break;
                case 1: new RestTemplate().getForObject("http://localhost:8082/dt-seleccion2/{name}", String.class, "343");
                    break;
                case 2: new RestTemplate().getForObject("http://localhost:8082/dt-seleccion-async/{name}", String.class, "343");
                    break;
                case 3: new RestTemplate().getForObject("http://localhost:8082/dt-seleccion-async2/{name}", String.class, "343");
                    break;

            }
                Thread.sleep(new Integer(new Random().nextInt(1000)).longValue());

        }
    }
}
