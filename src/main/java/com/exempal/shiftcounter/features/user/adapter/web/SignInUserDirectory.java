package com.exempal.shiftcounter.features.user.adapter.web;

import com.exempal.shiftcounter.features.user.adapter.persistence.*;
import com.exempal.shiftcounter.features.user.domain.UserStatus;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
class SignInUserDirectory {
    private final AppUserRepository users;
    SignInUserDirectory(AppUserRepository users){this.users=users;}
    List<AppUserEntity> findAllByOrderByDisplayNameAsc(){
        return users.findAllByStatusOrderByDisplayNameAsc(UserStatus.ACTIVE);
    }
}
