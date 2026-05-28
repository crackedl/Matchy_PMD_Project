package com.example.matchy_team_generator.data;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

public class TeamWithMembers {
    @Embedded
    public TeamEntity team;

    @Relation(
            parentColumn = "id",
            entityColumn = "id",
            associateBy = @Junction(
                    value = TeamUserCrossRef.class,
                    parentColumn = "teamId",
                    entityColumn = "userId"
            )
    )
    public List<UserEntity> members;
}
