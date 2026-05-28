package com.example.matchy_team_generator.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.matchy_team_generator.data.TeamWithMembers;
import com.example.matchy_team_generator.data.UserEntity;
import com.example.matchy_team_generator.databinding.ItemTeamBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder> {
    private final List<TeamWithMembers> teams = new ArrayList<>();

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTeamBinding binding = ItemTeamBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TeamViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        TeamWithMembers team = teams.get(position);
        int memberCount = team.members == null ? 0 : team.members.size();
        holder.binding.teamNameText.setText(team.team.teamName);
        holder.binding.teamMetaText.setText(String.format(
                Locale.US,
                "%s | %s | %d members",
                team.team.strategyUsed,
                team.team.criteriaUsed,
                memberCount
        ));
        holder.binding.teamMembersText.setText(memberNames(team.members));
    }

    @Override
    public int getItemCount() {
        return teams.size();
    }

    public void submit(List<TeamWithMembers> newTeams) {
        teams.clear();
        if (newTeams != null) {
            teams.addAll(newTeams);
        }
        notifyDataSetChanged();
    }

    private String memberNames(List<UserEntity> members) {
        if (members == null || members.isEmpty()) {
            return "No members assigned";
        }
        StringBuilder builder = new StringBuilder();
        for (UserEntity member : members) {
            builder.append(member.name).append(" <").append(member.email).append(">\n");
        }
        return builder.toString().trim();
    }

    static class TeamViewHolder extends RecyclerView.ViewHolder {
        final ItemTeamBinding binding;

        TeamViewHolder(ItemTeamBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
