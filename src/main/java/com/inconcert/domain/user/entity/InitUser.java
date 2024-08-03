package com.inconcert.domain.user.entity;

import com.inconcert.domain.role.entity.Role;
import com.inconcert.domain.role.repository.RoleRepository;
import com.inconcert.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class InitUser implements ApplicationRunner {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${adminPw}")
    private String adminPassword;

    @Transactional
    @Override
    public void run(ApplicationArguments args) throws Exception {
        LocalDate birthLocalDate = LocalDate.now();
        String hashedPassword = passwordEncoder.encode(adminPassword);

        // user와 admin 권한이 없으면 생성
        roleRepository.findByName("ROLE_USER").orElseGet(() -> {
            Role role = new Role("ROLE_USER");
            return roleRepository.save(role);
        });

        Role admin = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> {
            Role role = new Role("ROLE_ADMIN");
            return roleRepository.save(role);
        });

        // 관리자 계정이 없을 때만 생성
        userRepository.findByUsername("admin")
                .orElseGet(() -> {
                    Set<Role> roles = new HashSet<>();
                    roles.add(admin);
                  
                    User userAdmin = User.builder()
                            .birth(birthLocalDate)
                            .username("admin")
                            .name("admin")
                            .email("admin@inconcert.com")
                            .gender(Gender.FEMALE)
                            .mbti(Mbti.ISTJ)
                            .nickname("admin")
                            .password(hashedPassword)
                            .phoneNumber("01012345678")
                            .roles(roles)
                            .build();

                    return userRepository.save(userAdmin);
                });
    }
}