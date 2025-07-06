package labs.externalmicroservice.security;

import labs.externalmicroservice.security.jwt.JwtTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
//@EnableGlobalMethodSecurity(securedEnabled = true)
//@EnableMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
@EnableAsync
public class SecurityConfig {
//    @Autowired
    private final UserDetailsService userDetailsService;

//    @Autowired
    private final AuthEntryPointJwt unauthorizedHandler;

    private final JwtTokenFilter jwtTokenFilter;

    @Autowired
    public SecurityConfig(final UserDetailsService userDetailsService, AuthEntryPointJwt unauthorizedHandler, final JwtTokenFilter jwtTokenFilter) {
        this.userDetailsService = userDetailsService;
        this.unauthorizedHandler = unauthorizedHandler;
        this.jwtTokenFilter = jwtTokenFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
//                .addFilterBefore((request, response, chain) -> {
//                    System.out.println("До фильтров Security: Context={} " +
//                            SecurityContextHolder.getContext().getAuthentication());
//                    chain.doFilter(request, response);
//                }, UsernamePasswordAuthenticationFilter.class)
//                .addFilterBefore(new WebAsyncManagerIntegrationFilter(), SecurityContextPersistenceFilter.class)
                .securityContext(securityContext -> securityContext
                        .securityContextRepository(new DelegatingSecurityContextRepository(
                                new RequestAttributeSecurityContextRepository(), // для асинхронных запросов
                                new HttpSessionSecurityContextRepository() // для обычных
                        ))
                )
                .authorizeHttpRequests(
                        requests -> requests
                                .requestMatchers(new AntPathRequestMatcher("/signin")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/create")).permitAll()
//                                .requestMatchers("/swagger-ui/**").permitAll()
//                                .requestMatchers("/v3/api-docs*/**").permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/v3/**")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**")).permitAll()
//                                .requestMatchers(this::isAsyncRequest)
//                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .anyRequest().authenticated()

                )
//                .requestCache(customizer -> customizer.disable())
//                .httpBasic(Customizer.withDefaults())

                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
//                .anonymous(AbstractHttpConfigurer::disable)
//                .exceptionHandling(customizer -> customizer
//                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .sessionManagement(customizer -> customizer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
//                .addFilterAfter(jwtTokenFilter(), UsernamePasswordAuthenticationFilter.class)
//                .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
                .build();
    }

//    @Bean
//    public JwtTokenFilter jwtTokenFilter() {
//        System.out.println("NEW jwtTokenFilter");
//        return new JwtTokenFilter();
//    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return new ProviderManager(authProvider);
    }

    @Autowired
    public void configure(AuthenticationManagerBuilder builder) {
        builder.eraseCredentials(false);
    }
}
