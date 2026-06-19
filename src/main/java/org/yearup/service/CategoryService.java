package org.yearup.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.models.Category;
import org.yearup.repository.CategoryRepository;

import java.util.List;

@Service
public class CategoryService
{
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository)
    {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAllCategories()
    {
        // get all categories
        return categoryRepository.findAll();
    }

    public Category getById(int categoryId)
    {
        // get category by id; 404 if it doesn't exist
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }

    public Category create(Category category)
    {
        // no id set -> JPA inserts a new row
        return categoryRepository.save(category);
    }

    public Category update(int categoryId, Category category)
    {
        // make sure it exists first, so updating a missing id is a clean 404
        if (!categoryRepository.existsById(categoryId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");

        // trust the URL's id, not whatever the body sent
        category.setCategoryId(categoryId);

        // id is set -> JPA updates the existing row
        return categoryRepository.save(category);
    }

    public void delete(int categoryId)
    {
        // 404 if there's nothing to delete
        if (!categoryRepository.existsById(categoryId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");

        categoryRepository.deleteById(categoryId);
    }
}