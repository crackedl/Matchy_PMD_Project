package com.example.matchy_team_generator.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.matchy_team_generator.data.ProjectDao;
import com.example.matchy_team_generator.data.ProjectDatabase;
import com.example.matchy_team_generator.data.TeamEntity;
import com.example.matchy_team_generator.data.TeamUserCrossRef;
import com.example.matchy_team_generator.data.TeamWithMembers;
import com.example.matchy_team_generator.data.UserEntity;
import com.example.matchy_team_generator.data.UserSkillEntity;
import com.example.matchy_team_generator.model.Role;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProjectRepository {
    private final ProjectDao projectDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ProjectRepository(Application application) {
        ProjectDatabase database = ProjectDatabase.getInstance(application);
        projectDao = database.projectDao();
    }

    public LiveData<List<UserEntity>> getAllUsers() {
        return projectDao.getAllUsers();
    }

    public LiveData<List<UserEntity>> getUsersByRole(Role role) {
        return projectDao.getUsersByRole(role);
    }

    public LiveData<List<UserEntity>> getAllStudents() {
        return projectDao.getAllStudents();
    }

    public LiveData<List<UserSkillEntity>> getAllUserSkills() {
        return projectDao.getAllUserSkills();
    }

    public LiveData<List<UserSkillEntity>> getSkillsForUser(String userId) {
        return projectDao.getSkillsForUser(userId);
    }

    public LiveData<List<TeamWithMembers>> getTeamsWithMembers() {
        return projectDao.getTeamsWithMembers();
    }

    public void insertUser(UserEntity user) {
        executorService.execute(() -> projectDao.insertUser(user));
    }

    public void insertUsers(List<UserEntity> users) {
        executorService.execute(() -> projectDao.insertUsers(users));
    }

    public void insertUserSkill(UserSkillEntity skill) {
        executorService.execute(() -> projectDao.insertUserSkill(skill));
    }

    public void insertUserSkills(List<UserSkillEntity> skills) {
        executorService.execute(() -> projectDao.insertUserSkills(skills));
    }

    public void insertStudentProfile(UserEntity student, List<UserSkillEntity> skills) {
        executorService.execute(() -> projectDao.insertStudentProfile(student, skills));
    }

    public void replaceGeneratedTeams(List<TeamEntity> teams, List<TeamUserCrossRef> refs) {
        executorService.execute(() -> projectDao.replaceTeams(teams, refs));
    }

    public void deleteAllTeams() {
        executorService.execute(() -> {
            projectDao.deleteAllTeamMemberships();
            projectDao.deleteAllTeams();
        });
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
