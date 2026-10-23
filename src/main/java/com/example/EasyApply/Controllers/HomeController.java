package com.example.EasyApply.Controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Value("${easyapply.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @GetMapping("/")
    public String home() {
        return "redirect:" + frontendUrl.replaceAll("/$", "") + "/";
    }
}
