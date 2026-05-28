package com.example.matchy_team_generator.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.matchy_team_generator.data.UserEntity;
import com.example.matchy_team_generator.databinding.ItemStudentBinding;
import com.example.matchy_team_generator.model.SkillName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {
    private final List<UserEntity> students = new ArrayList<>();
    private final Map<String, Map<String, Integer>> skillsByUser = new HashMap<>();
    private final OnStudentDeleteClick deleteClick;

    public StudentAdapter() {
        this(null);
    }

    public StudentAdapter(OnStudentDeleteClick deleteClick) {
        this.deleteClick = deleteClick;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStudentBinding binding = ItemStudentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new StudentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        UserEntity student = students.get(position);
        holder.binding.studentNameText.setText(student.name);
        holder.binding.studentEmailText.setText(student.email);
        holder.binding.studentSkillSummaryText.setText(skillSummary(student.id));
        holder.binding.deleteStudentButton.setVisibility(deleteClick == null ? View.GONE : View.VISIBLE);
        holder.binding.deleteStudentButton.setOnClickListener(v -> {
            if (deleteClick != null) {
                deleteClick.onDeleteStudent(student);
            }
        });
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public void submit(List<UserEntity> newStudents, Map<String, Map<String, Integer>> newSkillMap) {
        students.clear();
        if (newStudents != null) {
            students.addAll(newStudents);
        }
        skillsByUser.clear();
        if (newSkillMap != null) {
            skillsByUser.putAll(newSkillMap);
        }
        notifyDataSetChanged();
    }

    private String skillSummary(String userId) {
        Map<String, Integer> skills = skillsByUser.get(userId);
        if (skills == null || skills.isEmpty()) {
            return "No skills saved";
        }

        StringBuilder builder = new StringBuilder();
        for (String skill : SkillName.PLAIN_SKILLS) {
            Integer value = skills.get(skill);
            builder.append(label(skill)).append(": ").append(value == null ? "-" : value).append("  ");
        }
        return builder.toString().trim();
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

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        final ItemStudentBinding binding;

        StudentViewHolder(ItemStudentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnStudentDeleteClick {
        void onDeleteStudent(UserEntity student);
    }
}
