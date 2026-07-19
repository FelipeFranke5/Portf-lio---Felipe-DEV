package dev.franke.felipe.website_backend.controller;

import dev.franke.felipe.website_backend.dto.SkillDTO;
import dev.franke.felipe.website_backend.dto.SkillRequest;
import dev.franke.felipe.website_backend.service.SkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@Slf4j
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    @GetMapping
    public List<SkillDTO> getSkills() {
        log.info("Entered getSkills from SkillController");
        return skillService.getSkills();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void registerSkill(@Valid @RequestBody SkillRequest skillRequest) {
        log.info("Entered method to create new Skill from SkillController. Request: {}", skillRequest);
        skillService.saveSkill(skillRequest);
        log.trace("Skill has been created successfully");
    }

    @GetMapping("{id}")
    public SkillDTO retrieveSkill(@PathVariable String id) {
        log.info("Entered retrieveSkill from SkillController. ID: {}", id);
        return skillService.getSkillById(id);
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateSkill(@PathVariable String id, @Valid @RequestBody SkillRequest skillRequest) {
        log.info("Entered method to update existing Skill from SkillController. ID: {}", id);
        skillService.updateSkill(id, skillRequest);
        log.trace("Skill has been updated");
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSkill(@PathVariable String id) {
        log.info("Entered method to delete existing Skill from SkillController. ID: {}", id);
        skillService.deleteSkill(id);
        log.trace("Skill has been deleted");
    }
}
