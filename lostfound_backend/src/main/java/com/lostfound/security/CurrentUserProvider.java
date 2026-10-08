package com.lostfound.security;

import com.lostfound.entity.User;
import com.lostfound.exception.ResourceNotFoundException;
import com.lostfound.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// Small helper so controllers can go from the authenticated UserPrincipal
// (which only holds id/email/authorities) to the full User entity when needed.
@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public User resolve(UserPrincipal principal) {
        if (principal == null) {
            throw new ResourceNotFoundException("No authenticated user on this request");
        }
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user no longer exists"));
    }
}
