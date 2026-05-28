package com.example.matchy_team_generator.util;

import com.example.matchy_team_generator.data.TeamEntity;
import com.example.matchy_team_generator.data.TeamUserCrossRef;
import com.example.matchy_team_generator.data.UserEntity;
import com.example.matchy_team_generator.data.UserSkillEntity;
import com.example.matchy_team_generator.model.GenerationStrategy;
import com.example.matchy_team_generator.model.SkillName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class TeamGeneratorHelper {
    private TeamGeneratorHelper() {
    }

    public static class GeneratedTeams {
        public final List<TeamEntity> teams;
        public final List<TeamUserCrossRef> teamMembers;
        public final List<List<UserEntity>> groupedStudents;

        public GeneratedTeams(List<TeamEntity> teams, List<TeamUserCrossRef> teamMembers, List<List<UserEntity>> groupedStudents) {
            this.teams = teams;
            this.teamMembers = teamMembers;
            this.groupedStudents = groupedStudents;
        }
    }

    public static GeneratedTeams generateTeams(
            List<UserEntity> students,
            List<UserSkillEntity> skills,
            int targetStudentsPerTeam,
            String criteria,
            GenerationStrategy strategy
    ) {
        if (students == null || students.isEmpty()) {
            return new GeneratedTeams(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }
        if (targetStudentsPerTeam <= 0) {
            throw new IllegalArgumentException("Target students per team must be greater than zero.");
        }
        if (!SkillName.isValidCriteria(criteria)) {
            throw new IllegalArgumentException("Unknown sorting criteria: " + criteria);
        }

        Map<String, Map<String, Integer>> skillMap = buildSkillMap(skills);
        List<UserEntity> sortedStudents = new ArrayList<>(students);
        Collections.sort(sortedStudents, scoreComparator(skillMap, criteria));

        List<Integer> teamSizes = calculateTeamSizes(students.size(), targetStudentsPerTeam);
        List<List<UserEntity>> grouped = strategy == GenerationStrategy.BALANCED
                ? buildBalancedTeams(sortedStudents, teamSizes)
                : buildHomogeneousTeams(sortedStudents, teamSizes);

        List<TeamEntity> teams = new ArrayList<>();
        List<TeamUserCrossRef> refs = new ArrayList<>();
        for (int i = 0; i < grouped.size(); i++) {
            String teamId = UUID.randomUUID().toString();
            teams.add(new TeamEntity(teamId, String.format(Locale.US, "Team %d", i + 1), strategy, criteria));
            for (UserEntity member : grouped.get(i)) {
                refs.add(new TeamUserCrossRef(teamId, member.id));
            }
        }

        return new GeneratedTeams(teams, refs, grouped);
    }

    public static List<Integer> calculateTeamSizes(int studentCount, int targetStudentsPerTeam) {
        if (studentCount <= 0) {
            return new ArrayList<>();
        }
        if (targetStudentsPerTeam <= 0) {
            throw new IllegalArgumentException("Target students per team must be greater than zero.");
        }

        int teamCount = studentCount / targetStudentsPerTeam;
        if (teamCount == 0) {
            teamCount = 1;
        }

        int baseSize = studentCount / teamCount;
        int remainder = studentCount % teamCount;
        List<Integer> sizes = new ArrayList<>();
        for (int i = 0; i < teamCount; i++) {
            sizes.add(baseSize + (i < remainder ? 1 : 0));
        }
        return sizes;
    }

    private static List<List<UserEntity>> buildHomogeneousTeams(List<UserEntity> sortedStudents, List<Integer> teamSizes) {
        List<List<UserEntity>> teams = emptyTeams(teamSizes.size());
        int studentIndex = 0;
        for (int teamIndex = 0; teamIndex < teamSizes.size(); teamIndex++) {
            for (int j = 0; j < teamSizes.get(teamIndex) && studentIndex < sortedStudents.size(); j++) {
                teams.get(teamIndex).add(sortedStudents.get(studentIndex++));
            }
        }
        return teams;
    }

    private static List<List<UserEntity>> buildBalancedTeams(List<UserEntity> sortedStudents, List<Integer> teamSizes) {
        List<List<UserEntity>> teams = emptyTeams(teamSizes.size());
        int teamIndex = 0;
        int direction = 1;

        for (UserEntity student : sortedStudents) {
            while (teams.get(teamIndex).size() >= teamSizes.get(teamIndex)) {
                int next = teamIndex + direction;
                if (next < 0 || next >= teams.size()) {
                    direction *= -1;
                    next = teamIndex + direction;
                }
                teamIndex = next;
            }

            teams.get(teamIndex).add(student);

            int next = teamIndex + direction;
            if (next < 0 || next >= teams.size()) {
                direction *= -1;
                next = teamIndex + direction;
            }
            if (next >= 0 && next < teams.size()) {
                teamIndex = next;
            }
        }
        return teams;
    }

    private static List<List<UserEntity>> emptyTeams(int teamCount) {
        List<List<UserEntity>> teams = new ArrayList<>();
        for (int i = 0; i < teamCount; i++) {
            teams.add(new ArrayList<>());
        }
        return teams;
    }

    private static Comparator<UserEntity> scoreComparator(Map<String, Map<String, Integer>> skillMap, String criteria) {
        return (left, right) -> {
            double rightScore = scoreFor(right.id, skillMap, criteria);
            double leftScore = scoreFor(left.id, skillMap, criteria);
            int scoreCompare = Double.compare(rightScore, leftScore);
            if (scoreCompare != 0) {
                return scoreCompare;
            }
            return left.name.compareToIgnoreCase(right.name);
        };
    }

    private static double scoreFor(String userId, Map<String, Map<String, Integer>> skillMap, String criteria) {
        Map<String, Integer> userSkills = skillMap.get(userId);
        if (userSkills == null || userSkills.isEmpty()) {
            return 0;
        }
        if (!SkillName.OVERALL_AVERAGE.equals(criteria)) {
            Integer value = userSkills.get(criteria);
            return value == null ? 0 : value;
        }

        double total = 0;
        for (String skill : SkillName.PLAIN_SKILLS) {
            Integer value = userSkills.get(skill);
            total += value == null ? 0 : value;
        }
        return total / SkillName.PLAIN_SKILLS.size();
    }

    private static Map<String, Map<String, Integer>> buildSkillMap(List<UserSkillEntity> skills) {
        Map<String, Map<String, Integer>> skillMap = new HashMap<>();
        if (skills == null) {
            return skillMap;
        }
        for (UserSkillEntity skill : skills) {
            Map<String, Integer> userSkills = skillMap.get(skill.userId);
            if (userSkills == null) {
                userSkills = new HashMap<>();
                skillMap.put(skill.userId, userSkills);
            }
            userSkills.put(skill.skillName, clamp(skill.proficiencyLevel));
        }
        return skillMap;
    }

    private static int clamp(int value) {
        return Math.max(1, Math.min(5, value));
    }
}
