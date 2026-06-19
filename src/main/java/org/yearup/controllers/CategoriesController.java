package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.yearup.models.Category;
import org.yearup.models.Product;
import org.yearup.service.CategoryService;
import org.yearup.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/categories")
@CrossOrigin
public class CategoriesController
{
    private CategoryService categoryService;
    private ProductService productService;

    @Autowired
    public CategoriesController(CategoryService categoryService, ProductService productService)
    {
        this.categoryService = categoryService;
        this.productService = productService;
    }

    // GET http://localhost:8080/categories
    @GetMapping
    public List<Category> getAll()
    {
        return categoryService.getAllCategories();
    }

    // GET http://localhost:8080/categories/1
    @GetMapping("{id}")
    public Category getById(@PathVariable int id)
    {
        return categoryService.getById(id);
    }

    // GET http://localhost:8080/categories/1/products
    @GetMapping("{categoryId}/products")
    public List<Product> getProductsById(@PathVariable int categoryId)
    {
        // NOTE: verify this method name against your ProductService.
        // Common names in this starter are getProductsByCategoryId(...) or search(...).
        return productService.(categoryId);
    }

    // POST http://localhost:8080/categories  (ADMIN only)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> addCategory(@RequestBody Category category)
    {
        Category created = categoryService.create(category);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // PUT http://localhost:8080/categories/1  (ADMIN only)
    @PutMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Category updateCategory(@PathVariable int id, @RequestBody Category category)
    {
        return categoryService.update(id, category);
    }

    // DELETE http://localhost:8080/categories/1  (ADMIN only)
    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable int id)
    {
        categoryService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}