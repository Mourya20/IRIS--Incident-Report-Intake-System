package com.iris.backend.service;

import com.iris.backend.entity.AppUser;
import com.iris.backend.repository.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class CurrentUserService {

    private final AppUserRepository appUserRepository;

    public CurrentUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public AppUser requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new NoSuchElementException("Authenticated user not found");
        }
        return appUserRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new NoSuchElementException("Authenticated user not found"));
    }
}
