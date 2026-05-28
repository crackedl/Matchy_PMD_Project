package com.example.matchy_team_generator.util;

import com.example.matchy_team_generator.data.UserEntity;
import com.example.matchy_team_generator.data.UserSkillEntity;
import com.example.matchy_team_generator.model.Role;
import com.example.matchy_team_generator.model.SkillName;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public final class DemoDataSeeder {
    private static final String[] FIRST_NAMES = {
            "Alex", "Maria", "Andrei", "Ioana", "Vlad", "Elena", "Mihai", "Ana", "Radu", "Diana",
            "Stefan", "Irina"
    };

    private static final String[] LAST_NAMES = {
            "Popescu", "Ionescu", "Stan", "Dima", "Marin", "Tudor", "Dobre", "Matei", "Nica", "Rusu"
    };

    private DemoDataSeeder() {
    }

    public static class DemoStudents {
        public final List<UserEntity> students;
        public final List<UserSkillEntity> skills;

        public DemoStudents(List<UserEntity> students, List<UserSkillEntity> skills) {
            this.students = students;
            this.skills = skills;
        }
    }

    public static DemoStudents createStudents(int count) {
        List<UserEntity> students = new ArrayList<>();
        List<UserSkillEntity> skills = new ArrayList<>();
        Random random = new Random(2026);

        for (int i = 0; i < count; i++) {
            String firstName = FIRST_NAMES[i % FIRST_NAMES.length];
            String lastName = LAST_NAMES[(i / FIRST_NAMES.length) % LAST_NAMES.length];
            String id = String.format(Locale.US, "demo.student.%03d@university.test", i + 1);
            String name = String.format(Locale.US, "%s %s %03d", firstName, lastName, i + 1);
            students.add(new UserEntity(id, name, id, Role.STUDENT));

            int profile = i % 6;
            for (String skill : SkillName.PLAIN_SKILLS) {
                int score = baseScore(profile, skill) + random.nextInt(3) - 1;
                skills.add(new UserSkillEntity(id, skill, clamp(score)));
            }
        }

        return new DemoStudents(students, skills);
    }

    private static int baseScore(int profile, String skill) {
        switch (profile) {
            case 0:
                return technicalBuilder(skill);
            case 1:
                return securityNetworker(skill);
            case 2:
                return dataScientist(skill);
            case 3:
                return hardwareScientist(skill);
            case 4:
                return teamOrganizer(skill);
            default:
                return balancedGeneralist(skill);
        }
    }

    private static int technicalBuilder(String skill) {
        if (SkillName.PROGRAMMING_LANGUAGES.equals(skill) || SkillName.ALGORITHMIC_THINKING.equals(skill)) {
            return 5;
        }
        if (SkillName.MATHEMATICS.equals(skill) || SkillName.CRITICAL_THINKING.equals(skill)) {
            return 4;
        }
        return 3;
    }

    private static int securityNetworker(String skill) {
        if (SkillName.SYSTEM_SECURITY.equals(skill) || SkillName.NETWORK_ARCHITECTURE.equals(skill)) {
            return 5;
        }
        if (SkillName.HARDWARE_INFRASTRUCTURE.equals(skill) || SkillName.CRITICAL_THINKING.equals(skill)) {
            return 4;
        }
        return 3;
    }

    private static int dataScientist(String skill) {
        if (SkillName.DATA_SCIENCE_STATISTICS.equals(skill) || SkillName.MATHEMATICS.equals(skill)) {
            return 5;
        }
        if (SkillName.PROGRAMMING_LANGUAGES.equals(skill) || SkillName.ALGORITHMIC_THINKING.equals(skill)) {
            return 4;
        }
        return 3;
    }

    private static int hardwareScientist(String skill) {
        if (SkillName.HARDWARE_INFRASTRUCTURE.equals(skill) || SkillName.PHYSICS.equals(skill)) {
            return 5;
        }
        if (SkillName.NETWORK_ARCHITECTURE.equals(skill) || SkillName.MATHEMATICS.equals(skill)) {
            return 4;
        }
        return 3;
    }

    private static int teamOrganizer(String skill) {
        if (SkillName.TEAM_COLLABORATION.equals(skill) || SkillName.TIME_MANAGEMENT.equals(skill)) {
            return 5;
        }
        if (SkillName.ADAPTABILITY.equals(skill) || SkillName.CRITICAL_THINKING.equals(skill)) {
            return 4;
        }
        return 2;
    }

    private static int balancedGeneralist(String skill) {
        if (SkillName.ADAPTABILITY.equals(skill) || SkillName.CRITICAL_THINKING.equals(skill)) {
            return 4;
        }
        return 3;
    }

    private static int clamp(int value) {
        return Math.max(1, Math.min(5, value));
    }
}
