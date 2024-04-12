package com.project.nearby;

import com.project.nearby.services.EateriesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NearbyApplication {


	public static void main(String[] args) {
		SpringApplication.run(NearbyApplication.class, args);

		EateriesService es= new EateriesService();
		es.getEateries();
	}


}
