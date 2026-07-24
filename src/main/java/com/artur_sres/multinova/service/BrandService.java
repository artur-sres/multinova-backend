package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.Brand;
import com.artur_sres.multinova.domain.User;
import com.artur_sres.multinova.dto.brand.BrandRequestDTO;
import com.artur_sres.multinova.dto.brand.BrandResponseDTO;
import com.artur_sres.multinova.exception.*;
import com.artur_sres.multinova.mapper.BrandMapper;
import com.artur_sres.multinova.repository.BrandRepository;
import com.artur_sres.multinova.repository.ProductRepository;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BrandService {
    private final BrandRepository brandRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final ProductRepository productRepository;
    private final BrandMapper brandMapper;

    public BrandService(BrandRepository brandRepository, AuthenticatedUserProvider authenticatedUserProvider, ProductRepository productRepository, BrandMapper brandMapper) {
        this.brandRepository = brandRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.productRepository =productRepository;
        this.brandMapper = brandMapper;
    }

    private Brand getAuthorizedBrand(Long id, Long userId) {
        return brandRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BrandNotFoundException("Brand not found."));
    }

    public BrandResponseDTO searchById(Long id) {
        User currentUser = authenticatedUserProvider.getCurrentUser();
        Brand brand = getAuthorizedBrand(id, currentUser.getId());
        return brandMapper.toResponseDTO(brand);
    }

    @Transactional
    public BrandResponseDTO create(BrandRequestDTO dto){
        User currentUser = authenticatedUserProvider.getCurrentUser();

        if(brandRepository.existsByNameAndUserId(dto.getName(), currentUser.getId())) {
            throw new BrandAlreadyExistsException("A brand with this name already exists.");
        }

        Brand brand = new Brand();
        brand.setName(dto.getName());
        brand.setUser(currentUser);

        brandRepository.save(brand);
        return brandMapper.toResponseDTO(brand);
    }

    @Transactional
    public BrandResponseDTO update(Long id, BrandRequestDTO dto) {
        User currentUser = authenticatedUserProvider.getCurrentUser();

        Brand brand = getAuthorizedBrand(id, currentUser.getId());

        if (!currentUser.getId().equals(brand.getUser().getId())) {
            throw new AccessDeniedException("You do not have permission to access this resource.");
        }

        if(brandRepository.existsByNameAndUserIdAndIdNot(dto.getName(), currentUser.getId(), id)) {
            throw new BrandAlreadyExistsException("A brand with this name already exists.");
        }

        brand.setName(dto.getName());

        return brandMapper.toResponseDTO(brand);
    }

    @Transactional
    public void delete(Long id) {
        User currentUser = authenticatedUserProvider.getCurrentUser();

        Brand brand = getAuthorizedBrand(id, currentUser.getId());

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
                .map(brandMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}
