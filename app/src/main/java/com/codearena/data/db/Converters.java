package com.codearena.data.db;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Room TypeConverters for SQLite serialization of List<String>.
 */
public class Converters {
    private static final Gson gson = new Gson();

    @TypeConverter
    public static String fromStringList(List<String> list) {
        if (list == null) return "[]";
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<String> toStringList(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new ArrayList<>();
        }
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        try {
            List<String> result = gson.fromJson(value, listType);
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}

