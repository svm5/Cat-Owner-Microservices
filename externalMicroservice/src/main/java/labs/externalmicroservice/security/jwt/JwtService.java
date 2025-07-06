package labs.externalmicroservice.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import labs.externalmicroservice.service.UserDetailsImpl;
import labs.externalmicroservice.service.UserServiceImpl;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.security.Key;
import java.util.Date;

@Component
public class JwtService {
    @Value("${svm5.app.jwtSecret}")
    private String jwtSecret;

    @Value("${svm5.app.jwtExpirationMs}")
    private long jwtExpirationMs;

    @Value("${svm5.app.jwtCookieName}")
    private String jwtCookie;

    public String getJwtTokenFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, jwtCookie);
        if (cookie != null) {
            System.out.println("getJwtTokenFromCookies " + cookie.toString() + " " + cookie.getValue());
            return cookie.getValue();
        } else {
            System.out.println(
                    "getJwtTokenFromCookies no");
            return null;
        }
    }

    public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal) {
        try {
            System.out.println("Before jwt");
            String jwt = generateTokenFromUsername(userPrincipal.getUsername());
            System.out.println("After jwt " + jwt);
            return ResponseCookie.from(jwtCookie, jwt).path("/").maxAge(60 * 60 * 24).httpOnly(true).build();
        } catch (Exception e) {
            System.out.println("Error generating jwt cookie " + e.getMessage());
            System.out.println(e);
        }
        return null;
    }



    public ResponseCookie getCleanJwtCookie() {
        return ResponseCookie.from(jwtCookie, null).path("/").build();
    }

    public String getUsernameFromJwtToken(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parse(token);
            return true;
        } catch (Exception e) {
            System.out.println("Invalid JWT token " + e.getMessage());
        }

        return false;
    }

    public String generateTokenFromUsername(String username) {
        System.out.println("generateTokenFromUsernamee");
        return Jwts
                .builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
}
