package com.example.InvestmentGoalManagementPlatform.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * Checks that an ID token handed to us by the browser really was issued by
 * Google for this application. The verifier validates the signature against
 * Google's published keys, plus the issuer, audience and expiry.
 */
@Component
public class GoogleTokenVerifier {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifier(
            @Value("${google.oauth.client-id}") String clientId
    ) {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        )
                .setAudience(List.of(clientId))
                .build();
    }

    public GoogleIdToken.Payload verify(String idToken) {
        GoogleIdToken token;

        try {
            token = verifier.verify(idToken);
        } catch (GeneralSecurityException | IOException exception) {
            // Malformed token, or Google's key endpoint was unreachable.
            throw new BadCredentialsException(
                    "Could not verify Google ID token",
                    exception
            );
        }

        if (token == null) {
            throw new BadCredentialsException("Invalid Google ID token");
        }

        GoogleIdToken.Payload payload = token.getPayload();

        // Without this an attacker could register an unverified Google account
        // using someone else's address and take over their profile here.
        if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
            throw new BadCredentialsException(
                    "Google account email is not verified"
            );
        }

        return payload;
    }
}
