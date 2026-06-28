package io.github.orizynpx.arxwipe.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArxivTaxonomyTest {

    @Test
    fun `getByCategoryGroup returns only categories from requested group`() {
        val csCategories = ArxivTaxonomy.getByCategoryGroup(MainField.COMPUTER_SCIENCE)
        
        assertTrue(csCategories.isNotEmpty())
        assertTrue(csCategories.all { it.group == MainField.COMPUTER_SCIENCE })
    }

    @Test
    fun `getByCategoryGroup returns empty list for group with no categories`() {
        
        
        
        
        val physics = ArxivTaxonomy.getByCategoryGroup(MainField.PHYSICS)
        assertTrue(physics.isNotEmpty())
        assertTrue(physics.all { it.group == MainField.PHYSICS })
        
        val math = ArxivTaxonomy.getByCategoryGroup(MainField.MATHEMATICS)
        assertTrue(math.isNotEmpty())
        assertTrue(math.all { it.group == MainField.MATHEMATICS })
    }

    @Test
    fun `categories list contains expected wildcard categories`() {
        val wildcards = ArxivTaxonomy.categories.filter { it.categoryId.endsWith(".*") }
        val expectedCodes = listOf(
            "cs.*", "econ.*", "eess.*", "math.*", "physics.*", 
            "astro-ph.*", "cond-mat.*", "nlin.*", "q-bio.*", "q-fin.*", "stat.*"
        )
        
        expectedCodes.forEach { code ->
            assertTrue("Should contain $code", wildcards.any { it.categoryId == code })
        }
    }
}
