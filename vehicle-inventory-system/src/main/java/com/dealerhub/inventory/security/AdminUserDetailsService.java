package com.dealerhub.inventory.security;

import com.dealerhub.inventory.domain.AdminUser;
import com.dealerhub.inventory.repository.AdminUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;

    public AdminUserDetailsService(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminUser admin = adminUserRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("No admin user '" + username + "'"));

        return new User(
                admin.getUsername(),
                admin.getPasswordHash(),
                admin.isEnabled(),
                true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }
}
