package com.exempal.shiftcounter.features.user.adapter.persistence;

import com.exempal.shiftcounter.features.user.application.UserAdministration;
import com.exempal.shiftcounter.features.user.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.util.*;

@Service
public class JpaUserAdministration implements UserAdministration {
    private final AppUserRepository users; private final PasswordEncoder encoder; private final Clock clock;
    public JpaUserAdministration(AppUserRepository users, PasswordEncoder encoder, Clock clock){this.users=users;this.encoder=encoder;this.clock=clock;}
    @Override @Transactional(readOnly=true) public List<UserView> list(){return users.findAllByOrderByDisplayNameAsc().stream().map(JpaUserAdministration::view).toList();}
    @Override @Transactional public void create(String displayName,String pin,UserRole role){
        String name=validName(displayName); validPin(pin); Objects.requireNonNull(role,"Role is required");
        if(users.existsByDisplayNameIgnoreCase(name)) throw new IllegalArgumentException("Display name already exists");
        users.save(new AppUserEntity(UUID.randomUUID(),name,encoder.encode(pin),role,clock.instant()));
    }
    @Override @Transactional public void updateProfile(UUID id,String displayName,UserRole role){
        String name=validName(displayName); Objects.requireNonNull(role,"Role is required"); List<AppUserEntity> locked=users.findAllForUpdate();
        AppUserEntity user=require(locked,id); if(users.existsByDisplayNameIgnoreCaseAndIdNot(name,id)) throw new IllegalArgumentException("Display name already exists");
        if(user.getRole()==UserRole.OWNER && role!=UserRole.OWNER) requireAnotherActiveOwner(locked,user);
        user.updateProfile(name,role,clock.instant());
    }
    @Override @Transactional public void changePin(UUID id,String pin){validPin(pin);AppUserEntity user=users.findById(id).orElseThrow(()->new IllegalArgumentException("User not found"));user.changePin(encoder.encode(pin),clock.instant());}
    @Override @Transactional public void changeStatus(UUID id,UserStatus status){
        Objects.requireNonNull(status,"Status is required");List<AppUserEntity> locked=users.findAllForUpdate();AppUserEntity user=require(locked,id);
        if(user.getRole()==UserRole.OWNER && status==UserStatus.BLOCKED) requireAnotherActiveOwner(locked,user);
        user.changeStatus(status,clock.instant());
    }
    private static void requireAnotherActiveOwner(List<AppUserEntity> all,AppUserEntity changed){if(all.stream().noneMatch(u->!u.getId().equals(changed.getId())&&u.getRole()==UserRole.OWNER&&u.getStatus()==UserStatus.ACTIVE))throw new IllegalStateException("The last active OWNER cannot be blocked or demoted");}
    private static AppUserEntity require(List<AppUserEntity> all,UUID id){return all.stream().filter(u->u.getId().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("User not found"));}
    private static String validName(String value){String name=value==null?"":value.trim();if(name.isEmpty()||name.length()>120)throw new IllegalArgumentException("Display name must contain 1 to 120 characters");return name;}
    private static void validPin(String pin){if(pin==null||!pin.matches("\\d{6}"))throw new IllegalArgumentException("PIN must contain exactly 6 digits");}
    private static UserView view(AppUserEntity u){return new UserView(u.getId(),u.getDisplayName(),u.getRole(),u.getStatus(),u.getFailedAttempts(),u.getLockedUntil(),u.getVersion());}
}
