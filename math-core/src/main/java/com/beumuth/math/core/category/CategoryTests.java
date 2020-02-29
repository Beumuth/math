package com.beumuth.math.core.category;

import com.beumuth.math.client.category.CategoryClient;
import com.beumuth.math.client.settheory.orderedset.OrderedSet;
import com.beumuth.math.client.settheory.orderedset.OrderedSets;
import com.beumuth.math.core.internal.client.ClientService;
import com.beumuth.math.core.settheory.tuple.Tuples;
import feign.FeignException;
import org.bitbucket.radistao.test.annotation.BeforeAllMethods;
import org.bitbucket.radistao.test.runner.BeforeAfterSpringTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;

import static com.beumuth.math.client.category.Categories.ALL_STANDARD;
import static com.beumuth.math.client.category.Categories.CATEGORY;
import static com.beumuth.math.core.external.feign.FeignAssertions.assertExceptionLike;
import static org.junit.Assert.*;

@RunWith(BeforeAfterSpringTestRunner.class)
@SpringBootTest
public class CategoryTests {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ClientService clientService;
    @Autowired
    private MockCategoryService mockCategoryService;

    private static CategoryClient categoryClient;

    private long idNonexistentCategories;
    private OrderedSet<Long> idsNonexistentCategories;

    @BeforeAllMethods
    public void setupAll() {
        categoryClient = clientService.getClient(CategoryClient.class);
    }

    @Before
    public void setupTest() {
        idNonexistentCategories = mockCategoryService.idNonexistentCategory();
        idsNonexistentCategories = mockCategoryService.idsNonexistentCategories(5);
    }

    @After
    public void cleanupTest() {
        categoryService.deleteNonstandardCategories();
    }

    @Test
    public void doesCategoryExistTest_doesNotExist_shouldReturnFalse() {
        assertFalse(categoryClient.doesCategoryExist(idNonexistentCategories));
    }

    @Test
    public void doesCategoryExistTest_doesExist_shouldReturnTrue() {
        assertTrue(categoryClient.doesCategoryExist(CATEGORY));
    }

    @Test
    public void doCategoriesExistTest_emptySet_shouldReturnEmptySet() {
        assertEquals(OrderedSets.empty(), categoryClient.doCategoriesExist(OrderedSets.empty()));
    }

    @Test
    public void doCategoriesExistTest_single_doesNotExist_shouldReturnFalse() {
        assertEquals(
            OrderedSets.singleton(false),
            categoryClient.doCategoriesExist(OrderedSets.with(idNonexistentCategories))
        );
    }

    @Test
    public void doCategoriesExistTest_single_exists_shouldReturnTrue() {
        assertEquals(
            OrderedSets.singleton(true),
            categoryClient.doCategoriesExist(
                OrderedSets.singleton(CATEGORY)
            )
        );
    }

    @Test
    public void doCategoriesExistTest_multiple() {
        OrderedSet<Long> idCategories = categoryService.createCategories(3);
        assertEquals(
            Tuples.join(
                Collections.<Boolean>nCopies(idCategories.size(), true),
                Collections.<Boolean>nCopies(idsNonexistentCategories.size(), false)
            ),
            categoryClient.doCategoriesExist(
                OrderedSets.with(idCategories, idsNonexistentCategories)
            )
        );
    }

    @Test
    public void numCategoriesTest_noneAdded_shouldReturnSizeOfStandardCategories() {
        assertEquals(ALL_STANDARD.size(), categoryClient.numCategories());
    }

    @Test
    public void numCategoriesTest_nAdded_shouldReturnNPlusSizeOfStandardCategories() {
        assertEquals(
            categoryService.createCategories(5).size() + ALL_STANDARD.size(),
            categoryClient.numCategories()
        );
    }

    @Test
    public void getAllCategoriesTest_noneAdded_shouldReturnStandardCategories() {
        assertEquals(
            ALL_STANDARD,
            categoryClient.getAllCategories()
        );
    }

    @Test
    public void getAllCategoriesTest_someAdded_shouldBeReturnedWithStandardCategories() {
        assertEquals(
            OrderedSets.with(
                ALL_STANDARD,
                categoryService.createCategories(5)
            ),
            categoryClient.getAllCategories()
        );
    }

    @Test
    public void getStandardCategoriesTest_shouldBeReturned() {
        assertEquals(
            ALL_STANDARD,
            categoryClient.getStandardCategories()
        );
    }

