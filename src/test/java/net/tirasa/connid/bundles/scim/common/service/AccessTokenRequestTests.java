/**
 * Copyright (C) 2018 ConnId (connid-dev@googlegroups.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tirasa.connid.bundles.scim.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import net.tirasa.connid.bundles.scim.common.SCIMConnectorConfiguration;
import net.tirasa.connid.bundles.scim.v2.service.SCIMv2Client;
import org.identityconnectors.common.security.GuardedString;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The access token request sent to the token endpoint (OAuth 2.0, RFC 6749).
 */
class AccessTokenRequestTests {

    private HttpServer server;

    private final AtomicReference<String> requestBody = new AtomicReference<>();

    @BeforeEach
    void startTokenEndpoint() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/token", exchange -> {
            try (InputStream in = exchange.getRequestBody()) {
                requestBody.set(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            }
            byte[] response = "{\"access_token\":\"abc123\",\"token_type\":\"Bearer\"}"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(response);
            }
        });
        server.start();
    }

    @AfterEach
    void stopTokenEndpoint() {
        server.stop(0);
    }

    private SCIMConnectorConfiguration configuration() {
        SCIMConnectorConfiguration config = new SCIMConnectorConfiguration();
        config.setBaseAddress("http://127.0.0.1:" + server.getAddress().getPort() + "/scim/v2/");
        config.setAccessTokenBaseAddress("http://127.0.0.1:" + server.getAddress().getPort() + "/token");
        config.setClientId("my-client");
        config.setClientSecret("s3cr&t=+");
        return config;
    }

    @Test
    void clientCredentialsGrantWhenNoUsername() {
        SCIMConnectorConfiguration config = configuration();

        String token = new SCIMv2Client(config).getBearerToken(true);

        assertEquals("abc123", token);
        String body = requestBody.get();
        assertTrue(body.contains("grant_type=client_credentials"), body);
        assertTrue(body.contains("client_id=my-client"), body);
        // form-encoded, so a secret with '&', '=' or '+' reaches the server intact
        assertTrue(body.contains("client_secret=s3cr%26t%3D%2B"), body);
    }

    @Test
    void usernamePasswordRequestUnchanged() {
        SCIMConnectorConfiguration config = configuration();
        config.setClientSecret("secret");
        config.setUsername("user");
        config.setPassword(new GuardedString("pass".toCharArray()));

        String token = new SCIMv2Client(config).getBearerToken(true);

        assertEquals("abc123", token);
        String body = requestBody.get();
        assertEquals("&client_id=my-client&client_secret=secret&username=user&password=pass", body);
        assertFalse(body.contains("grant_type"), body);
    }
}
