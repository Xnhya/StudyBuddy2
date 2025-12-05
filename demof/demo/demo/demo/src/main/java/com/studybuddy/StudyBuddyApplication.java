package com.studybuddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StudyBuddyApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyBuddyApplication.class, args);
        
        // Mensaje útil en consola para saber que todo cargó bien
        System.out.println("=============================================");
        System.out.println(" STUDY BUDDY SE HA INICIADO CORRECTAMENTE ");
        System.out.println("   Página Web: http://localhost:8080/");
        System.out.println("=============================================");
    }
}