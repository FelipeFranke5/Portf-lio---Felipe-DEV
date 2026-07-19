package dev.franke.felipe.website_backend.dto;

import java.util.List;
import java.util.Map;

public record UnprocessableEntityResponse(String message, List<Map<String, List<String>>> errors) {
}
