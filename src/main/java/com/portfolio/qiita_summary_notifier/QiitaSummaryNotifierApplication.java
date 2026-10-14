package com.portfolio.qiita_summary_notifier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableResilientMethods // retryableを有効化するため
@EnableScheduling // スケジュールを使うため
public class QiitaSummaryNotifierApplication {

	public static void main(String[] args) {
		SpringApplication.run(QiitaSummaryNotifierApplication.class, args);
	}

}
