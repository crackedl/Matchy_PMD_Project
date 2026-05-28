package com.example.matchy_team_generator.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "user_skills",
        primaryKeys = {"userId", "skillName"},
        foreignKeys = @ForeignKey(
                entity = UserEntity.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("userId")}
)
public class UserSkillEntity {
    @NonNull
    public String userId;
    @NonNull
    public String skillName;
    public int proficiencyLevel;

    public UserSkillEntity(@NonNull String userId, @NonNull String skillName, int proficiencyLevel) {
        this.userId = userId;
        this.skillName = skillName;
        this.proficiencyLevel = proficiencyLevel;
    }
}
