package com.example.matchy_team_generator.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.matchy_team_generator.model.Role;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey
    @NonNull
    public String id;
    public String name;
    public String email;
    public Role role;

    public UserEntity(@NonNull String id, String name, String email, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }
}
