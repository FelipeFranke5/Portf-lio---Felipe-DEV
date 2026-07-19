package dev.franke.felipe.website_backend.dto;

import lombok.Getter;

@Getter
public enum SkillLevel {

    ZERO_EXPERIENCE_STILL_LEARNING(1),
    SOME_EXPERIENCE_STILL_LEARNING(2),
    INTERMEDIATE_KNOWLEDGE(3),
    ADVANCED_KNOWLEDGE(4),
    WORK_EXPERIENCE(5);

    private final String description;
    private final int level;

    SkillLevel(int level) {
        this.level = level;
        this.description = calculateDescription(level);
    }

    private String calculateDescription(int level) {
        return switch (level) {
            case 1 -> "Zero Experience - Still Learning";
            case 2 -> "Some Experience - Still Learning";
            case 3 -> "Has intermediate knowledge about the topic";
            case 4 -> "Has advanced knowledge about the topic, but no work experience";
            case 5 -> "Has advanced knowledge about the topic and work experience";
            default -> "Unknown";
        };
    }

    @Override
    public String toString() {
        return this.description;
    }

    /**
     * Resolves a SkillLevel from its numeric level (1-5).
     * NOTE: {@code SkillLevel.valueOf(String)} cannot be used for this purpose, since
     * {@code valueOf} expects the ENUM CONSTANT NAME (e.g. "WORK_EXPERIENCE"), not the
     * numeric level. Using {@code valueOf(String.valueOf(level))} always throws
     * {@link IllegalArgumentException}, since no constant is literally named "1", "2", etc.
     */
    public static SkillLevel fromLevel(int level) {
        for (SkillLevel skillLevel : values()) {
            if (skillLevel.getLevel() == level) {
                return skillLevel;
            }
        }
        throw new IllegalArgumentException("No SkillLevel constant found for level: " + level);
    }
}
