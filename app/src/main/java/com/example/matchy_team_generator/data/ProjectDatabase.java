package com.example.matchy_team_generator.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(
        entities = {
                UserEntity.class,
                UserSkillEntity.class,
                TeamEntity.class,
                TeamUserCrossRef.class
        },
        version = 1,
        exportSchema = false
)
@TypeConverters(Converters.class)
public abstract class ProjectDatabase extends RoomDatabase {
    private static volatile ProjectDatabase INSTANCE;

    public abstract ProjectDao projectDao();

    public static ProjectDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ProjectDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    ProjectDatabase.class,
                                    "matchy_team_generator.db"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
