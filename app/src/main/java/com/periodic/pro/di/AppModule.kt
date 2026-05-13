package com.periodic.pro.di

import com.periodic.pro.data.discover.DiscoverRepository
import com.periodic.pro.data.element.ElementRepository
import com.periodic.pro.data.favorites.FavoritesRepository
import com.periodic.pro.data.favorites.favoritesDataStore
import com.periodic.pro.data.lab.LabRepository
import com.periodic.pro.data.learn.LearnRepository
import com.periodic.pro.data.theme.ThemePreferenceRepository
import com.periodic.pro.feature.category.CategoryViewModel
import com.periodic.pro.feature.compare.CompareViewModel
import com.periodic.pro.feature.detail.DetailViewModel
import com.periodic.pro.feature.discover.DiscoverViewModel
import com.periodic.pro.feature.favorites.FavoritesViewModel
import com.periodic.pro.feature.home.HomeViewModel
import com.periodic.pro.feature.lab.LabViewModel
import com.periodic.pro.feature.learn.LearnViewModel
import com.periodic.pro.feature.profile.ProfileViewModel
import com.periodic.pro.feature.table.TableViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { ElementRepository(androidContext()) }
    single { FavoritesRepository(androidContext().favoritesDataStore) }
    single { ThemePreferenceRepository(androidContext()) }

    viewModel { TableViewModel(get(), get()) }
    viewModel { params -> CompareViewModel(get(), params.get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { FavoritesViewModel(get(), get()) }
    viewModel { params -> DetailViewModel(get(), get(), params.get()) }
    single { DiscoverRepository(androidContext()) }
    single { LearnRepository(androidContext()) }
    single { LabRepository(androidContext()) }
    viewModel { CategoryViewModel(get()) }
    viewModel { ProfileViewModel(get()) }
    viewModel { DiscoverViewModel(get()) }
    viewModel { LearnViewModel(get()) }
    viewModel { LabViewModel(get()) }
}
