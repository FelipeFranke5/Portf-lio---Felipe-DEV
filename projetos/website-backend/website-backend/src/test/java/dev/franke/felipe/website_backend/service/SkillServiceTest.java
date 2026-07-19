package dev.franke.felipe.website_backend.service;

import dev.franke.felipe.website_backend.dto.SkillDTO;
import dev.franke.felipe.website_backend.dto.SkillLevel;
import dev.franke.felipe.website_backend.dto.SkillRequest;
import dev.franke.felipe.website_backend.exception.SkillException;
import dev.franke.felipe.website_backend.exception.SkillNotFoundException;
import dev.franke.felipe.website_backend.model.Skill;
import dev.franke.felipe.website_backend.repository.SkillRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class SkillServiceTest {

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private SkillService service;

    private Skill buildSkill(String name, String category, int level) {
        return new Skill(UUID.randomUUID(), name, category, (short) level);
    }

    private List<Skill> getMockSkills() {
        return List.of(
                buildSkill("Java", "Backend", 5),
                buildSkill("Angular", "Frontend", 3),
                buildSkill("Docker", "DevOps", 1)
        );
    }

    // ─── getSkills ──────────────────────────────────────────────

    @Test
    @DisplayName("When there are no results in the Database, calling getSkills must return empty list")
    void emptyResults() {
        when(skillRepository.findAll()).thenReturn(List.of());
        List<SkillDTO> actualResult = service.getSkills();
        assertTrue(actualResult.isEmpty());
    }

    @Test
    @DisplayName(
            "When there are results in the Database, calling " +
            "getSkills must return the correct list of SkillDTO, with the level correctly mapped"
    )
    void notEmptyResults() {
        List<Skill> mockedSkills = getMockSkills();
        when(skillRepository.findAll()).thenReturn(mockedSkills);

        List<SkillDTO> actualResult = service.getSkills();

        assertEquals(3, actualResult.size());
        SkillDTO expectedFirst = new SkillDTO(mockedSkills.getFirst().getId(), "Java", "Backend", SkillLevel.WORK_EXPERIENCE);
        SkillDTO expectedSecond = new SkillDTO(mockedSkills.get(1).getId(), "Angular", "Frontend", SkillLevel.INTERMEDIATE_KNOWLEDGE);
        SkillDTO expectedThird = new SkillDTO(mockedSkills.get(2).getId(), "Docker", "DevOps", SkillLevel.ZERO_EXPERIENCE_STILL_LEARNING);
        assertEquals(List.of(expectedFirst, expectedSecond, expectedThird), actualResult);
    }

    @ParameterizedTest(name = "Numeric level {0} must be mapped to enum constant {1}")
    @DisplayName("Every valid numeric level (1-5) must be mapped to its corresponding SkillLevel constant")
    @CsvSource({
            "1, ZERO_EXPERIENCE_STILL_LEARNING",
            "2, SOME_EXPERIENCE_STILL_LEARNING",
            "3, INTERMEDIATE_KNOWLEDGE",
            "4, ADVANCED_KNOWLEDGE",
            "5, WORK_EXPERIENCE"
    })
    void everyValidLevelIsMappedToCorrectEnumConstant(int level, SkillLevel expected) {
        Skill skill = buildSkill("Some Skill", "Some Category", level);
        when(skillRepository.findAll()).thenReturn(List.of(skill));

        List<SkillDTO> result = service.getSkills();

        assertEquals(expected, result.getFirst().skillLevel());
    }

    @Test
    @DisplayName(
            "Given a Skill persisted with an out-of-range level (data corruption / manual tampering), " +
            "calling getSkills must throw SkillException instead of leaking an unchecked exception"
    )
    void getSkillsWithInvalidStoredLevelThrowsSkillException() {
        Skill corruptSkill = buildSkill("Corrupted", "Unknown", 99);
        when(skillRepository.findAll()).thenReturn(List.of(corruptSkill));

        SkillException exception = assertThrows(SkillException.class, () -> service.getSkills());
        assertEquals("Invalid stored skill level: 99", exception.getMessage());
    }

    @Test
    @DisplayName("Given an ID that is not a valid UUID, calling getSkillById must throw SkillException")
    void retrieveSkillUsingInvalidId() {
        String invalidId = "InvalidID";
        String expectedMessage = "Unparsable ID: InvalidID";

        SkillException exception = assertThrows(SkillException.class, () -> service.getSkillById(invalidId));

        assertEquals(expectedMessage, exception.getMessage());
        verifyNoInteractions(skillRepository);
    }

    @Test
    @DisplayName(
            "Given a valid ID that does NOT belong to any registered Skill, " +
            "calling getSkillById must throw SkillNotFoundException"
    )
    void retrieveSkillUsingIdNotFound() {
        String validId = UUID.randomUUID().toString();
        String expectedMessage = "Skill with id: " + validId + " not found";
        when(skillRepository.findById(UUID.fromString(validId))).thenReturn(Optional.empty());

        SkillNotFoundException exception = assertThrows(
                SkillNotFoundException.class, () -> service.getSkillById(validId)
        );
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName(
            "Given a valid ID that belongs to an existing Skill, " +
            "calling getSkillById must return the correct SkillDTO"
    )
    void retrieveSkillUsingIdFound() {
        Skill mockSkill = getMockSkills().getFirst();
        String validId = mockSkill.getId().toString();
        SkillDTO expectedResult = new SkillDTO(
                mockSkill.getId(),
                mockSkill.getName(),
                mockSkill.getCategory(),
                SkillLevel.fromLevel(mockSkill.getLevel())
        );
        when(skillRepository.findById(UUID.fromString(validId))).thenReturn(Optional.of(mockSkill));

        SkillDTO actualResult = service.getSkillById(validId);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    @DisplayName("Given a null Request, calling saveSkill must throw SkillException")
    void nullRequest() {
        String expectedMessage = "Request cannot be null";
        SkillException exception = assertThrows(SkillException.class, () -> service.saveSkill(null));
        assertEquals(expectedMessage, exception.getMessage());
        verifyNoInteractions(skillRepository);
    }

    @Test
    @DisplayName("Given a Request whose name is already used by an existing Skill, calling saveSkill must throw SkillException")
    void saveSkillWithExistingNameThrows() {
        SkillRequest request = new SkillRequest("Java", "Backend", 5);
        when(skillRepository.existsByName("Java")).thenReturn(true);

        SkillException exception = assertThrows(SkillException.class, () -> service.saveSkill(request));

        assertEquals("Skill with name Java already exists", exception.getMessage());
        verify(skillRepository, never()).save(any());
    }

    @Test
    @DisplayName("Given a valid Request, calling saveSkill must persist a NEW Skill with the request's data")
    void validRequest() {
        SkillRequest request = new SkillRequest("Kotlin", "Backend", 4);
        when(skillRepository.existsByName("Kotlin")).thenReturn(false);
        ArgumentCaptor<Skill> captor = ArgumentCaptor.forClass(Skill.class);

        service.saveSkill(request);

        verify(skillRepository, times(1)).save(captor.capture());
        Skill savedSkill = captor.getValue();
        assertNull(savedSkill.getId(), "A brand new Skill must not have an id set by the service");
        assertEquals(request.name(), savedSkill.getName());
        assertEquals(request.category(), savedSkill.getCategory());
        assertEquals(request.level(), savedSkill.getLevel());
    }

    @Test
    @DisplayName("Given a null Request, calling updateSkill must throw SkillException")
    void updateWithNullRequestThrows() {
        String validId = UUID.randomUUID().toString();
        SkillException exception = assertThrows(
                SkillException.class, () -> service.updateSkill(validId, null)
        );
        assertEquals("Request cannot be null", exception.getMessage());
        verifyNoInteractions(skillRepository);
    }

    @Test
    @DisplayName("Given a null ID, calling updateSkill must throw SkillException")
    void updateWithNullIdThrows() {
        SkillRequest request = new SkillRequest("Java", "Backend", 5);
        SkillException exception = assertThrows(
                SkillException.class, () -> service.updateSkill(null, request)
        );
        assertEquals("Skill ID cannot be null", exception.getMessage());
        verifyNoInteractions(skillRepository);
    }

    @Test
    @DisplayName("Given an ID that is not a valid UUID, calling updateSkill must throw SkillException")
    void updateWithInvalidIdFormatThrows() {
        SkillRequest request = new SkillRequest("Java", "Backend", 5);
        SkillException exception = assertThrows(
                SkillException.class, () -> service.updateSkill("InvalidID", request)
        );
        assertEquals("Unparsable ID: InvalidID", exception.getMessage());
        verifyNoInteractions(skillRepository);
    }

    @Test
    @DisplayName(
            "Given a valid ID that does NOT belong to any registered Skill, " +
            "calling updateSkill must throw SkillNotFoundException"
    )
    void updateWithIdNotFoundThrows() {
        String validId = UUID.randomUUID().toString();
        SkillRequest request = new SkillRequest("Java", "Backend", 5);
        when(skillRepository.findById(UUID.fromString(validId))).thenReturn(Optional.empty());
        assertThrows(SkillNotFoundException.class, () -> service.updateSkill(validId, request));
    }

    @Test
    @DisplayName(
            "Given a valid ID and Request, calling updateSkill must overwrite the " +
            "existing Skill's fields and save the SAME instance"
    )
    void updateWithValidIdAndRequest() {
        Skill existingSkill = buildSkill("Java", "Backend", 3);
        String validId = existingSkill.getId().toString();
        SkillRequest request = new SkillRequest("Java Updated", "Backend Advanced", 5);
        when(skillRepository.findById(UUID.fromString(validId))).thenReturn(Optional.of(existingSkill));
        ArgumentCaptor<Skill> captor = ArgumentCaptor.forClass(Skill.class);

        service.updateSkill(validId, request);

        verify(skillRepository, times(1)).save(captor.capture());
        assertSame(
                existingSkill,
                captor.getValue(),
                "updateSkill must mutate and save the SAME instance it retrieved, not a copy"
        );
        assertEquals("Java Updated", existingSkill.getName());
        assertEquals("Backend Advanced", existingSkill.getCategory());
        assertEquals(5, existingSkill.getLevel());
    }

    @Test
    @DisplayName("Given an ID that is not a valid UUID, calling deleteSkill must throw SkillException")
    void deleteWithInvalidIdFormatThrows() {
        SkillException exception = assertThrows(
                SkillException.class, () -> service.deleteSkill("InvalidID")
        );
        assertEquals("Unparsable ID: InvalidID", exception.getMessage());
        verifyNoInteractions(skillRepository);
    }

    @Test
    @DisplayName(
            "Given a valid ID that does NOT belong to any registered Skill, " +
            "calling deleteSkill must throw SkillNotFoundException"
    )
    void deleteWithIdNotFoundThrows() {
        String validId = UUID.randomUUID().toString();
        when(skillRepository.findById(UUID.fromString(validId))).thenReturn(Optional.empty());

        assertThrows(SkillNotFoundException.class, () -> service.deleteSkill(validId));
        verify(skillRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Given a valid ID, calling deleteSkill must call repository.delete exactly once with the found instance")
    void deleteWithValidIdCallsRepositoryOnce() {
        Skill existingSkill = buildSkill("Java", "Backend", 3);
        String validId = existingSkill.getId().toString();
        when(skillRepository.findById(UUID.fromString(validId))).thenReturn(Optional.of(existingSkill));
        ArgumentCaptor<Skill> captor = ArgumentCaptor.forClass(Skill.class);

        service.deleteSkill(validId);

        verify(skillRepository, times(1)).delete(captor.capture());
        assertSame(existingSkill, captor.getValue());
    }

    @Test
    @DisplayName("deleteSkill must not swallow exceptions raised by the repository - it must propagate them")
    void deleteSkillPropagatesRepositoryExceptions() {
        Skill existingSkill = buildSkill("Java", "Backend", 3);
        String validId = existingSkill.getId().toString();
        when(skillRepository.findById(UUID.fromString(validId))).thenReturn(Optional.of(existingSkill));
        doThrow(new RuntimeException("simulated database constraint violation"))
                .when(skillRepository).delete(existingSkill);

        RuntimeException exception = assertThrows(
                RuntimeException.class, () -> service.deleteSkill(validId)
        );
        assertEquals("simulated database constraint violation", exception.getMessage());
    }
}
