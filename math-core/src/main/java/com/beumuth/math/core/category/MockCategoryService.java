package com.beumuth.math.core.category;

import com.beumuth.math.client.settheory.orderedset.OrderedSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MockCategoryService {
    @Autowired
    private CategoryService categoryService;

    public long idNonexistentCategory() {
        long idCategory = categoryService.createCategory();
        categoryService.deleteCategory(idCategory);
        return idCategory;
    }

    public OrderedSet<Long> idsNonexistentCategories(int howMany) {
        OrderedSet<Long> idCategories = categoryService.createCategories(howMany);
        categoryService.deleteCategories(idCategories);
        return idCategories;
    }
}
