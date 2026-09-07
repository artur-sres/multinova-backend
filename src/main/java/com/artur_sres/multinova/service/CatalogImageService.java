package com.artur_sres.multinova.service;

import com.artur_sres.multinova.repository.*;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.function.Consumer;

@Service
@Transactional
public class CatalogImageService {
    private final ProductRepository products;
    private final CustomerRepository customers;
    private final AuthenticatedUserProvider users;
    private final ImageStorageService storage;
    public CatalogImageService(ProductRepository products, CustomerRepository customers,
            AuthenticatedUserProvider users, ImageStorageService storage) {
        this.products = products; this.customers = customers; this.users = users; this.storage = storage;
    }

    public String change(boolean product, Long id, MultipartFile file) {
        Long userId = users.getCurrentUser().getId();
        String oldKey;
        Consumer<String> setter;
        if (product) {
            var entity = products.findByIdAndUserIdAndDeletedAtIsNull(id, userId).orElseThrow(this::missing);
            oldKey = entity.getImageKey();
            setter = key -> { entity.setImageKey(key); entity.setImageUrl(null); };
        } else {
            var entity = customers.findByIdAndUserId(id, userId).orElseThrow(this::missing);
            oldKey = entity.getImageKey();
            setter = key -> { entity.setImageKey(key); entity.setPhotoUrl(null); };
        }
        String newKey = file == null ? null : storage.store(file);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) {
                storage.delete(status == STATUS_COMMITTED ? oldKey : newKey);
            }
        });
        setter.accept(newKey);
        return newKey == null ? null : (product ? "/products/" : "/customers/") + id
                + (product ? "/image?v=" : "/photo?v=") + newKey;
    }

    @Transactional(readOnly = true)
    public byte[] read(boolean product, Long id) {
        Long userId = users.getCurrentUser().getId();
        String key = product
                ? products.findByIdAndUserIdAndDeletedAtIsNull(id, userId).orElseThrow(this::missing).getImageKey()
                : customers.findByIdAndUserId(id, userId).orElseThrow(this::missing).getImageKey();
        return storage.read(key);
    }
    private ResponseStatusException missing() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Cadastro não encontrado.");
    }
}
