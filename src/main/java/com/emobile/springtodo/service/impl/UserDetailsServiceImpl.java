package com.emobile.springtodo.service.impl;

import com.emobile.springtodo.entity.ServiceUser;
import com.emobile.springtodo.security.AppUserDetails;
import com.emobile.springtodo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Primary
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserService userService;

    @Override
    public AppUserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        final ServiceUser user = userService.findByUsernameOrEmail(usernameOrEmail);
        return new AppUserDetails(user);
    }
}
