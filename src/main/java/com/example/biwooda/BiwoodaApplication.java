package com.example.biwooda;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;


@SpringBootApplication
public class BiwoodaApplication {

	public static void main(String[] args) {
		SpringApplication.run(BiwoodaApplication.class, args);
	}

}
