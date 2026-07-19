package dev.franke.felipe.website_backend.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

import java.util.List;

public record ProjectRequest(
        @NotNull(message = "Please provide the project name")
        @NotBlank(message = "The project name cannot be blank")
        @Size(min = 5, max = 100, message = "The project name should have between 5 and 100 characters")
        String name,
        @NotNull(message = "Please provide the project description")
        @NotBlank(message = "The project description cannot be left blank")
        @Size(min = 5, max = 500, message = "The project description should have between 5 and 500 characters")
        String description,
        @NotEmpty(message = "The stack is required")
        List<String> stack,
        @URL(message = "Incorrect URL format for the github URL")
        @Pattern(
                regexp = "^https://github\\.com/.+",
                message = "The github URL must start with https://github.com/"
        )
        String githubURL,
        @URL(message = "Incorrect URL format for the demo URL")
        String demoURL,
        boolean featured
) {}
