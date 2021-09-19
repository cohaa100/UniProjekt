package propraganda.praktikum.logic.util;

import com.google.common.io.Files;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.File;

import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;


public final class JwtHelper {

        private JwtHelper() {}

        //
        static PrivateKey get(final String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
            final byte[] keyBytes = Files.toByteArray(new File(filename));

            final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            final KeyFactory key = KeyFactory.getInstance("RSA");
            return key.generatePrivate(spec);
        }

        public static String createJWT(final String keyLoction,final String githubAppId,final long ttlMillis) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
//The JWT signature algorithm we will be using to sign the token
            final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.RS256;

            final long nowMillis = System.currentTimeMillis();
            final Date now = new Date(nowMillis);

//We will sign our JWT with our private key
            final Key signingKey = get(keyLoction);

//Let's set the JWT Claims
            final JwtBuilder builder = Jwts.builder()
                    .setIssuedAt(now)
                    .setIssuer(githubAppId)
                    .signWith(signingKey, signatureAlgorithm);

//if it has been specified, let's add the expiration
            if (ttlMillis > 0) {
                final long expMillis = nowMillis + ttlMillis;
                final Date exp = new Date(expMillis);
                builder.setExpiration(exp);
            }

//Builds the JWT and serializes it to a compact, URL-safe string
            return builder.compact();
        }

    }
