package com.java.TMDTPicnic.service;

import com.java.TMDTPicnic.dto.request.CategoryRequest;
import com.java.TMDTPicnic.dto.response.CategoryResponse;
import com.java.TMDTPicnic.entity.Category;
import com.java.TMDTPicnic.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryResponse createCategory(CategoryRequest request) {
        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .parent(parent)
                .build();

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();

        // Tạo map lưu tất cả category theo ID
        Map<Long, CategoryResponse> map = new HashMap<>();
        List<CategoryResponse> roots = new ArrayList<>();

        // Bước 1: Map tất cả Category sang CategoryResponse (chưa có children)
        for (Category c : categories) {
            CategoryResponse dto = CategoryResponse.builder()
                    .id(c.getId())
                    .name(c.getName())
                    .description(c.getDescription())
                    .parentId(c.getParent() != null ? c.getParent().getId() : null)
                    .children(new ArrayList<>()) // thêm children rỗng
                    .build();
            map.put(c.getId(), dto);
        }

        // Bước 2: Xây dựng cấu trúc cây
        for (Category c : categories) {
            if (c.getParent() != null) {
                CategoryResponse parent = map.get(c.getParent().getId());
                if (parent != null) {
                    parent.getChildren().add(map.get(c.getId()));
                }
            } else {
                roots.add(map.get(c.getId()));
            }
        }

        // Trả về danh mục cha (các root categories)
        return roots;
    }

    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return mapToResponse(category);
    }

    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setParent(parent);

        Category updated = categoryRepository.save(category);
        return mapToResponse(updated);
    }

    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found");
        }
        categoryRepository.deleteById(id);
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .build();
    }
}
