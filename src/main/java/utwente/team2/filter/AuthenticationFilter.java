package utwente.team2.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import utwente.team2.resource.Login;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    @Context
    HttpServletRequest servletRequest;

    @Context
    HttpServletResponse servletResponse;

    @Context
    HttpHeaders headers;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        System.out.println("Filter started...");
        System.out.println("Resource requested: " + requestContext.getUriInfo().getPath());

        Cookie jwsCookie = headers.getCookies().get("token");

        if (jwsCookie == null) {
            System.out.println("No cookie with token provided. Redirecting to login.");
            forwardUnauthorized("not_authorized");
            abortWithUnauthorized(requestContext);
            return;
        }

        String jws = jwsCookie.getValue();

        try {
            // Validate the token
            validateToken(jws);

            final SecurityContext currentSecurityContext = requestContext.getSecurityContext();
            requestContext.setSecurityContext(new SecurityContext() {

                @Override
                public Principal getUserPrincipal() {
                    return () -> Login.getTokenClaims(Login.getCookie(servletRequest, "token").getValue()).getBody().getSubject();
                }

                @Override
                public boolean isUserInRole(String role) {
                    return true;
                }

                @Override
                public boolean isSecure() {
                    return currentSecurityContext.isSecure();
                }

                @Override
                public String getAuthenticationScheme() {
                    return null;
                }
            });

            System.out.println("Filter passed.");

        } catch (JwtException e) {
            System.out.println("Filter blocked: token is invalid or expired.");
            forwardUnauthorized("token_expired");
            abortWithUnauthorized(requestContext);
            return;
        }
    }

    private void validateToken(String token) throws JwtException {
        Jws<Claims> jws = Jwts.parser().setSigningKey(Login.KEY).parseClaimsJws(token);
    }

    private void forwardUnauthorized(String error) {
        try {
            // TODO if application makes a request to API, then json with error should be sent

            servletResponse.setStatus(401);
            servletResponse.sendRedirect("/runner/login/?error=" + error);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void abortWithUnauthorized(ContainerRequestContext requestContext) {
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED).build());
    }
}