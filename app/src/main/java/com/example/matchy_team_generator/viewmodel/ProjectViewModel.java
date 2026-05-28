package com.example.matchy_team_generator.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.matchy_team_generator.data.TeamWithMembers;
import com.example.matchy_team_generator.data.UserEntity;
import com.example.matchy_team_generator.data.UserSkillEntity;
import com.example.matchy_team_generator.model.GenerationStrategy;
import com.example.matchy_team_generator.model.Role;
import com.example.matchy_team_generator.repository.ProjectRepository;
import com.example.matchy_team_generator.util.TeamGeneratorHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProjectViewModel extends AndroidViewModel {
    private final ProjectRepository repository;
    public final LiveData<List<UserEntity>> students;
    public final LiveData<List<UserSkillEntity>> allSkills;
    public final LiveData<List<TeamWithMembers>> teams;

    public ProjectViewModel(@NonNull Application application) {
        super(application);
        repository = new ProjectRepository(application);
        students = repository.getUsersByRole(Role.STUDENT);
        allSkills = repository.getAllUserSkills();
        teams = repository.getTeamsWithMembers();
    }

    public String registerStudent(String name, String email, Map<String, Integer> skillValues) {
        String id = email.trim().toLowerCase(Locale.US);
        UserEntity student = new UserEntity(id, name.trim(), email.trim(), Role.STUDENT);
        List<UserSkillEntity> skills = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : skillValues.entrySet()) {
            skills.add(new UserSkillEntity(id, entry.getKey(), entry.getValue()));
        }
        repository.insertStudentProfile(student, skills);
        return id;
    }

    public void generateTeams(List<UserEntity> currentStudents, List<UserSkillEntity> currentSkills, int targetSize, String criteria, GenerationStrategy strategy) {
        TeamGeneratorHelper.GeneratedTeams generated = TeamGeneratorHelper.generateTeams(
                currentStudents,
                currentSkills,
                targetSize,
                criteria,
                strategy
        );
        repository.replaceGeneratedTeams(generated.teams, generated.teamMembers);
    }

    public void deleteAllTeams() {
        repository.deleteAllTeams();
    }

    @Override
    protected void onCleared() {
        repository.shutdown();
        super.onCleared();
    }
}
