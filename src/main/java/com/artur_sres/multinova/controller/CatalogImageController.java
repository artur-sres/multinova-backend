package com.artur_sres.multinova.controller;

import com.artur_sres.multinova.service.CatalogImageService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Collections;
import java.util.Map;

@RestController
public class CatalogImageController {
    private final CatalogImageService images;
    public CatalogImageController(CatalogImageService images) { this.images = images; }

    @PutMapping(value = "/products/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> product(@PathVariable Long id, @RequestParam MultipartFile file) {
        return Collections.singletonMap("imageUrl", images.change(true, id, file));
    }
    @PutMapping(value = "/customers/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> customer(@PathVariable Long id, @RequestParam MultipartFile file) {
        return Collections.singletonMap("photoUrl", images.change(false, id, file));
    }
    @DeleteMapping("/products/{id}/image")
    public Map<String, String> deleteProduct(@PathVariable Long id) {
        return Collections.singletonMap("imageUrl", images.change(true, id, null));
    }
    @DeleteMapping("/customers/{id}/photo")
    public Map<String, String> deleteCustomer(@PathVariable Long id) {
        return Collections.singletonMap("photoUrl", images.change(false, id, null));
    }
    @GetMapping("/products/{id}/image")
    public ResponseEntity<byte[]> readProduct(@PathVariable Long id) { return image(images.read(true, id)); }
    @GetMapping("/customers/{id}/photo")
    public ResponseEntity<byte[]> readCustomer(@PathVariable Long id) { return image(images.read(false, id)); }
    private ResponseEntity<byte[]> image(byte[] bytes) {
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).cacheControl(CacheControl.noStore()).body(bytes);
    }
}
