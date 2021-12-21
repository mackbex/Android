package com.officeslip.di.binding

import com.officeslip.data.remote.agent.AgentRepository
import com.officeslip.data.remote.agent.AgentRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AgentBinds{

    @Binds
    abstract fun bindAgent(data: AgentRepositoryImpl): AgentRepository
}