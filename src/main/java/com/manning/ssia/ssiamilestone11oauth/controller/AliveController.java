package com.manning.ssia.ssiamilestone11oauth.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = "/alive")
public class AliveController {
    @GetMapping
    public String alive() {   return "healthy!";   }
}

