package com.example.matchy_team_generator.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.matchy_team_generator.model.Role;

import java.util.List;

@Dao
public interface ProjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(UserEntity user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUsers(List<UserEntity> users);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUserSkill(UserSkillEntity skill);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUserSkills(List<UserSkillEntity> skills);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTeam(TeamEntity team);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTeams(List<TeamEntity> teams);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTeamUserCrossRefs(List<TeamUserCrossRef> refs);

    @Query("SELECT * FROM users ORDER BY name")
    LiveData<List<UserEntity>> getAllUsers();

    @Query("SELECT * FROM users WHERE role = :role ORDER BY name")
    LiveData<List<UserEntity>> getUsersByRole(Role role);

    @Query("SELECT * FROM users WHERE role = 'STUDENT' ORDER BY name")
    LiveData<List<UserEntity>> getAllStudents();

    @Query("SELECT * FROM user_skills WHERE userId = :userId ORDER BY skillName")
    LiveData<List<UserSkillEntity>> getSkillsForUser(String userId);

    @Query("SELECT * FROM user_skills")
    LiveData<List<UserSkillEntity>> getAllUserSkills();

    @Transaction
    @Query("SELECT * FROM teams ORDER BY teamName")
    LiveData<List<TeamWithMembers>> getTeamsWithMembers();

    @Query("DELETE FROM team_user_cross_ref")
    void deleteAllTeamMemberships();

    @Query("DELETE FROM teams")
    void deleteAllTeams();

    @Transaction
    default void insertStudentProfile(UserEntity student, List<UserSkillEntity> skills) {
        insertUser(student);
        insertUserSkills(skills);
    }

    @Transaction
    default void insertStudentProfiles(List<UserEntity> students, List<UserSkillEntity> skills) {
        insertUsers(students);
        insertUserSkills(skills);
    }

    @Transaction
    default void replaceTeams(List<TeamEntity> teams, List<TeamUserCrossRef> refs) {
        deleteAllTeamMemberships();
        deleteAllTeams();
        insertTeams(teams);
        insertTeamUserCrossRefs(refs);
    }
}
