package com.periodic.pro.di

import com.periodic.pro.data.element.ElementRepository
import com.periodic.pro.data.favorites.FavoritesRepository
import com.periodic.pro.data.theme.ThemePreferenceRepository
import com.periodic.pro.feature.table.TableViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { ElementRepository(androidContext()) }
    single { FavoritesRepository(androidContext()) }
    single { ThemePreferenceRepository(androidContext()) }

    viewModel { TableViewModel(get(), get()) }
}
