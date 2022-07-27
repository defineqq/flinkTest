package com.joyme.test.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class hello {

    @ResponseBody
    @RequestMapping("/hello")
    public String hello(){
        return "hello world";

    }
}
