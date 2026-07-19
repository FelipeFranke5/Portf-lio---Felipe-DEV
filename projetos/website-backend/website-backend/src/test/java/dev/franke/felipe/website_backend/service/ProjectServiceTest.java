package dev.franke.felipe.website_backend.service;

import dev.franke.felipe.website_backend.dto.ProjectReadOnly;
import dev.franke.felipe.website_backend.dto.ProjectReadOnlyDetailed;
import dev.franke.felipe.website_backend.dto.ProjectRequest;
import dev.franke.felipe.website_backend.exception.ProjectException;
import dev.franke.felipe.website_backend.exception.ProjectNotFoundException;
import dev.franke.felipe.website_backend.model.Project;
import dev.franke.felipe.website_backend.repository.ProjectRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static dev.franke.felipe.website_backend.service.ProjectService.DESCRIPTION_MAX_LENGTH;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock
    private ProjectRepository repository;

    @InjectMocks
    private ProjectService service;

    private String getLongDescription() {
        return "a".repeat(600);
    }

    private String getShortDescription() {
        return getLongDescription().substring(0, DESCRIPTION_MAX_LENGTH) + "...";
    }

    private List<Project> getMockProjects() {
        List<String> stack = List.of("Java", "Angular", "PostgreSQL");
        String nullUrl = null;
        String notNullUrl = "https://github.com/abcde";
        boolean notFeatured = false;
        boolean featured = true;
        return List.of(
                new Project(
                        UUID.randomUUID(),
                        "Test Project 1  - Short Description",
                        "Test Description",
                        stack,
                        nullUrl,
                        nullUrl,
                        notFeatured,
                        LocalDateTime.now()
                ),
                new Project(
                        UUID.randomUUID(),
                        "Test Project 2 - Long Description",
                        getLongDescription(),
                        stack,
                        nullUrl,
                        nullUrl,
                        notFeatured,
                        LocalDateTime.now()
                ),
                new Project(
                        UUID.randomUUID(),
                        "Test Project 3 - With URL",
                        "Test Description",
                        stack,
                        notNullUrl,
                        notNullUrl,
                        featured,
                        LocalDateTime.now()
                ),
                new Project(
                        UUID.randomUUID(),
                        "Test Project 4",
                        "Test Description",
                        stack,
                        nullUrl,
                        notNullUrl,
                        notFeatured,
                        LocalDateTime.now()
                )
        );
    }

    private Project buildProjectWithDescription(String description) {
        return new Project(
                UUID.randomUUID(),
                "Project with test description",
                description,
                List.of("Java"),
                null,
                null,
                false,
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("When there are no results in the Database, calling getProjects must return empty list")
    void emptyResults() {
        when(repository.findAll()).thenReturn(List.of());
        List<ProjectReadOnly> actualResult = service.getProjects();
        assertTrue(actualResult.isEmpty());
    }

    @Test
    @DisplayName(
            "When there are results in the Database, calling " +
            "getProjects must return the correct List of ProjectReadOnly"
    )
    void notEmptyResults() {
        List<Project> mockedProjects = getMockProjects();
        ProjectReadOnly projectReadOnly1 = new ProjectReadOnly(
                mockedProjects.getFirst().getId(),
                mockedProjects.getFirst().getName(),
                mockedProjects.getFirst().getDescription(),
                mockedProjects.getFirst().getStack()
        );
        ProjectReadOnly projectReadOnly2 = new ProjectReadOnly(
                mockedProjects.get(1).getId(),
                mockedProjects.get(1).getName(),
                getShortDescription(),
                mockedProjects.get(1).getStack()
        );
        ProjectReadOnly projectReadOnly3 = new ProjectReadOnly(
                mockedProjects.get(2).getId(),
                mockedProjects.get(2).getName(),
                mockedProjects.get(2).getDescription(),
                mockedProjects.get(2).getStack()
        );
        ProjectReadOnly projectReadOnly4 = new ProjectReadOnly(
                mockedProjects.get(3).getId(),
                mockedProjects.get(3).getName(),
                mockedProjects.get(3).getDescription(),
                mockedProjects.get(3).getStack()
        );
        List<ProjectReadOnly> expectedResult = List.of(
                projectReadOnly1,
                projectReadOnly2,
                projectReadOnly3,
                projectReadOnly4
        );
        when(repository.findAll()).thenReturn(mockedProjects);
        List<ProjectReadOnly> actualResult = service.getProjects();
        assertFalse(actualResult.isEmpty());
        assertEquals(4, actualResult.size());
        assertEquals(expectedResult, actualResult);
    }

    @Test
    @DisplayName("Given a description with exactly 50 characters (the limit), getProjects must NOT truncate it")
    void descriptionAtExactLimitIsNotTruncated() {
        String description50Chars = "a".repeat(DESCRIPTION_MAX_LENGTH);
        Project project = buildProjectWithDescription(description50Chars);
        when(repository.findAll()).thenReturn(List.of(project));

        List<ProjectReadOnly> result = service.getProjects();

        assertEquals(50, result.getFirst().description().length());
        assertEquals(description50Chars, result.getFirst().description());
    }

    @Test
    @DisplayName(
            "Given a description with 51 characters (one over the limit), " +
            "getProjects must truncate it to 50 characters"
    )
    void descriptionOverLimitIsTruncated() {
        String description51Chars = "a".repeat(DESCRIPTION_MAX_LENGTH + 1);
        Project project = buildProjectWithDescription(description51Chars);
        when(repository.findAll()).thenReturn(List.of(project));

        List<ProjectReadOnly> result = service.getProjects();

        assertEquals(53, result.getFirst().description().length());
    }

    @Test
    @DisplayName("Given a project with a null description, getProjects must not throw and must keep description null")
    void nullDescriptionIsKeptAsNull() {
        Project project = buildProjectWithDescription(null);
        when(repository.findAll()).thenReturn(List.of(project));
        List<ProjectReadOnly> result = assertDoesNotThrow(() -> service.getProjects());
        assertNull(result.getFirst().description());
    }

    @Test
    @DisplayName("Given an ID that is not a valid UUID, calling getProject must throw ProjectException")
    void retrieveProjectUsingInvalidId() {
        String invalidId = "InvalidID";
        String errorExpectedMessage = "Unparsable ID 'InvalidID'. Should be in UUID format";
        ProjectException exception = assertThrows(ProjectException.class, () -> service.getProject(invalidId));
        assertEquals(errorExpectedMessage, exception.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName(
            "Given an valid ID that does NOT belong to any registered Project, " +
            "calling getProject must throw ProjectNotFoundException"
    )
    void retrieveProjectUsingIdNotFound() {
        String validId = UUID.randomUUID().toString();
        String errorExpectedMessage = "Project not found with ID '" + validId + "'";
        when(repository.findById(UUID.fromString(validId))).thenReturn(Optional.empty());
        ProjectNotFoundException exception = assertThrows(
                ProjectNotFoundException.class, () -> service.getProject(validId)
        );
        assertEquals(errorExpectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName(
            "Given an valid ID that belongs to existing Project, " +
            "calling getProject must return ProjectReadOnlyDetailed"
    )
    void retrieveProjectUsingIdFound() {
        Project mockProject = getMockProjects().getFirst();
        ProjectReadOnlyDetailed expectedResult = new ProjectReadOnlyDetailed(
                mockProject.getId(),
                mockProject.getName(),
                mockProject.getDescription(),
                mockProject.getStack(),
                mockProject.getGithubURL(),
                mockProject.getDemoURL(),
                mockProject.isFeatured(),
                mockProject.getCreatedAt()
        );
        ProjectReadOnlyDetailed actualResult = service.getProjectDTO(mockProject);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    @DisplayName("Given an null Request, calling saveProject must throw ProjectException")
    void nullRequest() {
        String expectedMessage = "The Project Request is required";
        ProjectException expectedException = assertThrows(ProjectException.class, () -> service.saveProject(null));
        assertEquals(expectedMessage, expectedException.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Given an valid Request, calling saveProject must persist a NEW Project with the request's data")
    void validRequest() {
        Project project = getMockProjects().getFirst();
        ProjectRequest request = new ProjectRequest(
                project.getName(),
                project.getDescription(),
                project.getStack(),
                project.getGithubURL(),
                project.getDemoURL(),
                project.isFeatured()
        );
        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        when(repository.save(any())).thenReturn(project);

        service.saveProject(request);

        verify(repository, times(1)).save(captor.capture());
        Project savedProject = captor.getValue();
        assertNull(savedProject.getId(), "A brand new Project must not have an id set by the service");
        assertEquals(request.name(), savedProject.getName());
        assertEquals(request.description(), savedProject.getDescription());
        assertEquals(request.stack(), savedProject.getStack());
        assertEquals(request.githubURL(), savedProject.getGithubURL());
        assertEquals(request.demoURL(), savedProject.getDemoURL());
        assertEquals(request.featured(), savedProject.isFeatured());
    }

    @Test
    @DisplayName("Given a null Project and a null Request, calling updateProject must throw ProjectException")
    void updateForNullProjectAndNullRequestThrows() {
        String expectedErrorMessage = "The project / request is required";
        ProjectException exception = assertThrows(
                ProjectException.class, () -> service.updateProject(null, null)
        );
        assertEquals(expectedErrorMessage, exception.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Given a null Project and a valid Request, calling updateProject must throw ProjectException")
    void updateForNullProjectThrows() {
        Project reference = getMockProjects().getFirst();
        ProjectRequest request = new ProjectRequest(
                reference.getName(),
                reference.getDescription(),
                reference.getStack(),
                reference.getGithubURL(),
                reference.getDemoURL(),
                reference.isFeatured()
        );
        assertThrows(ProjectException.class, () -> service.updateProject(null, request));
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Given a valid Project and a null Request, calling updateProject must throw ProjectException")
    void updateForNullRequestThrows() {
        Project project = getMockProjects().getFirst();
        assertThrows(ProjectException.class, () -> service.updateProject(project, null));
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName(
            "Given an valid Project and ProjectRequest, calling updateProject " +
            "must overwrite the project's fields and call repository.save with the SAME instance"
    )
    void updateForValidObject() {
        Project project = getMockProjects().getFirst();
        ProjectRequest projectRequest = new ProjectRequest(
                "New project name",
                "Desc",
                List.of("Kotlin", "Spring"),
                "https://github.com/new-repo",
                "https://demo.new",
                true
        );

        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        service.updateProject(project, projectRequest);

        verify(repository, times(1)).save(captor.capture());
        assertSame(
                project,
                captor.getValue(),
                "updateProject must mutate and save the SAME instance it received, not a copy"
        );
        assertEquals("New project name", project.getName());
        assertEquals("Desc", project.getDescription());
        assertEquals(List.of("Kotlin", "Spring"), project.getStack());
        assertEquals("https://github.com/new-repo", project.getGithubURL());
        assertEquals("https://demo.new", project.getDemoURL());
        assertTrue(project.isFeatured());
    }

    @Test
    @DisplayName(
            "Given a valid Project, calling deleteProject must call " +
            "repository.delete exactly once with that same instance"
    )
    void deleteValidProjectCallsRepositoryOnce() {
        Project project = getMockProjects().getFirst();

        service.deleteProject(project);

        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        verify(repository, times(1)).delete(captor.capture());
        assertSame(project, captor.getValue());
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName(
            "deleteProject must not swallow exceptions " +
            "raised by the repository (e.g. constraint violations) - it must propagate them"
    )
    void deleteProjectPropagatesRepositoryExceptions() {
        Project project = getMockProjects().get(2);
        doThrow(new RuntimeException("simulated database constraint violation"))
                .when(repository).delete(project);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.deleteProject(project));

        assertEquals("simulated database constraint violation", exception.getMessage());
    }

    @Test
    @DisplayName("Given a null project, calling service.deleteProject throws ProjectException")
    void deleteProjectWithNullProjectThrows() {
        assertThrows(ProjectException.class, () -> service.deleteProject(null));
        verify(repository, never()).delete(any());
    }
}
