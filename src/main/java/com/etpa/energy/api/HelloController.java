package com.etpa.energy.api;
import org.springframework.web.bind.annotation.*;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello() { return "OK"; }
}
