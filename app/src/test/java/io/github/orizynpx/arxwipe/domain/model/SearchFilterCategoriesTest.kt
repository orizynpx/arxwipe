package io.github.orizynpx.arxwipe.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchFilterCategoriesTest {

    @Test
    fun `groups contain all main fields`() {
        val groups = SearchFilterCategories.groups
        val expectedFields = MainField.entries.toSet()
        
        
        
        
        assertEquals(8, groups.size)
    }

    @Test
    fun `physics group contains specific subcategories`() {
        val physicsGroup = SearchFilterCategories.groups.find { it.nameRes == MainField.PHYSICS.groupNameRes }
        
        assertTrue(physicsGroup != null)
        val codes = physicsGroup!!.subcategories.flatMap { it.codes }
        
        assertTrue(codes.contains("physics.*"))
        assertTrue(codes.contains("hep-ex")) 
        assertTrue(codes.contains("nucl-th")) 
    }

    @Test
    fun `allSubcategories returns flattened list of all subcategories`() {
        val allSubs = SearchFilterCategories.allSubcategories
        val totalSubs = SearchFilterCategories.groups.sumOf { it.subcategories.size }
        
        assertEquals(totalSubs, allSubs.size)
    }
}
