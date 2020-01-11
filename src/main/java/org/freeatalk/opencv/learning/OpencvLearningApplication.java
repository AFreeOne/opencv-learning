package org.freeatalk.opencv.learning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OpencvLearningApplication {
    
    
    private static final Logger log = LoggerFactory.getLogger(OpencvLearningApplication.class);


	public static void main(String[] args) {
		SpringApplication.run(OpencvLearningApplication.class, args);
		log.info("================================ starter completed ==========================================");
	}

}
