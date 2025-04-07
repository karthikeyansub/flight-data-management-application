package com.flight.data.management.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;

public class AbstractWireMockTest extends AbstractIntegrationTest {

    private static WireMockServer wireMockServer;

    @BeforeAll
    static void init() {
        wireMockServer = new WireMockServer(new WireMockConfiguration().port(8081));
        wireMockServer.start();
        configureFor("localhost", 8081);
    }

    @AfterAll
    static void tearDown() {
        wireMockServer.stop();
    }
}