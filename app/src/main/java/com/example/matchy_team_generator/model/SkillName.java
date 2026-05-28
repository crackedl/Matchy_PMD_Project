package com.example.matchy_team_generator.model;

import java.util.Arrays;
import java.util.List;

public final class SkillName {
    public static final String PROGRAMMING_LANGUAGES = "PROGRAMMING_LANGUAGES";
    public static final String SYSTEM_SECURITY = "SYSTEM_SECURITY";
    public static final String HARDWARE_INFRASTRUCTURE = "HARDWARE_INFRASTRUCTURE";
    public static final String NETWORK_ARCHITECTURE = "NETWORK_ARCHITECTURE";
    public static final String ALGORITHMIC_THINKING = "ALGORITHMIC_THINKING";
    public static final String MATHEMATICS = "MATHEMATICS";
    public static final String PHYSICS = "PHYSICS";
    public static final String DATA_SCIENCE_STATISTICS = "DATA_SCIENCE_STATISTICS";
    public static final String TEAM_COLLABORATION = "TEAM_COLLABORATION";
    public static final String CRITICAL_THINKING = "CRITICAL_THINKING";
    public static final String ADAPTABILITY = "ADAPTABILITY";
    public static final String TIME_MANAGEMENT = "TIME_MANAGEMENT";
    public static final String OVERALL_AVERAGE = "OVERALL_AVERAGE";

    public static final List<String> PLAIN_SKILLS = Arrays.asList(
            PROGRAMMING_LANGUAGES,
            SYSTEM_SECURITY,
            HARDWARE_INFRASTRUCTURE,
            NETWORK_ARCHITECTURE,
            ALGORITHMIC_THINKING,
            MATHEMATICS,
            PHYSICS,
            DATA_SCIENCE_STATISTICS,
            TEAM_COLLABORATION,
            CRITICAL_THINKING,
            ADAPTABILITY,
            TIME_MANAGEMENT
    );

    private SkillName() {
    }

    public static boolean isValidCriteria(String criteria) {
        return OVERALL_AVERAGE.equals(criteria) || PLAIN_SKILLS.contains(criteria);
    }
}
