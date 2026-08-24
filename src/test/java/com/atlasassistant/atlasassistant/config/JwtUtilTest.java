package com.atlasassistant.atlasassistant.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil();

    @Test
    void generateToken_thenExtractEmail_returnsOriginalEmail() {
        String email = "test@example.com";

        String token = jwtUtil.generateToken(email);
        String extractedEmail = jwtUtil.extractEmail(token);

        assertEquals(email, extractedEmail);
    }

    @Test
    void isTokenValid_withGenuineToken_returnsTrue() {
        String token = jwtUtil.generateToken("test@example.com");

        assertTrue(jwtUtil.isTokenValid(token));
    }

    @Test
    void isTokenValid_withGarbageString_returnsFalse() {
        assertFalse(jwtUtil.isTokenValid("not.a.real.token"));
    }
}