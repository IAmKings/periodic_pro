package com.periodic.pro.data.element.model

import kotlinx.serialization.Serializable

@Serializable
data class Element(
    val atomicNumber: Int,
    val symbol: String,
    val name: String,
    val atomicMass: Double? = null,
    val category: String,
    val electronConfiguration: String? = null,
    val electronegativity: Double? = null,
    val atomicRadius: Double? = null,
    val ionizationEnergy: Double? = null,
    val density: Double? = null,
    val meltingPoint: Double? = null,
    val boilingPoint: Double? = null,
    val group: Int? = null,
    val period: Int? = null,
    val discoveredBy: String? = null,
    val yearDiscovered: Int? = null,
)
