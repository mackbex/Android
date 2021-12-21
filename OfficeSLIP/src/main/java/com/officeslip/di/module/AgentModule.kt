package com.officeslip.di.module

//import com.officeslip.data.remote.agent.AgentService
import com.officeslip.data.remote.agent.AgentDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AgentModule {
    @Singleton
    @Provides
    fun provideAgentDao() = AgentDao()

}

