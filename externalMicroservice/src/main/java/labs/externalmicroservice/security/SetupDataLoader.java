package labs.externalmicroservice.security;

import jakarta.transaction.Transactional;
import labs.externalmicroservice.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {
    private boolean alreadySetup = false;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetup || userRepository.findByUsername("admin") != null)
            return;

        Role adminRole = roleRepository.findByName(RoleTypes.ROLE_ADMIN);
        User user = new User("admin", encoder.encode("p@ssw0rd"));
        user.getRoles().add(adminRole);
        userRepository.save(user);

        alreadySetup = true;
    }
}
