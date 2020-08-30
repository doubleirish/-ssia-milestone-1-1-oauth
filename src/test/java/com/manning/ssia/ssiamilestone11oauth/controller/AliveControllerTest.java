package com.manning.ssia.ssiamilestone11oauth.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Slf4j
public class AliveControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void alive_authorized() throws Exception {
        String jsonStr = this.restTemplate
                .withBasicAuth("john", "12345")
                .getForObject("http://localhost:" + port + "/alive", String.class);
        assertThat(jsonStr).contains("healthy!");
    }

    @Test
    public void alive_unautorized() throws Exception {
        ResponseEntity<String> response = this.restTemplate
                .withBasicAuth("john", "badpass")
                .getForEntity("http://localhost:" + port + "/alive", String.class);
        log.info("/alive with bad creds response Code: " + response.getStatusCode());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED); // Expected :200, Actual   :401
    }
}