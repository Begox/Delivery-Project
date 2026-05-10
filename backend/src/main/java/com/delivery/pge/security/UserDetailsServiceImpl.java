package com.delivery.pge.security;

import com.delivery.pge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // identifier can be email or CPF
        return userRepository.findByCpfOrEmail(identifier, identifier)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuário não encontrado com identificador: " + identifier
                ));
    }
}
