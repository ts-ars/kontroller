package com.exempal.shiftcounter.core;

import com.exempal.shiftcounter.features.user.adapter.security.LocalPinAuthenticationProvider;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {
    @Bean PasswordEncoder passwordEncoder(){return PasswordEncoderFactories.createDelegatingPasswordEncoder();}
    @Bean SecurityFilterChain applicationSecurity(HttpSecurity http, LocalPinAuthenticationProvider provider) throws Exception {
        return http.authenticationProvider(provider)
          .authorizeHttpRequests(auth -> auth
            .requestMatchers("/css/**","/signin","/actuator/health/**","/actuator/info").permitAll()
            .requestMatchers("/api/signal/**").permitAll()
            .requestMatchers("/page/users","/users","/users/**").hasRole("OWNER")
            .requestMatchers("/page/settings","/settings/**","/api/settings/**").hasAnyRole("ADMIN","OWNER")
            .requestMatchers("/page/shift","/shift/**","/page/comment","/comments/**","/api/comments/**","/page/report","/report/**","/api/report/**").hasAnyRole("USER","ADMIN","OWNER")
            .anyRequest().authenticated())
          .formLogin(form -> form.loginPage("/signin").loginProcessingUrl("/signin").usernameParameter("userId").passwordParameter("pin").defaultSuccessUrl("/page/shift",true).failureUrl("/signin?error").permitAll())
          .logout(logout -> logout.logoutUrl("/signout").logoutSuccessUrl("/signin?signout").invalidateHttpSession(true).deleteCookies("JSESSIONID"))
          .csrf(csrf -> csrf.ignoringRequestMatchers("/api/signal/**"))
          .build();
    }
}
