package com.lohni.musicplayer.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lohni.musicplayer.utils.enums.ListFilterType
import com.lohni.musicplayer.utils.enums.ListType

@Entity
data class DashboardListConfiguration(
    @PrimaryKey @ColumnInfo("dlc_id") var id: Int,
    @ColumnInfo("dlc_type") var listType: ListType,
    @ColumnInfo("dlc_filter") var listFilterType: ListFilterType,
    @ColumnInfo("dlc_size") var listSize: Int
)