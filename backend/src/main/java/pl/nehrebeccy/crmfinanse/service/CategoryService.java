package pl.nehrebeccy.crmfinanse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.nehrebeccy.crmfinanse.dto.CategoryDTO;
import pl.nehrebeccy.crmfinanse.model.Category;
import pl.nehrebeccy.crmfinanse.repository.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<CategoryDTO> getCategoriesByType(String type) {
        Category.CategoryType categoryType = Category.CategoryType.valueOf(type.toUpperCase());
        return categoryRepository.findByType(categoryType).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CategoryDTO getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Kategoria nie znaleziona: " + id));
    }

    public CategoryDTO createCategory(CategoryDTO dto) {
        Category category = toEntity(dto);
        category = categoryRepository.save(category);
        return toDTO(category);
    }

    public CategoryDTO updateCategory(Long id, CategoryDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kategoria nie znaleziona: " + id));

        category.setName(dto.getName());
        category.setColor(dto.getColor());
        category.setIcon(dto.getIcon());
        category.setType(Category.CategoryType.valueOf(dto.getType().toUpperCase()));

        return toDTO(categoryRepository.save(category));
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    private CategoryDTO toDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .color(category.getColor())
                .icon(category.getIcon())
                .type(category.getType().name())
                .build();
    }

    private Category toEntity(CategoryDTO dto) {
        return Category.builder()
                .name(dto.getName())
                .color(dto.getColor())
                .icon(dto.getIcon())
                .type(Category.CategoryType.valueOf(dto.getType().toUpperCase()))
                .build();
    }
}
