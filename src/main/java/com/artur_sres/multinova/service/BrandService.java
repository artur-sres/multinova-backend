package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.Brand;
import com.artur_sres.multinova.domain.User;
import com.artur_sres.multinova.dto.brand.BrandRequestDTO;
import com.artur_sres.multinova.dto.brand.BrandResponseDTO;
import com.artur_sres.multinova.exception.*;
import com.artur_sres.multinova.repository.BrandRepository;
import com.artur_sres.multinova.repository.ProductRepository;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BrandService {
    private final BrandRepository brandRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final ProductRepository productRepository;

    public BrandService(BrandRepository brandRepository, AuthenticatedUserProvider authenticatedUserProvider, ProductRepository productRepository) {
        this.brandRepository = brandRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.productRepository =productRepository;
    }

    public BrandResponseDTO searchById(Long id) {
        User currentUser = authenticatedUserProvider.getCurrentUser();

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new BrandNotFoundException("Brand not found."));

        if (!currentUser.getId().equals(brand.getUser().getId())) {
            throw new AccessDeniedException("You do not have permission to access this resource.");
        }

        BrandResponseDTO response = new BrandResponseDTO();
        response.setId(brand.getId());
        response.setName(brand.getName());
        response.setUserId(brand.getUser().getId());

        return response;
    }

    public BrandResponseDTO create(BrandRequestDTO dto){
        User currentUser = authenticatedUserProvider.getCurrentUser();

        if(brandRepository.existsByNameAndUserId(dto.getName(), currentUser.getId())) {
            throw new BrandAlreadyExistsException("A brand with this name already exists.");
        }

        Brand brand = new Brand();
        brand.setName(dto.getName());
        brand.setUser(currentUser);

        Brand saved = brandRepository.save(brand);

        BrandResponseDTO response = new BrandResponseDTO();
        response.setId(saved.getId());
        response.setName(saved.getName());
        response.setUserId(saved.getUser().getId());

        return response;
    }

    public BrandResponseDTO update(Long id, BrandRequestDTO dto) {
        User currentUser = authenticatedUserProvider.getCurrentUser();

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new BrandNotFoundException("Brand not found."));

        if (!currentUser.getId().equals(brand.getUser().getId())) {
            throw new AccessDeniedException("You do not have permission to access this resource.");
        }

        if(brandRepository.existsByNameAndUserIdAndIdNot(dto.getName(), currentUser.getId(), id)) {
            throw new BrandAlreadyExistsException("A brand with this name already exists.");
        }

        brand.setName(dto.getName());

        brandRepository.save(brand);

        BrandResponseDTO response = new BrandResponseDTO();
        response.setId(brand.getId());
        response.setName(brand.getName());
        response.setUserId(brand.getUser().getId());

        return response;
    }

    public void delete(Long id) {
        User currentUser = authenticatedUserProvider.getCurrentUser();

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new BrandNotFoundException("Brand not found."));

        if (!currentUser.getId().equals(brand.getUser().getId())) {
            throw new AccessDeniedException("You do not have permission to access this resource.");
        }

        if (productRepository.existsByBrandId(id)) {
            throw new BrandInUseException("This brand cannot be deleted because it is associated with one or more products.");
        }

        brandRepository.delete(brand);
    }

    public List<BrandResponseDTO> listAll() {
        User currentUser = authenticatedUserProvider.getCurrentUser();

        List<Brand> brands = brandRepository.findByUserId(currentUser.getId());

        return brands.stream()
                .map(brand -> {
                    BrandResponseDTO dto = new BrandResponseDTO();
                    dto.setId(brand.getId());
                    dto.setName(brand.getName());
                    dto.setUserId(brand.getUser().getId());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
