package com.etpa.energy.web;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.*;

@Hidden
@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello() { return "OK"; }
}
