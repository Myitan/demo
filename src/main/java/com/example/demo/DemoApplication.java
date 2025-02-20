package com.example.demo;

import org.opencv.core.Core;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {
		static {
			try {
				nu.pattern.OpenCV.loadLocally();
				System.out.println("OpenCV loaded successfully!");
				System.out.println("Version: " + Core.VERSION);
			} catch (UnsatisfiedLinkError e) {
				System.err.println("OpenCV load failed: " + e.getMessage());
				System.exit(1);
			}
		}

		public static void main(String[] args) {
			SpringApplication.run(DemoApplication.class, args);
		}
}