package com.vm.coinfold.app.feature.expenses.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Long,
    val icon: String,
    // "order" is a reserved SQL word
    val sortOrder: Int,
    val isArchived: Boolean = false,
)
