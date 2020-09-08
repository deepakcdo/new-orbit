package com.bezkoder.spring.data.mongodb;

import com.bezkoder.spring.data.mongodb.controller.TutorialController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class SpringBootDataMongodbApplication {
	private static final Log logger = LogFactory.getLog(SpringBootDataMongodbApplication.class);


	public static void main(String[] args) {
		SpringApplication.run(SpringBootDataMongodbApplication.class, args);
		logger.info("SpringBootDataMongodbApplication Started !!!!!");
	}

}
