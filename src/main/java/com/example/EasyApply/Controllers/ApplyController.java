package com.example.EasyApply.Controllers;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApplyController {
    @GetMapping("hello")
    public String hello(){
        return "hello";
    }
}
