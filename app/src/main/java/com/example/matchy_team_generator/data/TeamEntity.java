package com.example.matchy_team_generator.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.matchy_team_generator.model.GenerationStrategy;

@Entity(tableName = "teams")
public class TeamEntity {
    @PrimaryKey
    @NonNull
    public String id;
    public String teamName;
    public GenerationStrategy strategyUsed;
    public String criteriaUsed;

    public TeamEntity(@NonNull String id, String teamName, GenerationStrategy strategyUsed, String criteriaUsed) {
        this.id = id;
        this.teamName = teamName;
        this.strategyUsed = strategyUsed;
        this.criteriaUsed = criteriaUsed;
    }
}
