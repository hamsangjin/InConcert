package com.inconcert.global.auth;

import com.inconcert.domain.role.entity.Role;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        log.info("loadUserByUsername: {}", user);
        log.info("loadUserByUsername: {}", user.getUsername());
        if(user == null) {}// 에러 처리
        return new CustomUserDetails(user.getId(), user.getUsername(), user.getPassword(), user.getEmail(), user.getRoles().stream().map(Role::getName).toList());
    }
}