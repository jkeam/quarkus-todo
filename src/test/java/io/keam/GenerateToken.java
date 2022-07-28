package io.keam;

import io.smallrye.jwt.build.Jwt;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Token generator for testing.
 */
public class GenerateToken {

    /**
     * Generate JWT token
     */
    public static void main(String[] args) {
        String username = "jpkeam";
        //String username = "jdoe@quarkus.io";
        String token =
                Jwt.issuer("https://example.com/issuer")
                        .subject(username)
                        .upn(username)
                        .groups(new HashSet<>(Arrays.asList("User")))
//                        .claim(Claims.birthdate.name(), "2001-07-13")
                        .sign();
        System.out.printf("username: %s%n", username);
        System.out.println(token);
    }
}