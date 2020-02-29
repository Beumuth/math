package com.beumuth.math.core.category;

import com.beumuth.math.client.settheory.orderedset.OrderedSet;
import com.github.instantpudd.validator.ClientErrorException;
import com.github.instantpudd.validator.Validator;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.beumuth.math.client.category.Categories.ALL_STANDARD;
import static com.github.instantpudd.validator.ClientErrorStatusCode.BAD_REQUEST;

@Controller
@RequestMapping("/api/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @RequestMapping(method=RequestMethod.GET, path="/category/{id}/exists")
    @ResponseBody
    public boolean doesCategoryExist(@PathVariable("id") long id) {
        return categoryService.doesCategoryExist(id);
    }

    @RequestMapping(method=RequestMethod.GET, path="/exist")
    @ResponseBody
    public List<Boolean> doCategoriesExist(@RequestParam(value="ids", required=false) OrderedSet<Long> ids) {
        return ids == null || ids.isEmpty() ?
            Collections.emptyList() :
            categoryService.doCategoriesExist(ids);
    }

    @RequestMapping(method=RequestMethod.GET, path="/count")
    @ResponseBody
    public int numCategories() {
        return categoryService.numCategories();
    }

    @RequestMapping(method=RequestMethod.GET, path="/category/{id}")
    @ResponseBody
    public long getCategory(@PathVariable("id") long id) {
        return categoryService.getCategory(id);
    }

    @RequestMapping(method=RequestMethod.GET, path="/all")
    @ResponseBody
    public OrderedSet<Long> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @RequestMapping(method=RequestMethod.GET)
    @ResponseBody
    public List<Long> getCategories(@RequestParam(value="ids", required=false) OrderedSet<Long> ids) {
        return ids == null || ids.isEmpty() ?
            Collections.emptyList() :
            categoryService.getCategories(ids);
    }

    @RequestMapping(method=RequestMethod.GET, path="/standard")
    @ResponseBody
    public OrderedSet<Long> getStandardCategories() {
        return ALL_STANDARD;
    }

    @RequestMapping(method=RequestMethod.POST, value="/category")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public long createCategory() {
        return categoryService.createCategory();
    }

    @RequestMapping(method=RequestMethod.POST)
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public OrderedSet<Long> createCategories(@RequestBody int howMany) throws ClientErrorException {
        Validator
            .returnStatus(BAD_REQUEST)
            .ifTrue(howMany <= 0)
            .withErrorMessage("The request body must be a positive integer")
            .execute();
        return categoryService.createCategories(howMany);
    }

    @RequestMapping(method=RequestMethod.DELETE, value="/category/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable("id") long id) throws ClientErrorException {
        Validator
            .returnStatus(BAD_REQUEST)
            .ifTrue(ALL_STANDARD.contains(id))
            .withErrorMessage(
                "The Category with id [" + id + "] cannot be deleted because it is a standard Category"
            ).execute();
        if(categoryService.doesCategoryExist(id)) {
            categoryService.deleteCategory(id);
        }
    }

    @RequestMapping(method=RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategories(@RequestParam(value="ids", required=false) Set<Long> ids) throws ClientErrorException {
        if(ids == null || ids.isEmpty()) {
            return;
        }
        Set<Long> idsOfStandardCategories = Sets.intersection(
            ids,
            ALL_STANDARD
        );
        Validator   //Ensure no standard categories are being deleted
            .returnStatus(BAD_REQUEST)
            .ifFalse(idsOfStandardCategories.isEmpty())
            .withErrorMessage(
                "Deletion could not happen because the given ids contain the following standard categories: [" +
                    idsOfStandardCategories
                        .stream()
                        .map( idStandardCategory ->
                            "[" + idStandardCategory + "]"
                        ).collect(Collectors.joining(",")) + "]"
            ).execute();
        categoryService.deleteCategories(   //Delete only the ids that are categories
            ids
                .stream()
                .filter(idCategory -> categoryService.doesCategoryExist(idCategory))
                .collect(Collectors.toSet())
        );
    }
}
