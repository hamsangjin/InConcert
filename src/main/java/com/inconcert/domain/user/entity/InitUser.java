package com.inconcert.domain.user.entity;

import com.inconcert.domain.role.entity.Role;
import com.inconcert.domain.role.repository.RoleRepository;
import com.inconcert.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static com.inconcert.domain.user.entity.Gender.MALE;
import static com.inconcert.domain.user.entity.Mbti.INTJ;

@Component
@RequiredArgsConstructor
public class InitUser implements ApplicationRunner {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private String adminPassword = "1234";

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

        Set<Role> roles = new HashSet<>();
        roles.add(admin);

        User userAdmin = User.builder()
                .birth(birthLocalDate)
                .username("admin")
                .name("admin")
                .email("admin@blog.com")
                .gender(MALE)
                .mbti(INTJ)
                .nickname("adminNick")
                .password(hashedPassword)
                .phoneNumber("123-456-7890")
                .roles(roles)
                .build();

        userRepository.save(userAdmin);
    }
}