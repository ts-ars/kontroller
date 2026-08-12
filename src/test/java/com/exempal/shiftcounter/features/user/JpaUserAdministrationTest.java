package com.exempal.shiftcounter.features.user;

import com.exempal.shiftcounter.features.user.adapter.persistence.*;
import com.exempal.shiftcounter.features.user.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.*; import java.util.*;
import static org.assertj.core.api.Assertions.*; import static org.mockito.Mockito.*;

class JpaUserAdministrationTest {
    private final AppUserRepository users=mock(AppUserRepository.class); private final PasswordEncoder encoder=mock(PasswordEncoder.class);
    private final Instant now=Instant.parse("2026-08-12T12:00:00Z"); private final JpaUserAdministration service=new JpaUserAdministration(users,encoder,Clock.fixed(now,ZoneOffset.UTC));
    @Test void createsUserWithEncodedSixDigitPin(){when(encoder.encode("123456")).thenReturn("encoded");service.create(" Operator ","123456",UserRole.USER);verify(encoder).encode("123456");verify(users).save(argThat(u->u.getDisplayName().equals("Operator")&&u.getPinHash().equals("encoded")&&u.getRole()==UserRole.USER));}
    @Test void rejectsNonSixDigitPin(){assertThatThrownBy(()->service.create("Operator","12345",UserRole.USER)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("6 digits");verifyNoInteractions(encoder);}
    @Test void cannotBlockLastActiveOwner(){var owner=account("Owner",UserRole.OWNER,UserStatus.ACTIVE);when(users.findAllForUpdate()).thenReturn(List.of(owner));assertThatThrownBy(()->service.changeStatus(owner.getId(),UserStatus.BLOCKED)).isInstanceOf(IllegalStateException.class).hasMessageContaining("last active OWNER");}
    @Test void canBlockOwnerWhenAnotherActiveOwnerExists(){var first=account("First",UserRole.OWNER,UserStatus.ACTIVE);var second=account("Second",UserRole.OWNER,UserStatus.ACTIVE);when(users.findAllForUpdate()).thenReturn(List.of(first,second));service.changeStatus(first.getId(),UserStatus.BLOCKED);assertThat(first.getStatus()).isEqualTo(UserStatus.BLOCKED);}
    @Test void cannotDemoteLastActiveOwner(){var owner=account("Owner",UserRole.OWNER,UserStatus.ACTIVE);when(users.findAllForUpdate()).thenReturn(List.of(owner));assertThatThrownBy(()->service.updateProfile(owner.getId(),"Owner",UserRole.ADMIN)).isInstanceOf(IllegalStateException.class);}
    private AppUserEntity account(String name,UserRole role,UserStatus status){var user=new AppUserEntity(UUID.randomUUID(),name,"hash",role,now.minusSeconds(10));if(status!=UserStatus.ACTIVE)user.changeStatus(status,now.minusSeconds(5));return user;}
}
