package com.exam.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@EnableR2dbcAuditing
public class LibraryApplication {

	public static void main(String[] args) {
		SpringApplication.run(LibraryApplication.class, args);

	}
	
}



