package com.lohni.musicplayer.utils.enums

import com.lohni.musicplayer.R

enum class ListFilterType {
    FAVOURITE, LAST_PLAYED, TIMES_PLAYED, ALPHABETICAL, TIME_PLAYED, LAST_CREATED;

    companion object {
        fun getFilterTypeAsString(listFilterType: ListFilterType?) : String {
            return when (listFilterType) {
                FAVOURITE -> "FAV"
                LAST_PLAYED -> "LAST"
                TIMES_PLAYED -> "TIMES"
                ALPHABETICAL -> "A-Z"
                TIME_PLAYED -> "TIME"
                LAST_CREATED -> "CREATE"
                else -> ""
            }
        }

        fun getFilterTypeAsInt(listFilterType: ListFilterType?) : Int {
            return when (listFilterType) {
                FAVOURITE -> 0
                LAST_PLAYED -> 1
                TIMES_PLAYED -> 2
                ALPHABETICAL -> 3
                TIME_PLAYED -> 4
                LAST_CREATED -> 6
                else -> 3
            }
        }

        fun getListFilterTypeByInt(type: Int): ListFilterType {
            return when (type) {
                0 -> FAVOURITE
                1 -> LAST_PLAYED
                3 -> ALPHABETICAL
                4 -> TIME_PLAYED
                5 -> LAST_CREATED
                else -> TIMES_PLAYED
            }
        }

        fun getTitleForFilterType(filterType: ListFilterType?): String? {
            return when (filterType) {
                FAVOURITE -> "Favourite"
                LAST_PLAYED -> "Last played"
                ALPHABETICAL -> "Alphabetical"
                TIME_PLAYED -> "Time played"
                LAST_CREATED -> "Last created"
                else -> "Times played"
            }
        }

        fun getDrawableForFilterType(filterType: ListFilterType): Int {
            return when (filterType) {
                FAVOURITE -> R.drawable.ic_filter_az
                LAST_PLAYED -> R.drawable.ic_filter_last
                ALPHABETICAL -> R.drawable.ic_filter_az
                TIME_PLAYED -> R.drawable.ic_filter_time
                LAST_CREATED -> R.drawable.ic_filter_create
                else -> R.drawable.ic_filter_times
            }
        }
    }
}