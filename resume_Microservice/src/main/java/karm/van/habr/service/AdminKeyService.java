package karm.van.habr.service;

import karm.van.habr.entity.AdminKey;
import karm.van.habr.repo.AdminKeyRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminKeyService {
    private final AdminKeyRepo adminKeyRepo;

    @Transactional
    public String getAdminRegKey(){
        Optional<AdminKey> adminKey = adminKeyRepo.findById(1L);
        return adminKey.map(key -> key.getAdmin_registration_key().toString()).orElseThrow();
    }
}