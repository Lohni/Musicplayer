package com.lohni.musicplayer.database.converter

import androidx.room.TypeConverter
import com.lohni.musicplayer.utils.enums.ListFilterType
import com.lohni.musicplayer.utils.enums.ListType

class Converters {
    @TypeConverter
    fun fromListType(value: ListType): Int {
        return ListType.getIdFromListType(value)
    }

    @TypeConverter
    fun toListType(value: Int): ListType {
        return ListType.getListTypeById(value)
    }

    @TypeConverter
    fun fromListFilterType(value: ListFilterType): Int {
        return ListFilterType.getFilterTypeAsInt(value)
    }

    @TypeConverter
    fun toListFilterType(value: Int): ListFilterType {
        return ListFilterType.getListFilterTypeByInt(value)
    }
}