    @Test
    public void getCategoryTest_doesNotExist_shouldReturn404() {
        try {
            categoryClient.getCategory(idNonexistentCategories);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistentCategories + "");
        }
    }

    @Test
    public void getCategoryTest_exists_shouldBeReturned() {
        assertEquals(
            CATEGORY,
            categoryClient.getCategory(CATEGORY)
        );
    }

    @Test
    public void getCategoriesTest_emptySet_shouldReturnEmptySet() {
        assertEquals(Collections.emptyList(), categoryClient.getCategories(OrderedSets.empty()));
    }

    @Test
    public void getCategoriesTest_one_doesNotExist_shouldReturnNull() {
        assertEquals(Collections.singletonList(null), categoryClient.getCategories(OrderedSets.singleton(idNonexistentCategories)));
    }

    @Test
    public void getCategoriesTest_one_exists_shouldBeReturned() {
        assertEquals(
            Collections.singletonList(CATEGORY),
            categoryClient.getCategories(OrderedSets.singleton(CATEGORY))
        );
    }

    @Test
    public void getCategoriesTest_multiple() {
        OrderedSet<Long> idCategories = categoryService.createCategories(5);
        assertEquals(
            Tuples.join(
                idCategories,
                Collections.<Long>nCopies(idsNonexistentCategories.size(), null)
            ),
            categoryClient.getCategories(
                OrderedSets.with(
                    idCategories,
                    idsNonexistentCategories
                )
            )
        );
    }

    @Test
    public void createCategoryTest_shouldBeCreated() {
        assertTrue(categoryService.doesCategoryExist(categoryClient.createCategory()));
    }

    @Test
    public void createCategoriesTest_notPositiveHowMany_shouldReturn400() {
        try {
            categoryClient.createCategories(0);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400);
        }
    }

    @Test
    public void createCategoriesTest_one_shouldCreateOne() {
        assertEquals(
            Collections.singletonList(true),
            categoryService.doCategoriesExist(
                categoryClient.createCategories(1)
            )
        );
    }

    @Test
    public void createCategoriesTest_n_shouldCreateN() {
        assertEquals(
            Collections.nCopies(5, true),
            categoryService.doCategoriesExist(
                categoryClient.createCategories(5)
            )
        );
    }

    @Test
    public void deleteCategoryTest_isStandardCategory_shouldReturn400() {
        ALL_STANDARD.forEach(idStandardCategory -> {
                try {
                    categoryClient.deleteCategory(idStandardCategory);
                    fail();
                } catch(FeignException e) {
                    assertExceptionLike(e, 400, idStandardCategory + "");
                }
            }
        );
    }

    @Test
    public void deleteCategoryTest_doesNotExist_shouldDoNothing() {
        categoryClient.deleteCategory(idNonexistentCategories);
    }

    @Test
    public void deleteCategoryTest_exists_shouldBeDeleted() {
        long idCategory = categoryService.createCategory();
        categoryClient.deleteCategory(idCategory);
        assertFalse(categoryService.doesCategoryExist(idCategory));
    }

    @Test
    public void deleteCategoriesTest_emptySet_shouldDoNothing() {
        categoryClient.deleteCategories(Collections.emptySet());
    }

    @Test
    public void deleteCategoriesTest_one_isStandardCategory_shouldReturn400() {
        ALL_STANDARD.forEach(idStandardCategory -> {
            try {
                categoryClient.deleteCategories(Collections.singleton(idStandardCategory));
                fail();
            } catch(FeignException e) {
                assertExceptionLike(
                    e,
                    400,
                    idStandardCategory + ""
                );
            }
        });
    }

    @Test
    public void deleteCategoriesTest_one_doesNotExist_shouldDoNothing() {
        categoryClient.deleteCategories(OrderedSets.singleton(idNonexistentCategories));
    }

    @Test
    public void deleteCategoriesTest_one_exists_nonStandard_shouldBeDeleted() {
        long idCategory = categoryService.createCategory();
        categoryClient.deleteCategories(Collections.singleton(idCategory));
        assertFalse(categoryService.doesCategoryExist(idCategory));
    }

    @Test
    public void deleteCategoriesTest_multiple() {
        OrderedSet<Long> idCategories = categoryService.createCategories(5);
        categoryClient.deleteCategories(idCategories);
        assertEquals(
            Collections.nCopies(idCategories.size(), false),
            categoryService.doCategoriesExist(idCategories)
        );
    }
}
