package com.example.matchy_team_generator;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.matchy_team_generator.data.TeamWithMembers;
import com.example.matchy_team_generator.data.UserEntity;
import com.example.matchy_team_generator.data.UserSkillEntity;
import com.example.matchy_team_generator.databinding.ActivityMainBinding;
import com.example.matchy_team_generator.model.GenerationStrategy;
import com.example.matchy_team_generator.model.SkillName;
import com.example.matchy_team_generator.ui.StudentAdapter;
import com.example.matchy_team_generator.ui.TeamAdapter;
import com.example.matchy_team_generator.viewmodel.ProjectViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private ProjectViewModel viewModel;
    private final StudentAdapter studentAdapter = new StudentAdapter();
    private final TeamAdapter teamAdapter = new TeamAdapter();
    private final Map<String, TextInputEditText> skillInputs = new HashMap<>();
    private final Map<String, Map<String, Integer>> skillsByUser = new HashMap<>();
    private List<UserEntity> currentStudents = new ArrayList<>();
    private List<UserSkillEntity> currentSkills = new ArrayList<>();
    private List<TeamWithMembers> currentTeams = new ArrayList<>();
    private boolean seedRequested;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(ProjectViewModel.class);
        setupSkillInputs();
        setupLists();
        setupClicks();
        observeData();
        showRoleChoice();
    }

    private void setupSkillInputs() {
        for (String skill : SkillName.PLAIN_SKILLS) {
            TextInputLayout layout = new TextInputLayout(this);
            layout.setHint(label(skill));
            TextInputEditText input = new TextInputEditText(layout.getContext());
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setText("3");
            input.setSelectAllOnFocus(true);
            layout.addView(input);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, dp(8));
            binding.studentSkillsContainer.addView(layout, params);
            skillInputs.put(skill, input);
        }
    }

    private void setupLists() {
        binding.studentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.studentsRecyclerView.setAdapter(studentAdapter);
        binding.teamsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.teamsRecyclerView.setAdapter(teamAdapter);
    }

    private void setupClicks() {
        binding.studentModeButton.setOnClickListener(v -> showStudentScreen());
        binding.professorModeButton.setOnClickListener(v -> showProfessorScreen());
        binding.studentBackButton.setOnClickListener(v -> showRoleChoice());
        binding.professorBackButton.setOnClickListener(v -> showRoleChoice());
        binding.saveStudentProfileButton.setOnClickListener(v -> saveStudentProfile());
        binding.viewAllStudentsButton.setOnClickListener(v -> showAllStudentsDialog());
        binding.viewAllTeamsButton.setOnClickListener(v -> showAllTeamsDialog());
        binding.generateTeamsButton.setOnClickListener(v -> generateTeams());
        binding.deleteTeamsButton.setOnClickListener(v -> {
            viewModel.deleteAllTeams();
            Toast.makeText(this, "All teams deleted", Toast.LENGTH_SHORT).show();
        });
    }

    private void observeData() {
        viewModel.students.observe(this, students -> {
            currentStudents = students == null ? new ArrayList<>() : students;
            if (currentStudents.size() < 120 && !seedRequested) {
                seedRequested = true;
                viewModel.seedDemoStudents(120);
                Toast.makeText(this, "Loading 120 demo students", Toast.LENGTH_SHORT).show();
            }
            binding.studentCountText.setText(String.format(Locale.US, "Students (%d)", currentStudents.size()));
            binding.studentListEmptyText.setVisibility(currentStudents.isEmpty() ? View.VISIBLE : View.GONE);
            binding.studentsRecyclerView.setVisibility(currentStudents.isEmpty() ? View.GONE : View.VISIBLE);
            studentAdapter.submit(sampleStudents(), skillsByUser);
        });

        viewModel.allSkills.observe(this, skills -> {
            currentSkills = skills == null ? new ArrayList<>() : skills;
            rebuildSkillMap();
            studentAdapter.submit(sampleStudents(), skillsByUser);
        });

        viewModel.teams.observe(this, teams -> {
            currentTeams = teams == null ? new ArrayList<>() : teams;
            binding.teamListEmptyText.setVisibility(currentTeams.isEmpty() ? View.VISIBLE : View.GONE);
            binding.teamsRecyclerView.setVisibility(currentTeams.isEmpty() ? View.GONE : View.VISIBLE);
            binding.viewAllTeamsButton.setVisibility(currentTeams.isEmpty() ? View.GONE : View.VISIBLE);
            teamAdapter.submit(sampleTeams());
        });
    }

    private void showRoleChoice() {
        binding.roleChoicePanel.setVisibility(View.VISIBLE);
        binding.studentScreen.setVisibility(View.GONE);
        binding.professorScreen.setVisibility(View.GONE);
        binding.subtitleText.setText(R.string.screen_subtitle);
    }

    private void showStudentScreen() {
        binding.roleChoicePanel.setVisibility(View.GONE);
        binding.studentScreen.setVisibility(View.VISIBLE);
        binding.professorScreen.setVisibility(View.GONE);
        binding.subtitleText.setText("Add one student account with skill scores.");
    }

    private void showProfessorScreen() {
        binding.roleChoicePanel.setVisibility(View.GONE);
        binding.studentScreen.setVisibility(View.GONE);
        binding.professorScreen.setVisibility(View.VISIBLE);
        binding.subtitleText.setText("Review a sample, generate teams, or open the full student list.");
    }

    private void saveStudentProfile() {
        String name = text(binding.studentNameEditText);
        String email = text(binding.studentEmailEditText);
        if (name.isEmpty()) {
            binding.studentNameLayout.setError("Name is required");
            return;
        }
        if (email.isEmpty() || !email.contains("@")) {
            binding.studentEmailLayout.setError("Valid email is required");
            return;
        }
        binding.studentNameLayout.setError(null);
        binding.studentEmailLayout.setError(null);

        Map<String, Integer> skillValues = new HashMap<>();
        for (String skill : SkillName.PLAIN_SKILLS) {
            int value = parseSkillValue(skillInputs.get(skill));
            if (value < 1 || value > 5) {
                Toast.makeText(this, "Each skill must be between 1 and 5", Toast.LENGTH_SHORT).show();
                return;
            }
            skillValues.put(skill, value);
        }

        viewModel.registerStudent(name, email, skillValues);
        clearStudentForm();
        Toast.makeText(this, "Student added", Toast.LENGTH_SHORT).show();
    }

    private void generateTeams() {
        if (currentStudents.isEmpty()) {
            Toast.makeText(this, "Add students before generating teams", Toast.LENGTH_SHORT).show();
            return;
        }

        int targetSize = parsePositiveInt(binding.teamSizeEditText);
        if (targetSize <= 0) {
            binding.teamSizeLayout.setError("Use a number greater than zero");
            return;
        }
        binding.teamSizeLayout.setError(null);

        String criteria = binding.criteriaSpinner.getSelectedItem().toString();
        GenerationStrategy strategy = binding.homogeneousRadioButton.isChecked()
                ? GenerationStrategy.HOMOGENEOUS
                : GenerationStrategy.BALANCED;
        viewModel.generateTeams(currentStudents, currentSkills, targetSize, criteria, strategy);
        Toast.makeText(this, "Teams generated", Toast.LENGTH_SHORT).show();
    }

    private void rebuildSkillMap() {
        skillsByUser.clear();
        for (UserSkillEntity skill : currentSkills) {
            Map<String, Integer> userSkills = skillsByUser.get(skill.userId);
            if (userSkills == null) {
                userSkills = new HashMap<>();
                skillsByUser.put(skill.userId, userSkills);
            }
            userSkills.put(skill.skillName, skill.proficiencyLevel);
        }
    }

    private List<UserEntity> sampleStudents() {
        int sampleSize = Math.min(8, currentStudents.size());
        return new ArrayList<>(currentStudents.subList(0, sampleSize));
    }

    private List<TeamWithMembers> sampleTeams() {
        int sampleSize = Math.min(4, currentTeams.size());
        return new ArrayList<>(currentTeams.subList(0, sampleSize));
    }

    private void showAllStudentsDialog() {
        LinearLayout content = dialogContent();
        TextInputEditText searchInput = addSearchInput(content, "Search students or skills");
        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        content.addView(recyclerView, recyclerParams());

        StudentAdapter dialogAdapter = new StudentAdapter();
        recyclerView.setAdapter(dialogAdapter);
        dialogAdapter.submit(currentStudents, skillsByUser);
        searchInput.addTextChangedListener(simpleTextWatcher(text -> dialogAdapter.submit(filterStudents(text), skillsByUser)));

        new AlertDialog.Builder(this)
                .setTitle(String.format(Locale.US, "All Students (%d)", currentStudents.size()))
                .setView(content)
                .setPositiveButton("Close", null)
                .show();
    }

    private void showAllTeamsDialog() {
        LinearLayout content = dialogContent();
        TextInputEditText searchInput = addSearchInput(content, "Search teams or members");
        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        content.addView(recyclerView, recyclerParams());

        TeamAdapter dialogAdapter = new TeamAdapter();
        recyclerView.setAdapter(dialogAdapter);
        dialogAdapter.submit(currentTeams);
        searchInput.addTextChangedListener(simpleTextWatcher(text -> dialogAdapter.submit(filterTeams(text))));

        new AlertDialog.Builder(this)
                .setTitle(String.format(Locale.US, "Generated Teams (%d)", currentTeams.size()))
                .setView(content)
                .setPositiveButton("Close", null)
                .show();
    }

    private LinearLayout dialogContent() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(4), dp(4), dp(4), 0);
        return content;
    }

    private TextInputEditText addSearchInput(LinearLayout content, String hint) {
        TextInputLayout searchLayout = new TextInputLayout(this);
        searchLayout.setHint(hint);
        TextInputEditText searchInput = new TextInputEditText(searchLayout.getContext());
        searchInput.setSingleLine(true);
        searchInput.setInputType(InputType.TYPE_CLASS_TEXT);
        searchLayout.addView(searchInput);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(10));
        content.addView(searchLayout, params);
        return searchInput;
    }

    private LinearLayout.LayoutParams recyclerParams() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(520)
        );
    }

    private TextWatcher simpleTextWatcher(SearchHandler handler) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.onSearch(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }

    private List<UserEntity> filterStudents(String query) {
        String normalized = query.trim().toLowerCase(Locale.US);
        if (normalized.isEmpty()) {
            return currentStudents;
        }

        List<UserEntity> filtered = new ArrayList<>();
        for (UserEntity student : currentStudents) {
            if (studentMatches(student, normalized)) {
                filtered.add(student);
            }
        }
        return filtered;
    }

    private boolean studentMatches(UserEntity student, String query) {
        if (student.name.toLowerCase(Locale.US).contains(query)
                || student.email.toLowerCase(Locale.US).contains(query)) {
            return true;
        }

        Map<String, Integer> skills = skillsByUser.get(student.id);
        if (skills == null) {
            return false;
        }
        for (Map.Entry<String, Integer> entry : skills.entrySet()) {
            String skillText = label(entry.getKey()).toLowerCase(Locale.US) + " " + entry.getValue();
            if (skillText.contains(query) || entry.getKey().toLowerCase(Locale.US).contains(query)) {
                return true;
            }
        }
        return false;
    }

    private List<TeamWithMembers> filterTeams(String query) {
        String normalized = query.trim().toLowerCase(Locale.US);
        if (normalized.isEmpty()) {
            return currentTeams;
        }

        List<TeamWithMembers> filtered = new ArrayList<>();
        for (TeamWithMembers team : currentTeams) {
            if (teamMatches(team, normalized)) {
                filtered.add(team);
            }
        }
        return filtered;
    }

    private boolean teamMatches(TeamWithMembers team, String query) {
        String teamText = (team.team.teamName + " "
                + team.team.strategyUsed + " "
                + team.team.criteriaUsed).toLowerCase(Locale.US);
        if (teamText.contains(query)) {
            return true;
        }
        if (team.members == null) {
            return false;
        }
        for (UserEntity member : team.members) {
            String memberText = (member.name + " " + member.email).toLowerCase(Locale.US);
            if (memberText.contains(query)) {
                return true;
            }
        }
        return false;
    }

    private interface SearchHandler {
        void onSearch(String query);
    }

    private void clearStudentForm() {
        binding.studentNameEditText.setText("");
        binding.studentEmailEditText.setText("");
        for (TextInputEditText input : skillInputs.values()) {
            input.setText("3");
        }
    }

    private int parseSkillValue(TextInputEditText input) {
        try {
            return Integer.parseInt(text(input));
        } catch (NumberFormatException error) {
            return -1;
        }
    }

    private int parsePositiveInt(TextInputEditText input) {
        try {
            return Integer.parseInt(text(input));
        } catch (NumberFormatException error) {
            return -1;
        }
    }

    private String text(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private String label(String raw) {
        String[] parts = raw.toLowerCase(Locale.US).split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.length() == 0) {
                continue;
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");
        }
        return builder.toString().trim();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
