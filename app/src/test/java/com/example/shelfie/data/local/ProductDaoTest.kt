package com.example.shelfie.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ProductDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ProductDao

    private fun p(id: Int, title: String, category: String, rate: Double, count: Int = 10, desc: String = "desc") =
        ProductEntity(id, title, 1.0, desc, category, "img", rate, count)

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), AppDatabase::class.java)
            .allowMainThreadQueries().build()
        dao = db.productDao()
        runBlocking {
            dao.upsertAll(
                listOf(
                    p(1, "Red Backpack", "bags", 3.9),
                    p(2, "Blue Backpack", "bags", 4.5),
                    p(3, "Gold Ring", "jewelery", 4.8, desc = "100% pure_gold"),
                    p(4, "SSD Drive", "electronics", 4.8, count = 300),
                    p(5, "Laptop", "electronics", 2.1),
                    p(6, "Phone", "electronics", 4.0),
                    p(7, "Watch", "jewelery", 3.0),
                )
            )
        }
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun topRatedIsOrderedByRatingDescAndLimited() = runBlocking {
        val result = dao.getTopRated(5).first()
        assertEquals(listOf(4, 3, 2, 6, 1), result.map { it.id })
    }

    @Test
    fun searchIsCaseInsensitive() = runBlocking {
        assertEquals(listOf(2, 1), dao.search("bAcKpAcK").first().map { it.id })
    }

    @Test
    fun searchMatchesPartialText() = runBlocking {
        assertEquals(listOf(4), dao.search("Dri").first().map { it.id })
    }

    @Test
    fun searchMatchesDescriptionAndCategory() = runBlocking {
        assertEquals(setOf(3, 7), dao.search("jewel").first().map { it.id }.toSet())
    }

    @Test
    fun searchNoMatchReturnsEmpty() = runBlocking {
        assertEquals(emptyList<ProductEntity>(), dao.search("zzz").first())
    }

    @Test
    fun searchTreatsEscapedWildcardsLiterally() = runBlocking {
        assertEquals(listOf(3), dao.search(escapeLike("100%")).first().map { it.id })
        assertEquals(listOf(3), dao.search(escapeLike("pure_gold")).first().map { it.id })
        assertEquals(emptyList<ProductEntity>(), dao.search(escapeLike("pure_gol_")).first())
        assertEquals(emptyList<ProductEntity>(), dao.search(escapeLike("%")).first().filter { it.id != 3 })
    }

    @Test
    fun unescapedWildcardMatchesEverything() = runBlocking {
        assertEquals(7, dao.search("%").first().size)
    }

    @Test
    fun escapeLikeEscapesSpecialCharacters() {
        assertEquals("a\\%b\\_c\\\\d", escapeLike("a%b_c\\d"))
    }
}
