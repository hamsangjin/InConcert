package com.inconcert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
//@EnableScheduling // 스케줄링 관련 (현재는 로컬에서 작업하므로 주석 처리)
public class InconcertApplication {

	public static void main(String[] args) {
		SpringApplication.run(InconcertApplication.class, args);
	}
}