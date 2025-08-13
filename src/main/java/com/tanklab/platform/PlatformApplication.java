package com.tanklab.platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@ComponentScan("com.tanklab.platform.config")
@MapperScan("com.tanklab.platform.mapper")
public class PlatformApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(PlatformApplication.class, args);
		//inItChainClient();
	}


}
