package com.periodic.pro.data.favorites

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class FavoritesRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var repository: FavoritesRepository

    @Before
    fun setUp() {
        val testFile = File(tempFolder.root, "test_favorites.preferences_pb")
        repository = FavoritesRepository(
            dataStore = PreferenceDataStoreFactory.create(
                produceFile = { testFile }
            )
        )
    }

    @Test
    fun `initial favorites set is empty`() = runTest {
        val favorites = repository.favoritesFlow.first()
        assertTrue(favorites.isEmpty())
    }

    @Test
    fun `toggle adds element to favorites`() = runTest {
        repository.toggle(1)

        val favorites = repository.favoritesFlow.first()
        assertEquals(setOf(1), favorites)
    }

    @Test
    fun `toggle removes element from favorites`() = runTest {
        repository.toggle(1)
        repository.toggle(1)

        val favorites = repository.favoritesFlow.first()
        assertTrue(favorites.isEmpty())
    }

    @Test
    fun `toggle multiple elements`() = runTest {
        repository.toggle(1)
        repository.toggle(2)
        repository.toggle(3)

        val favorites = repository.favoritesFlow.first()
        assertEquals(setOf(1, 2, 3), favorites)
    }

    @Test
    fun `isFavorite returns correct state`() = runTest {
        repository.toggle(1)

        val isFav = repository.isFavorite(1).first()
        assertEquals(true, isFav)

        val isNotFav = repository.isFavorite(2).first()
        assertEquals(false, isNotFav)
    }

    @Test
    fun `getAll returns same data as favoritesFlow`() = runTest {
        repository.toggle(1)
        repository.toggle(26)

        val all = repository.getAll().first()
        assertEquals(setOf(1, 26), all)
    }
}
