package com.example.matchy_team_generator.data;

import androidx.room.TypeConverter;

import com.example.matchy_team_generator.model.GenerationStrategy;
import com.example.matchy_team_generator.model.Role;

public class Converters {
    @TypeConverter
    public static String fromRole(Role role) {
        return role == null ? null : role.name();
    }

    @TypeConverter
    public static Role toRole(String value) {
        return value == null ? null : Role.valueOf(value);
    }

    @TypeConverter
    public static String fromGenerationStrategy(GenerationStrategy strategy) {
        return strategy == null ? null : strategy.name();
    }

    @TypeConverter
    public static GenerationStrategy toGenerationStrategy(String value) {
        return value == null ? null : GenerationStrategy.valueOf(value);
    }
}
