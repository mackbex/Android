package com.example.practice.util

interface EntityMapper<Entity, DomainModel> {
    fun mapFromEntity(entity:Entity):DomainModel
    fun mapToEntity(model:DomainModel):Entity
}