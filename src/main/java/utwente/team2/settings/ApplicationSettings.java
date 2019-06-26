package utwente.team2.settings;

import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

public class ApplicationSettings {

        public static final String DOMAIN = "http://localhost:8080";
//    public static final String DOMAIN = "http://130.89.82.94:8080";
//    public static final String DOMAIN = "http://farm02.ewi.utwente.nl:7004/";

    private static final String SECRET = "LZ_FzX6IB-sSeEScwB0XjhQaetivpLf91QzsQAAYnVI";
    private static final byte[] SECRET_BYTES = SECRET.getBytes();
    public static final SecretKey APP_KEY = Keys.hmacShaKeyFor(SECRET_BYTES);
}
