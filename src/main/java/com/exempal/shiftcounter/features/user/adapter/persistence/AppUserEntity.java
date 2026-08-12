package com.exempal.shiftcounter.features.user.adapter.persistence;

import com.exempal.shiftcounter.features.user.domain.UserRole;
import com.exempal.shiftcounter.features.user.domain.UserStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_user")
public class AppUserEntity {
    @Id private UUID id;
    @Column(name="display_name", nullable=false, unique=true) private String displayName;
    @Column(name="pin_hash", nullable=false) private String pinHash;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private UserRole role;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private UserStatus status;
    @Column(name="failed_attempts", nullable=false) private int failedAttempts;
    @Column(name="locked_until") private Instant lockedUntil;
    @Column(name="created_at", nullable=false) private Instant createdAt;
    @Column(name="updated_at", nullable=false) private Instant updatedAt;

    protected AppUserEntity() {}
    public AppUserEntity(UUID id, String displayName, String pinHash, UserRole role, Instant now) {
        this.id=id; this.displayName=displayName; this.pinHash=pinHash; this.role=role;
        this.status=UserStatus.ACTIVE; this.createdAt=now; this.updatedAt=now;
    }
    public UUID getId(){return id;} public String getDisplayName(){return displayName;}
    public String getPinHash(){return pinHash;} public UserRole getRole(){return role;}
    public UserStatus getStatus(){return status;} public int getFailedAttempts(){return failedAttempts;}
    public Instant getLockedUntil(){return lockedUntil;}
    public boolean maySignIn(Instant now){return status==UserStatus.ACTIVE && (lockedUntil==null || !lockedUntil.isAfter(now));}
    public void signInSucceeded(Instant now){failedAttempts=0; lockedUntil=null; updatedAt=now;}
    public void signInFailed(Instant now, int maxAttempts, java.time.Duration lockDuration){
        failedAttempts++;
        if(failedAttempts>=maxAttempts){lockedUntil=now.plus(lockDuration); failedAttempts=0;}
        updatedAt=now;
    }
}
