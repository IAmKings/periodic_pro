package com.periodic.pro.data.element

import android.content.Context
import android.content.res.AssetManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

class ElementRepositoryTest {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var assetManager: AssetManager

    private lateinit var repository: ElementRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.assets } returns assetManager

        // Mini JSON for elements with 3 elements
        val elementsJson = """
            {
                "elements": [
                    {
                        "atomicNumber": 1,
                        "symbol": "H",
                        "name": "Hydrogen",
                        "atomicMass": 1.008,
                        "category": "nonmetal",
                        "electronConfiguration": "1s1",
                        "electronegativity": 2.2,
                        "atomicRadius": 120,
                        "ionizationEnergy": 13.598,
                        "density": 8.988e-05,
                        "meltingPoint": 13.99,
                        "boilingPoint": 20.271,
                        "group": 1,
                        "period": 1,
                        "discoveredBy": "Henry Cavendish",
                        "yearDiscovered": 1766
                    },
                    {
                        "atomicNumber": 26,
                        "symbol": "Fe",
                        "name": "Iron",
                        "atomicMass": 55.845,
                        "category": "transition-metal",
                        "electronConfiguration": "[Ar] 3d6 4s2",
                        "electronegativity": 1.83,
                        "atomicRadius": 140,
                        "ionizationEnergy": 7.902,
                        "density": 7.874,
                        "meltingPoint": 1811.0,
                        "boilingPoint": 3134.0,
                        "group": 8,
                        "period": 4,
                        "discoveredBy": "Ancient",
                        "yearDiscovered": null
                    },
                    {
                        "atomicNumber": 79,
                        "symbol": "Au",
                        "name": "Gold",
                        "atomicMass": 196.9666,
                        "category": "transition-metal",
                        "electronConfiguration": "[Xe] 4f14 5d10 6s1",
                        "electronegativity": 2.54,
                        "atomicRadius": 144,
                        "ionizationEnergy": 9.225,
                        "density": 19.32,
                        "meltingPoint": 1337.33,
                        "boilingPoint": 3129.0,
                        "group": 11,
                        "period": 6,
                        "discoveredBy": "Ancient",
                        "yearDiscovered": null
                    }
                ]
            }
        """.trimIndent()

        val zhJson = """
            [
                { "atomicNumber": 1, "nameZh": "氢", "pinyin": "qīng" },
                { "atomicNumber": 26, "nameZh": "铁", "pinyin": "tiě" },
                { "atomicNumber": 79, "nameZh": "金", "pinyin": "jīn" }
            ]
        """.trimIndent()

        every { assetManager.open("elements.json") } returns
            ByteArrayInputStream(elementsJson.toByteArray())
        every { assetManager.open("elements_zh.json") } returns
            ByteArrayInputStream(zhJson.toByteArray())

        repository = ElementRepository(context)
    }

    @Test
    fun `load all elements returns correct count`() {
        val all = repository.getAll()
        assertEquals(3, all.size)
    }

    @Test
    fun `getByNumber returns correct element`() {
        val element = repository.getByNumber(1)
        assertNotNull(element)
        assertEquals("H", element?.symbol)
        assertEquals("Hydrogen", element?.name)
        assertEquals(1, element?.atomicNumber)
    }

    @Test
    fun `search by symbol returns matching elements`() {
        val results = repository.search("Fe")
        assertEquals(1, results.size)
        assertEquals("Fe", results[0].symbol)
    }

    @Test
    fun `search by Chinese name returns matching elements`() {
        val results = repository.search("氢")
        assertEquals(1, results.size)
        assertEquals("H", results[0].symbol)
    }

    @Test
    fun `search by lowercase symbol is case insensitive`() {
        val results = repository.search("fe")
        assertEquals(1, results.size)
        assertEquals("Fe", results[0].symbol)
    }

    @Test
    fun `search by English name returns matching elements`() {
        val results = repository.search("Gold")
        assertEquals(1, results.size)
        assertEquals("Au", results[0].symbol)
    }

    @Test
    fun `search by atomic number prefix returns matching elements`() {
        val results = repository.search("7")
        assertEquals(1, results.size)
        assertEquals("Au", results[0].symbol)
    }

    @Test
    fun `search with empty query returns empty list`() {
        val results = repository.search("")
        assertTrue(results.isEmpty())
    }

    @Test
    fun `search with no match returns empty list`() {
        val results = repository.search("zzz")
        assertTrue(results.isEmpty())
    }
}
