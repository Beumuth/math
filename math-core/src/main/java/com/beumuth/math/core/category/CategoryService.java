package com.beumuth.math.core.category;

import com.beumuth.math.client.settheory.orderedset.OrderedSet;
import com.beumuth.math.core.jgraph.element.ElementService;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.beumuth.math.client.category.Categories.ALL_STANDARD;
import static com.beumuth.math.client.category.Categories.CATEGORY;

@Service
public class CategoryService {
    @Autowired
    private ElementService elementService;

    public boolean doesCategoryExist(long id) {
        return elementService.doesElementExist(id) &&
            elementService.doesElementHaveB(id, CATEGORY);
    }

    public List<Boolean> doCategoriesExist(OrderedSet<Long> ids) {
        return elementService.doElementsHaveB(ids, CATEGORY);
    }

    public int numCategories() {
        return elementService.numElementsWithB(CATEGORY);
    }

    public long getCategory(long id) {
        return id;
    }

    public List<Long> getCategories(OrderedSet<Long> ids) {
        List<Boolean> areCategories = doCategoriesExist(ids);
        return IntStream
            .range(0, ids.size())
            .mapToObj(i -> areCategories.get(i) ? ids.get(i) : null)
            .collect(Collectors.toCollection(OrderedSet::new));
    }

    public OrderedSet<Long> getAllCategories() {
        return elementService.getIdsWithB(CATEGORY);
    }

    public long createCategory() {
        return elementService.createPendantTo(CATEGORY);
    }

    public OrderedSet<Long> createCategories(int howMany) {
        return elementService.createPendantsTo(CATEGORY, howMany);
    }

    public void deleteCategory(long id) {
        elementService.deleteElement(id);
    }

    public void deleteCategories(Set<Long> ids) {
        elementService.deleteElements(ids);
    }

    public void deleteNonstandardCategories() {
        deleteCategories(
            Sets.difference(
                getAllCategories(),
                ALL_STANDARD
            )
        );
    }
}
