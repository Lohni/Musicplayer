package com.lohni.musicplayer.database.dto

import androidx.room.ColumnInfo
import com.lohni.musicplayer.database.entity.*

class ItemPlayedDTO(
    @ColumnInfo("id") val id: Int,
    @ColumnInfo("title") val title: String,
    @ColumnInfo("subtitle") val subTitle: String,
    @ColumnInfo("timeplayed") val timePlayed: Long,
    @ColumnInfo("credat") val credat: String,
    @ColumnInfo("type") val type: Int,
    @ColumnInfo("refid") val refId: Int,
    @ColumnInfo("reftype") val refType: Int
)
