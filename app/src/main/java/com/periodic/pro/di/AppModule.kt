package com.periodic.pro.di

import com.periodic.pro.data.discover.DiscoverRepository
import com.periodic.pro.data.element.ElementRepository
import com.periodic.pro.data.favorites.FavoritesRepository
import com.periodic.pro.data.favorites.favoritesDataStore
import com.periodic.pro.data.lab.LabRepository
import com.periodic.pro.data.learn.LearnRepository
import com.periodic.pro.data.theme.ThemePreferenceRepository
import com.periodic.pro.data.update.ApkInstaller
import com.periodic.pro.data.permission.PermissionsManager
import com.periodic.pro.data.update.UpdatePreferences
import com.periodic.pro.data.update.UpdateRepository
import com.periodic.pro.data.update.UpdateService
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
    viewModel { params -> DetailViewModel(get(), get(), get(), params.get()) }
    single { DiscoverRepository(androidContext()) }
    single { LearnRepository(androidContext()) }
    single { LabRepository(androidContext()) }
    viewModel { CategoryViewModel(get()) }
    single { UpdateRepository(androidContext()) }
    single { UpdatePreferences(androidContext()) }
    single { UpdateService(get(), get()) }
    single { PermissionsManager(androidContext()) }
    single { ApkInstaller(androidContext()) }
    viewModel { ProfileViewModel(get(), get(), get()) }
    viewModel { DiscoverViewModel(get(), get()) }
    viewModel { LearnViewModel(get(), get()) }
    viewModel { LabViewModel(get(), get()) }
}
