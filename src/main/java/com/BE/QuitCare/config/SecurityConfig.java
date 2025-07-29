package com.BE.QuitCare.config;


import com.BE.QuitCare.service.AuthenticationService;
import com.BE.QuitCare.utils.OAuth2AuthenticationFailureHandler;
import com.BE.QuitCare.utils.OAuth2AuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsUtils;

@Configuration
public class SecurityConfig {
    @Autowired
    Filter filter;

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Autowired
    OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)  throws Exception {
        return http
                .cors().and()//Bật hỗ trợ CORS để chấp nhận các request từ domain khác (dùng khi front-end nằm ở domain khác, ví dụ React chạy localhost:3000 gọi tới Spring Boot ở localhost:8080).
                .csrf(AbstractHttpConfigurer::disable)//Tắt CSRF (Cross Site Request Forgery) vì bạn đang làm API (stateless) nên không cần. CSRF chủ yếu dùng khi frontend và backend cùng domain và sử dụng session.
                .authorizeHttpRequests(//Cấu hình các quy tắc phân quyền:
                        req -> req
                                .requestMatchers("/**")             // Cho phép tất cả các API
                                .permitAll()
                                .requestMatchers(CorsUtils::isPreFlightRequest)
                                .permitAll()                                    // Cho phép preflight request của CORS
                                .anyRequest()
                                .authenticated()                                // Còn lại phải đăng nhập
                )
                .userDetailsService(authenticationService) //Nói cho Spring biết: khi xác thực username/password thì hãy dùng service này để tải thông tin người dùng từ database (UserDetails)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2Login(oauth -> oauth
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                )
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class).build();
    }
}
