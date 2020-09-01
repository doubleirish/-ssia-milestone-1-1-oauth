package com.manning.ssia.ssiamilestone11oauth.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.security.oauth2.common.util.JacksonJsonParser;
import org.springframework.web.util.UriComponentsBuilder;

import static org.assertj.core.api.Assertions.assertThat;



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class OAuthTest {
    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("When the Oauth password grant is supplied both good client and good user credentials an access token is returned")

    public void givenGoodCredsPasswordGrantTestisAuthorized() throws Exception {

        String url = "http://localhost:" + port + "/oauth/token";
        //e.g ?grant_type=password&username=john&password=12345&scope=read'
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("grant_type", "password")
                .queryParam("username", "john")
                .queryParam("password", "12345")
                .queryParam("scope", "read");

        String jsonStr = this.restTemplate
                .withBasicAuth("client", "secret")
                .postForObject(builder.build().encode().toUri().toASCIIString(), null, String.class);


        JacksonJsonParser jsonParser = new JacksonJsonParser();
        assertThat(jsonParser.parseMap(jsonStr).get("access_token").toString()).isNotEmpty();
        assertThat(jsonParser.parseMap(jsonStr).get("refresh_token").toString()).isNotEmpty();
    }
}
