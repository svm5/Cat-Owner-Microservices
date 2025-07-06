package labs.externalmicroservice.security.jwt;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
//@Async("abcTaskExecutor")
public class JwtTokenFilter extends OncePerRequestFilter {
//    @Autowired
    private final UserDetailsService userDetailsService;

//    @Autowired
    private final JwtService jwtService;

    public JwtTokenFilter(final UserDetailsService userDetailsService, final JwtService jwtService) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.println("In doFilterInternal");
        try {
//            if (request.getDispatcherType() == DispatcherType.ASYNC) {
//                filterChain.doFilter(request, response);
//                return;
//            }

            String jwt = jwtService.getJwtTokenFromCookies(request);
            if (jwt != null && jwtService.validateJwtToken(jwt)) {
                String username = jwtService.getUsernameFromJwtToken(jwt);
                System.out.println("In doFilterInternal username: " + username);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("In doFilterInternal auth: " + SecurityContextHolder.getContext().getAuthentication());
            }
        } catch (Exception e) {
            System.out.println("doFilterInternal exception" + e);
        }
//        finally {
        filterChain.doFilter(request, response);
//            SecurityContextHolder.clearContext();
//        }
    }
}
