package com.periodic.pro.feature.table

import app.cash.turbine.test
import com.periodic.pro.data.element.ElementRepository
import com.periodic.pro.data.element.model.Category
import com.periodic.pro.data.element.model.Element
import com.periodic.pro.data.favorites.FavoritesRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TableViewModelTest {

    @MockK
    private lateinit var elementRepo: ElementRepository

    @MockK
    private lateinit var favoritesRepo: FavoritesRepository

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var viewModel: TableViewModel

    private val sampleElements = listOf(
        Element(
            atomicNumber = 1,
            symbol = "H",
            name = "Hydrogen",
            atomicMass = 1.008,
            category = Category.NONMETAL,
        ),
        Element(
            atomicNumber = 2,
            symbol = "He",
            name = "Helium",
            atomicMass = 4.0026,
            category = Category.NOBLE_GAS,
        ),
        Element(
            atomicNumber = 26,
            symbol = "Fe",
            name = "Iron",
            atomicMass = 55.845,
            category = Category.TRANSITION_METAL,
        ),
        Element(
            atomicNumber = 79,
            symbol = "Au",
            name = "Gold",
            atomicMass = 196.9666,
            category = Category.TRANSITION_METAL,
        ),
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        every { elementRepo.getAll() } returns sampleElements
        every { elementRepo.getZh(any()) } returns null
        every { favoritesRepo.favoritesFlow } returns flowOf(emptySet())
        coEvery { favoritesRepo.toggle(any()) } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load elements updates state with elements`() = testScope.runTest {
        viewModel = TableViewModel(elementRepo, favoritesRepo)

        // Advance dispatcher to process the init coroutine
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(4, state.elements.size)
        assertEquals("H", state.elements[0].symbol)
    }

    @Test
    fun `search query filters state`() = testScope.runTest {
        viewModel = TableViewModel(elementRepo, favoritesRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handle(TableIntent.Search("H"))

        assertEquals("H", viewModel.state.value.searchQuery)
    }

    @Test
    fun `filter by category updates selectedCategory`() = testScope.runTest {
        viewModel = TableViewModel(elementRepo, favoritesRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handle(TableIntent.FilterByCategory(Category.TRANSITION_METAL))

        assertEquals(Category.TRANSITION_METAL, viewModel.state.value.selectedCategory)
    }

    @Test
    fun `filter by category null resets filter`() = testScope.runTest {
        viewModel = TableViewModel(elementRepo, favoritesRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handle(TableIntent.FilterByCategory(Category.TRANSITION_METAL))
        viewModel.handle(TableIntent.FilterByCategory(null))

        assertEquals(null, viewModel.state.value.selectedCategory)
    }

    @Test
    fun `enter multi select mode`() = testScope.runTest {
        viewModel = TableViewModel(elementRepo, favoritesRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handle(TableIntent.EnterMultiSelect)

        assertTrue(viewModel.state.value.isMultiSelectMode)
    }

    @Test
    fun `enter multi select via long click adds to selected`() = testScope.runTest {
        viewModel = TableViewModel(elementRepo, favoritesRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handle(TableIntent.OnElementLongClick(1))

        val state = viewModel.state.value
        assertTrue(state.isMultiSelectMode)
        assertEquals(setOf(1), state.selectedIds)
    }

    @Test
    fun `exit multi select clears selection`() = testScope.runTest {
        viewModel = TableViewModel(elementRepo, favoritesRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handle(TableIntent.OnElementLongClick(1))
        viewModel.handle(TableIntent.ExitMultiSelect)

        val state = viewModel.state.value
        assertFalse(state.isMultiSelectMode)
        assertTrue(state.selectedIds.isEmpty())
    }

    @Test
    fun `element click in normal mode sends navigate effect`() = testScope.runTest {
        viewModel = TableViewModel(elementRepo, favoritesRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.handle(TableIntent.OnElementClick(6))

            val effect = awaitItem()
            assertEquals(TableEffect.NavigateToDetail(6), effect)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `element click in multi select mode toggles selection`() = testScope.runTest {
        viewModel = TableViewModel(elementRepo, favoritesRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handle(TableIntent.OnElementLongClick(1))
        viewModel.handle(TableIntent.OnElementClick(2))

        assertEquals(setOf(1, 2), viewModel.state.value.selectedIds)
    }
}
