package com.exempal.shiftcounter.features.user.adapter.security;

import com.exempal.shiftcounter.features.user.adapter.persistence.*;
import com.exempal.shiftcounter.features.user.domain.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.util.UUID;

@Component
public class OwnerBootstrap implements ApplicationRunner {
    private final AppUserRepository users; private final PasswordEncoder encoder; private final Clock clock;
    private final String name; private final String pin;
    public OwnerBootstrap(AppUserRepository users, PasswordEncoder encoder, Clock clock,
      @Value("${security.bootstrap-owner.name:}") String name,
      @Value("${security.bootstrap-owner.pin:}") String pin){this.users=users;this.encoder=encoder;this.clock=clock;this.name=name;this.pin=pin;}
    @Override @Transactional public void run(ApplicationArguments ignored){
        if(users.existsByRole(UserRole.OWNER)) return;
        if(name.isBlank() && pin.isBlank()) return;
        if(name.isBlank() || !pin.matches("\\d{6}")) throw new IllegalStateException("BOOTSTRAP_OWNER_NAME and six-digit BOOTSTRAP_OWNER_PIN must be supplied together");
        if(users.existsByDisplayNameIgnoreCase(name.trim())) throw new IllegalStateException("Bootstrap owner name already exists");
        users.save(new AppUserEntity(UUID.randomUUID(),name.trim(),encoder.encode(pin),UserRole.OWNER,clock.instant()));
    }
}
