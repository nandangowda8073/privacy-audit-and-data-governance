package com.piiscan.auth.config;

import com.piiscan.auth.entity.Role;
import com.piiscan.auth.entity.User;
import com.piiscan.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            log.info("Seeding initial platform users and roles...");

            User superAdmin = User.builder()
                    .fullName("Enterprise Super Admin")
                    .username("superadmin")
                    .email("admin@piiscan.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ROLE_SUPER_ADMIN)
                    .active(true)
                    .build();

            User securityOfficer = User.builder()
                    .fullName("Alex Rivera (Security Officer)")
                    .username("secops")
                    .email("secops@piiscan.com")
                    .password(passwordEncoder.encode("SecOps@123"))
                    .role(Role.ROLE_SECURITY_OFFICER)
                    .active(true)
                    .build();

            User auditor = User.builder()
                    .fullName("Sarah Chen (Compliance Auditor)")
                    .username("auditor")
                    .email("auditor@piiscan.com")
                    .password(passwordEncoder.encode("Auditor@123"))
                    .role(Role.ROLE_AUDITOR)
                    .active(true)
                    .build();

            userRepository.save(superAdmin);
            userRepository.save(securityOfficer);
            userRepository.save(auditor);

            log.info("Database initialized with 3 default role accounts: Super Admin, Security Officer, Auditor.");
        }
    }
}
