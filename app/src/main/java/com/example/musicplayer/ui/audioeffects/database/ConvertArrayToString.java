package com.example.musicplayer.ui.audioeffects.database;

import java.util.Arrays;

import androidx.room.TypeConverter;

public class ConvertArrayToString {
    @TypeConverter
    public static String convertShortArrayToString(short[] array){
        return Arrays.toString(array);
    }

    @TypeConverter
    public static short[] convertStringToShortArray(String arrayString){
        arrayString = arrayString.substring(1, arrayString.length() - 1);
        String[] values = arrayString.split(", ");
        short[] finalValues = new short[values.length];
        for (int i = 0;i<values.length;i++){
            finalValues[i] = Short.parseShort(values[i]);
        }
        return finalValues;
    }

}
