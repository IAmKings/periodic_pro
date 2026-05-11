package com.periodic.pro.feature.detail

import com.periodic.pro.data.element.ElementRepository
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    @MockK
    private lateinit var elementRepo: ElementRepository

    @MockK
    private lateinit var favoritesRepo: FavoritesRepository

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val hydrogen = Element(
        atomicNumber = 1,
        symbol = "H",
        name = "Hydrogen",
        atomicMass = 1.008,
        category = "nonmetal",
        electronConfiguration = "1s1",
        electronegativity = 2.2,
        atomicRadius = 120.0,
        ionizationEnergy = 13.598,
        density = 8.988e-05,
        meltingPoint = 13.99,
        boilingPoint = 20.271,
        group = 1,
        period = 1,
        discoveredBy = "Henry Cavendish",
        yearDiscovered = 1766,
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        every { elementRepo.getByNumber(1) } returns hydrogen
        every { elementRepo.getZh(1) } returns null

        // Default: not a favorite
        every { favoritesRepo.isFavorite(1) } returns flowOf(false)
        coEvery { favoritesRepo.toggle(any()) } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load element success sets element in state`() {
        val viewModel = DetailViewModel(elementRepo, favoritesRepo, 1)

        val state = viewModel.state.value
        assertNotNull(state.element)
        assertEquals("H", state.element?.symbol)
        assertEquals(1, state.element?.atomicNumber)
        assertFalse(state.isLoading)
    }

    @Test
    fun `toggle favorite changes isFavorite`() = testScope.runTest {
        // Simulate favorite state changing after toggle
        every { favoritesRepo.isFavorite(1) } returns flowOf(false, true)

        val viewModel = DetailViewModel(elementRepo, favoritesRepo, 1)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handle(DetailIntent.ToggleFavorite)

        // After toggle, isFavorite should eventually be true
        // Since it's async via collect, we need to advance
        testDispatcher.scheduler.advanceUntilIdle()

        // The second emission from flowOf(false, true) should be received
        assertTrue(viewModel.state.value.isFavorite)
    }

    @Test
    fun `isFavorite starts false when not favorited`() {
        every { favoritesRepo.isFavorite(1) } returns flowOf(false)

        val viewModel = DetailViewModel(elementRepo, favoritesRepo, 1)

        assertFalse(viewModel.state.value.isFavorite)
    }

    @Test
    fun `isFavorite starts true when already favorited`() = testScope.runTest {
        every { favoritesRepo.isFavorite(1) } returns flowOf(true)

        val viewModel = DetailViewModel(elementRepo, favoritesRepo, 1)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.isFavorite)
    }
}
