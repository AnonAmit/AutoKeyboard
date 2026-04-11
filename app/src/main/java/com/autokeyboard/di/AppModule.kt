package com.autokeyboard.di

import android.content.Context
import com.autokeyboard.data.local.PreferencesManager
import com.autokeyboard.data.local.SecureStorage
import com.autokeyboard.data.provider.ProviderRegistry
import com.autokeyboard.data.provider.RewriteRepository
import com.autokeyboard.data.provider.local.LocalModelManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager = PreferencesManager(context)

    @Provides
    @Singleton
    fun provideSecureStorage(
        @ApplicationContext context: Context
    ): SecureStorage = SecureStorage(context)

    @Provides
    @Singleton
    fun provideLocalModelManager(
        @ApplicationContext context: Context
    ): LocalModelManager = LocalModelManager(context)

    @Provides
    @Singleton
    fun provideProviderRegistry(
        secureStorage: SecureStorage,
        localModelManager: LocalModelManager
    ): ProviderRegistry = ProviderRegistry(secureStorage, localModelManager)

    @Provides
    @Singleton
    fun provideRewriteRepository(
        registry: ProviderRegistry,
        preferencesManager: PreferencesManager
    ): RewriteRepository = RewriteRepository(registry, preferencesManager)
}
