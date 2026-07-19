package dev.franke.felipe.website_backend.controller;

import dev.franke.felipe.website_backend.dto.ProjectReadOnly;
import dev.franke.felipe.website_backend.dto.ProjectReadOnlyDetailed;
import dev.franke.felipe.website_backend.dto.ProjectRequest;
import dev.franke.felipe.website_backend.model.Project;
import dev.franke.felipe.website_backend.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@Slf4j
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService service;

    @GetMapping
    public List<ProjectReadOnly> listProjects() {
        log.trace("Entered the project controller and called to retrieve projects");
        List<ProjectReadOnly> projects = service.getProjects();
        log.trace("Returned list after service call: {}", projects);
        return projects;
    }

    @GetMapping("{id}")
    public ProjectReadOnlyDetailed retrieveProject(@PathVariable String id) {
        log.trace("Entered the project controller and called to retrieve project with ID: {}", id);
        Project project = service.getProject(id);
        log.trace("Returning single project after service call: {}", project);
        return service.getProjectDTO(project);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void newProject(@Valid @RequestBody ProjectRequest request) {
        log.trace("Entered method to save new Project");
        log.trace("Request: {}", request);
        service.saveProject(request);
        log.trace("Exiting method to create new project after service call");
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateExistingProject(@PathVariable String id, @Valid @RequestBody ProjectRequest request) {
        log.trace("Entered method to Update an existing project");
        log.trace("ID: {}", id);
        log.trace("Request to update: {}", request);
        Project project = service.getProject(id);
        log.trace("Retrieved project instance (service call). Result: {}", project);
        log.trace("Calling service method to update existing project");
        service.updateProject(project, request);
        log.trace("Called service method to update existing project");
        log.trace("Method to update existing project was executed");
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExistingProject(@PathVariable String id) {
        log.trace("Entered method to delete a project");
        log.trace("ID for the project to be deleted: {}", id);
        Project project = service.getProject(id);
        log.trace("Retrieved project instance to be deleted. Result: {}", project);
        log.trace("Calling service method to delete");
        service.deleteProject(project);
        log.trace("Called service method to delete");
        log.trace("Exiting method to delete a project");
    }
}
