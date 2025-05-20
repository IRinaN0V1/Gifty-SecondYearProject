package com.example.gifty.di

import com.example.gifty.Api
import com.example.gifty.DataSources.Categories.CategoryDataSourceImpl
import com.example.gifty.DataSources.Events.EventDataSourceImpl
import com.example.gifty.DataSources.Forms.FormDataSourceImpl
import com.example.gifty.DataSources.Gifts.GiftDataSourceImpl
import com.example.gifty.DataSources.Users.UserDataSourceImpl
import com.example.gifty.Interactors.CategoryInteractor
import com.example.gifty.Interactors.EventInteractor
import com.example.gifty.Interactors.UserInteractor
import com.example.gifty.Interactors.FormInteractor
import com.example.gifty.Interactors.GiftInteractor
import com.example.gifty.Repositories.CategoryDataRepository
import com.example.gifty.Repositories.EventRepository
import com.example.gifty.Repositories.UserRepository
import com.example.gifty.Repositories.FormRepository
import com.example.gifty.Repositories.GiftRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DependencyInjectionModule {
    @Provides
    @Singleton
    fun provideUserDataSourceImpl(api: Api): UserDataSourceImpl {
        return UserDataSourceImpl(api)
    }

    @Provides
    @Singleton
    fun provideUserInteractor(userRepository: UserRepository): UserInteractor {
        return UserInteractor(userRepository)
    }

    @Provides
    @Singleton
    fun provideUserRepository(userDataSourceImpl: UserDataSourceImpl): UserRepository {
        return UserRepository(userDataSourceImpl)
    }

    @Provides
    @Singleton
    fun provideFormDataSourceImpl(api: Api): FormDataSourceImpl {
        return FormDataSourceImpl(api)
    }

    @Provides
    @Singleton
    fun provideFormInteractor(formRepository: FormRepository): FormInteractor {
        return FormInteractor(formRepository)
    }

    @Provides
    @Singleton
    fun provideFromRepository(formDataSourceImpl: FormDataSourceImpl): FormRepository {
        return FormRepository(formDataSourceImpl)
    }

    @Provides
    @Singleton
    fun provideEventDataSourceImpl(api: Api): EventDataSourceImpl {
        return EventDataSourceImpl(api)
    }

    @Provides
    @Singleton
    fun provideEventInteractor(eventRepository: EventRepository): EventInteractor {
        return EventInteractor(eventRepository)
    }

    @Provides
    @Singleton
    fun provideEventRepository(eventDataSourceImpl: EventDataSourceImpl): EventRepository {
        return EventRepository(eventDataSourceImpl)
    }

    @Provides
    @Singleton
    fun provideGiftDataSourceImpl(api: Api): GiftDataSourceImpl {
        return GiftDataSourceImpl(api)
    }

    @Provides
    @Singleton
    fun provideGiftInteractor(giftRepository: GiftRepository): GiftInteractor {
        return GiftInteractor(giftRepository)
    }

    @Provides
    @Singleton
    fun provideGiftRepository(giftDataSourceImpl: GiftDataSourceImpl): GiftRepository {
        return GiftRepository(giftDataSourceImpl)
    }

    @Provides
    @Singleton
    fun provideCategoryDataSourceImpl(api: Api): CategoryDataSourceImpl {
        return CategoryDataSourceImpl(api)
    }

    @Provides
    @Singleton
    fun provideCategoryInteractor(categoryRepository: CategoryDataRepository): CategoryInteractor {
        return CategoryInteractor(categoryRepository)
    }

    @Provides
    @Singleton
    fun provideCategoryDataRepository(categoryDataSourceImpl: CategoryDataSourceImpl): CategoryDataRepository {
        return CategoryDataRepository(categoryDataSourceImpl)
    }
}