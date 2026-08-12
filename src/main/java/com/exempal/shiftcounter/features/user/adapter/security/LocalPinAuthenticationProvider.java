package com.exempal.shiftcounter.features.user.adapter.security;

import com.exempal.shiftcounter.features.user.adapter.persistence.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;
import java.util.UUID;

@Component
public class LocalPinAuthenticationProvider implements AuthenticationProvider {
    static final int MAX_ATTEMPTS=5;
    static final Duration LOCK_DURATION=Duration.ofMinutes(15);
    private final AppUserRepository users; private final PasswordEncoder encoder; private final Clock clock;
    public LocalPinAuthenticationProvider(AppUserRepository users, PasswordEncoder encoder, Clock clock){this.users=users;this.encoder=encoder;this.clock=clock;}
    @Override @Transactional
    public Authentication authenticate(Authentication input) {
        AppUserEntity user;
        try { user=users.findById(UUID.fromString(input.getName())).orElseThrow(() -> new BadCredentialsException("Invalid credentials")); }
        catch(IllegalArgumentException ex){throw new BadCredentialsException("Invalid credentials");}
        Instant now=clock.instant();
        if(!user.maySignIn(now)) throw new LockedException("User is blocked or temporarily locked");
        if(!encoder.matches(String.valueOf(input.getCredentials()), user.getPinHash())){
            user.signInFailed(now,MAX_ATTEMPTS,LOCK_DURATION); users.save(user);
            throw new BadCredentialsException("Invalid credentials");
        }
        user.signInSucceeded(now); users.save(user);
        List<GrantedAuthority> roles=user.getRole().name().equals("OWNER")
                ? List.of(new SimpleGrantedAuthority("ROLE_OWNER"),new SimpleGrantedAuthority("ROLE_ADMIN"),new SimpleGrantedAuthority("ROLE_USER"))
                : user.getRole().name().equals("ADMIN")
                ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN"),new SimpleGrantedAuthority("ROLE_USER"))
                : List.of(new SimpleGrantedAuthority("ROLE_USER"));
        return UsernamePasswordAuthenticationToken.authenticated(user.getDisplayName(), null, roles);
    }
    @Override public boolean supports(Class<?> type){return UsernamePasswordAuthenticationToken.class.isAssignableFrom(type);}
}
