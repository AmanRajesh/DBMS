// src/main/java/org/example/libraryservice/HomeController.java
package org.example.libraryservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Welcome to the Library Service!";
    }
}