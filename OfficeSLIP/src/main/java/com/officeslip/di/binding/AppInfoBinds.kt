package com.officeslip.di.binding

import com.officeslip.data.local.AppInfoRepository
import com.officeslip.data.local.AppInfoRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class AppInfoBinds {

    @Binds
    abstract fun bindLocalDataScore(data: AppInfoRepositoryImpl): AppInfoRepository

}