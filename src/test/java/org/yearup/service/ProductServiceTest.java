package org.yearup.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yearup.models.Product;
import org.yearup.repository.ProductRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

// disregard this message
// MockitoExtension wires up the @Mock and @InjectMocks fields below.
// No Spring context and no database are started -- this is a pure unit test
// of the search logic, which is why it runs in milliseconds.
@ExtendWith(MockitoExtension.class)
class ProductServiceTest
{
    @Mock
    private ProductRepository productRepository;   // a fake repo we fully control

    @InjectMocks
    private ProductService productService;         // real service, fake repo injected in

    // small helper so each test reads cleanly
    private Product product(int id, double price, String subCategory, boolean featured)
    {
        return new Product(id, "Product " + id, price, 1, "desc", subCategory, 10, featured, "img.jpg");
    }

    // THE regression test for Bug 1.
    // Before the fix, search() ended with an unconditional .filter(Product::isFeatured),
    // so this non-featured product would have been dropped. After the fix it must appear.
    @Test
    void search_includesNonFeaturedProducts()
    {
        Product featured    = product(1, 30.00, "rock", true);
        Product notFeatured = product(2, 40.00, "jazz", false);

        // when no category is passed, the service calls findAll()
        when(productRepository.findAll()).thenReturn(List.of(featured, notFeatured));

        // no filters at all -> every product should come back
        List<Product> results = productService.search(null, null, null, null);

        assertEquals(2, results.size(), "Both products should be returned when no filters are applied");
        assertTrue(results.contains(notFeatured), "Non-featured products must NOT be excluded from search");
    }

    // Proves the minPrice filter is inclusive (>=) and actually excludes cheaper items.
    @Test
    void search_filtersByMinPrice_inclusive()
    {
        Product cheap      = product(1, 10.00, "rock", false);
        Product onBoundary = product(2, 25.00, "rock", false);   // exactly minPrice
        Product expensive  = product(3, 40.00, "rock", false);

        when(productRepository.findAll()).thenReturn(List.of(cheap, onBoundary, expensive));

        List<Product> results = productService.search(null, 25.00, null, null);

        assertEquals(2, results.size(), "Only products priced >= 25 should remain");
        assertTrue(results.contains(onBoundary), "A product priced exactly at minPrice must be included");
        assertFalse(results.contains(cheap), "A product priced below minPrice must be excluded");
    }

    // Proves min + max together define a band, and that passing a category
    // routes the lookup through findByCategoryId instead of findAll.
    @Test
    void search_filtersByPriceBand_withCategory()
    {
        Product tooCheap = product(1, 10.00, "rock", false);
        Product inBand   = product(2, 30.00, "rock", false);
        Product tooDear  = product(3, 80.00, "rock", false);

        // category was supplied, so the service uses the category lookup
        when(productRepository.findByCategoryId(1)).thenReturn(List.of(tooCheap, inBand, tooDear));

        List<Product> results = productService.search(1, 20.00, 50.00, null);

        assertEquals(1, results.size());
        assertEquals(inBand, results.get(0));
    }

    // Proves the subcategory filter is case-insensitive and optional.
    @Test
    void search_filtersBySubCategory_caseInsensitive()
    {
        Product rock = product(1, 30.00, "Rock", false);
        Product jazz = product(2, 30.00, "Jazz", false);

        when(productRepository.findAll()).thenReturn(List.of(rock, jazz));

        List<Product> results = productService.search(null, null, null, "rock");

        assertEquals(1, results.size(), "Only the matching subcategory should remain");
        assertEquals(rock, results.get(0));
    }
}
