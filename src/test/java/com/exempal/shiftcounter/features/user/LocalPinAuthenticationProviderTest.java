package com.exempal.shiftcounter.features.user;
import com.exempal.shiftcounter.features.user.adapter.persistence.*;
import com.exempal.shiftcounter.features.user.adapter.security.LocalPinAuthenticationProvider;
import com.exempal.shiftcounter.features.user.domain.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.*; import java.util.*;
import static org.assertj.core.api.Assertions.*; import static org.mockito.Mockito.*;
class LocalPinAuthenticationProviderTest {
 private final AppUserRepository users=mock(AppUserRepository.class); private final PasswordEncoder encoder=mock(PasswordEncoder.class);
 private final Instant now=Instant.parse("2026-08-12T10:00:00Z"); private final LocalPinAuthenticationProvider provider=new LocalPinAuthenticationProvider(users,encoder,Clock.fixed(now,ZoneOffset.UTC));
 @Test void authenticatesWithEncodedPinAndExpandsAdminRole(){var u=user(UserRole.ADMIN);when(users.findById(u.getId())).thenReturn(Optional.of(u));when(encoder.matches("123456",u.getPinHash())).thenReturn(true);var result=provider.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(u.getId().toString(),"123456"));assertThat(result.getName()).isEqualTo("Alex");assertThat(result.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_ADMIN","ROLE_USER");verify(encoder).matches("123456","{bcrypt}hash");}
 @Test void fifthFailureTemporarilyLocksUser(){var u=user(UserRole.USER);when(users.findById(u.getId())).thenReturn(Optional.of(u));when(encoder.matches(any(),any())).thenReturn(false);for(int i=0;i<5;i++)assertThatThrownBy(()->provider.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(u.getId().toString(),"000000"))).isInstanceOf(BadCredentialsException.class);assertThat(u.getLockedUntil()).isEqualTo(now.plus(Duration.ofMinutes(15)));assertThatThrownBy(()->provider.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(u.getId().toString(),"123456"))).isInstanceOf(LockedException.class);}
 private AppUserEntity user(UserRole role){return new AppUserEntity(UUID.randomUUID(),"Alex","{bcrypt}hash",role,now.minusSeconds(60));}
}
