package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.*;
import com.artur_sres.multinova.repository.*;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CatalogImageServiceTests {
    final ProductRepository products = mock(ProductRepository.class);
    final CustomerRepository customers = mock(CustomerRepository.class);
    final AuthenticatedUserProvider users = mock(AuthenticatedUserProvider.class);
    final ImageStorageService storage = mock(ImageStorageService.class);
    final CatalogImageService service = new CatalogImageService(products, customers, users, storage);
    CatalogImageServiceTests() {
        User user = new User(); user.setId(42L);
        when(users.getCurrentUser()).thenReturn(user);
    }

    @Test void forbidsReadingAndReplacingAnotherUsersImages() {
        when(products.findByIdAndUserIdAndDeletedAtIsNull(1L, 42L)).thenReturn(Optional.empty());
        when(customers.findByIdAndUserId(1L, 42L)).thenReturn(Optional.empty());
        for (boolean product : new boolean[]{true, false}) {
            assertThrows(ResponseStatusException.class, () -> service.read(product, 1L));
            assertThrows(ResponseStatusException.class, () -> service.change(product, 1L, new MockMultipartFile("file", new byte[]{1})));
            assertThrows(ResponseStatusException.class, () -> service.change(product, 1L, null));
        }
        verifyNoInteractions(storage);
    }

    @Test void deletesOldFileOnlyAfterCommitAndNewFileOnRollback() {
        for (int status : new int[]{TransactionSynchronization.STATUS_COMMITTED, TransactionSynchronization.STATUS_ROLLED_BACK}) {
            reset(storage);
            Product product = new Product(); product.setImageKey("old.png");
            when(products.findByIdAndUserIdAndDeletedAtIsNull(1L, 42L)).thenReturn(Optional.of(product));
            var file = new MockMultipartFile("file", new byte[]{1});
            when(storage.store(file)).thenReturn("new.png");
            TransactionSynchronizationManager.initSynchronization();
            try {
                assertEquals("/products/1/image?v=new.png", service.change(true, 1L, file));
                verify(storage, never()).delete(any());
                TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCompletion(status));
                verify(storage).delete(status == TransactionSynchronization.STATUS_COMMITTED ? "old.png" : "new.png");
            } finally { TransactionSynchronizationManager.clearSynchronization(); }
        }
    }

    @Test void removesCustomerPhotoAndLegacyUrl() {
        Customer customer = new Customer(); customer.setImageKey("old.png"); customer.setPhotoUrl("https://example.com/photo.png");
        when(customers.findByIdAndUserId(1L, 42L)).thenReturn(Optional.of(customer));
        TransactionSynchronizationManager.initSynchronization();
        try {
            assertNull(service.change(false, 1L, null));
            assertNull(customer.getImageKey());
            assertNull(customer.getPhotoUrl());
            TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_COMMITTED));
            verify(storage).delete("old.png");
        } finally { TransactionSynchronizationManager.clearSynchronization(); }
    }
}
