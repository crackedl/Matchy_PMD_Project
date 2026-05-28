package com.example.matchy_team_generator.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "team_user_cross_ref",
        primaryKeys = {"teamId", "userId"},
        foreignKeys = {
                @ForeignKey(entity = TeamEntity.class, parentColumns = "id", childColumns = "teamId", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = UserEntity.class, parentColumns = "id", childColumns = "userId", onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("teamId"), @Index("userId")}
)
public class TeamUserCrossRef {
    @NonNull
    public String teamId;
    @NonNull
    public String userId;

    public TeamUserCrossRef(@NonNull String teamId, @NonNull String userId) {
        this.teamId = teamId;
        this.userId = userId;
    }
}
