package pl.nehrebeccy.crmfinanse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.nehrebeccy.crmfinanse.model.Category;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByType(Category.CategoryType type);

    Optional<Category> findByName(String name);
}
