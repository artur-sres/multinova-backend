package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.Category;
import com.artur_sres.multinova.domain.User;
import com.artur_sres.multinova.dto.category.CategoryRequestDTO;
import com.artur_sres.multinova.dto.category.CategoryResponseDTO;
import com.artur_sres.multinova.exception.AccessDeniedException;
import com.artur_sres.multinova.exception.CategoryAlreadyExistsException;
import com.artur_sres.multinova.exception.CategoryInUseException;
import com.artur_sres.multinova.exception.CategoryNotFoundException;
import com.artur_sres.multinova.repository.CategoryRepository;
import com.artur_sres.multinova.repository.ProductRepository;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, AuthenticatedUserProvider authenticatedUserProvider, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.productRepository =productRepository;
    }

    private CategoryResponseDTO toResponseDTO(Category category) {
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setUserId(category.getUser().getId());
        return dto;
    }

    public CategoryResponseDTO searchById(Long id) {
        User currentUser = authenticatedUserProvider.getCurrentUser();

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("category not found."));

        if (!currentUser.getId().equals(category.getUser().getId())) {
            throw new AccessDeniedException("You do not have permission to access this resource.");
        }

        return toResponseDTO(category);
    }

    public CategoryResponseDTO create(CategoryRequestDTO dto){
        User currentUser = authenticatedUserProvider.getCurrentUser();

        if(categoryRepository.existsByNameAndUserId(dto.getName(), currentUser.getId())) {
            throw new CategoryAlreadyExistsException("A category with this name already exists.");
        }

        Category category = new Category();
        category.setName(dto.getName());
        category.setUser(currentUser);

        categoryRepository.save(category);
        return toResponseDTO(category);
    }

    public CategoryResponseDTO update(Long id, CategoryRequestDTO dto) {
        User currentUser = authenticatedUserProvider.getCurrentUser();

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("category not found."));

        if (!currentUser.getId().equals(category.getUser().getId())) {
            throw new AccessDeniedException("You do not have permission to access this resource.");
        }

        if(categoryRepository.existsByNameAndUserIdAndIdNot(dto.getName(), currentUser.getId(), id)) {
            throw new CategoryAlreadyExistsException("A category with this name already exists.");
        }

        category.setName(dto.getName());

        categoryRepository.save(category);
        return toResponseDTO(category);
    }

    public void delete(Long id) {
        User currentUser = authenticatedUserProvider.getCurrentUser();

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("category not found."));

        if (!currentUser.getId().equals(category.getUser().getId())) {
            throw new AccessDeniedException("You do not have permission to access this resource.");
        }

        if (productRepository.existsByCategoryId(id)) {
            throw new CategoryInUseException("This category cannot be deleted because it is associated with one or more products.");
        }

        categoryRepository.delete(category);
    }

    public List<CategoryResponseDTO> listAll() {
        User currentUser = authenticatedUserProvider.getCurrentUser();
        List<Category> categories = categoryRepository.findByUserId(currentUser.getId());

        return categories.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}
