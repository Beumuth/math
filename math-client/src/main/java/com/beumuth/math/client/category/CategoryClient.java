package com.beumuth.math.client.category;

import com.beumuth.math.MathClient;
import com.beumuth.math.client.settheory.orderedset.OrderedSet;
import com.google.common.collect.BiMap;
import feign.Param;
import feign.RequestLine;

import java.util.List;
import java.util.Set;

public interface CategoryClient extends MathClient {
    @RequestLine("GET api/categories/category/{id}/exists")
    boolean doesCategoryExist(@Param("id") long id);

    @RequestLine("GET api/categories/exist?ids={ids}")
    List<Boolean> doCategoriesExist(@Param("ids") OrderedSet<Long> ids);

    @RequestLine("GET api/categories/count")
    int numCategories();

    @RequestLine("GET api/categories")
    OrderedSet<Long> getAllCategories();

    @RequestLine("GET api/categories/standard")
    BiMap<StandardCategory, Long> getStandardCategories();

    @RequestLine("GET api/categories/category/{id}")
    long getCategory(@Param("id") long id);

    @RequestLine("GET api/categories?ids={ids}")
    List<Long> getCategories(@Param("ids") OrderedSet<Long> ids);

    @RequestLine("POST api/categories/category")
    long createCategory();

    @RequestLine("POST api/categories")
    OrderedSet<Long> createCategories(int howMany);

    @RequestLine("DELETE api/categories/category/{id}")
    void deleteCategory(long id);

    @RequestLine("DELETE api/categories?ids={ids}")
    void deleteCategories(@Param("ids") Set<Long> ids);
}
