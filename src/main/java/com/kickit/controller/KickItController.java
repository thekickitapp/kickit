package com.kickit.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/main")
public class KickItController {

    @GetMapping("/hello")
    public String helloWorld(){

        return "HelloWorld";
    }
}
