package uk.gov.cabinetoffice.csl.domain.learningcatalogue;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.cabinetoffice.csl.domain.IParentLearningResource;
import uk.gov.cabinetoffice.csl.domain.LearningResourceType;
import uk.gov.cabinetoffice.csl.domain.User;
import uk.gov.cabinetoffice.csl.domain.learnerrecord.ModuleRecord;
import uk.gov.cabinetoffice.csl.domain.learnerrecord.State;
import uk.gov.cabinetoffice.csl.domain.learningcatalogue.event.Event;
import uk.gov.cabinetoffice.csl.util.Cacheable;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static uk.gov.cabinetoffice.csl.domain.learningcatalogue.CourseStatus.PUBLISHED;

@Data
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Course implements IParentLearningResource<Module>, Cacheable {
    private String id;
    private String title;
    private String shortDescription;
    private CourseStatus status;
    private CourseVisibility visibility;
    private Collection<Module> modules = Collections.emptyList();
    private List<Audience> audiences = Collections.emptyList();

    private Map<String, Integer> departmentCodeToRequiredAudienceMap = new HashMap<>();

    @JsonIgnore
    public String getCourseType() {
        if (modules.isEmpty()) {
            return "unknown";
        } else if (modules.size() == 1) {
            return modules.stream().findFirst().get().getModuleType().getText();
        } else {
            return "blended";
        }
    }

    @JsonIgnore
    public Integer getDurationInSeconds() {
        return modules.stream().mapToInt(Module::getDurationInSeconds).sum();
    }

    @JsonIgnore
    public void updateEvent(String moduleId, Event event) {
        Optional.ofNullable(getModule(moduleId))
                .ifPresent(module -> module.updateEvent(event));
    }

    @JsonIgnore
    public Audience getRequiredAudienceWithDepCode(String departmentCode) {
        Audience audience = null;
        Integer index = departmentCodeToRequiredAudienceMap.get(departmentCode);
        if (index != null) {
            audience = audiences.get(index);
        }
        return audience;
    }

    @JsonIgnore
    public Module getModule(String moduleId) {
        List<Module> modules = this.modules.stream().filter(m -> m.getId().equals(moduleId)).toList();
        if (modules.size() != 1) {
            return null;
        } else {
            return modules.get(0);
        }
    }

    @JsonIgnore
    public boolean isMandatoryLearningForUser(User user) {
        return getLearningPeriodForDepartmentHierarchy(user.getDepartmentCodes()).isPresent();
    }

    @JsonIgnore
    public Optional<Audience> getAudienceForDepartmentHierarchy(Collection<String> departmentCodeHierarchy) {
        for (String code : departmentCodeHierarchy) {
            Audience audience = getRequiredAudienceWithDepCode(code);
            if (audience != null) {
                return Optional.of(audience);
            }
        }
        return Optional.empty();
    }

    @JsonIgnore
    public Optional<LearningPeriod> getLearningPeriodForUser(User user) {
        return getLearningPeriodForDepartmentHierarchy(user.getDepartmentCodes());
    }

    @JsonIgnore
    public Optional<LearningPeriod> getLearningPeriodForDepartmentHierarchy(Collection<String> departmentCodeHierarchy) {
        Optional<Audience> optionalAudience = getAudienceForDepartmentHierarchy(departmentCodeHierarchy);
        return optionalAudience.map(Audience::getLearningPeriod);
    }

    @JsonIgnore
    public List<String> getRequiredModuleIdsForCompletion() {
        return getRequiredModulesForCompletion().stream()
                .map(Module::getId)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<Module> getRequiredModulesForCompletion() {
        return this.modules.stream()
                .filter(Module::isRequiredForCompletion)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public boolean isDateAfterLearningPeriod(User user, LocalDateTime date) {
        return getLearningPeriodForDepartmentHierarchy(user.getDepartmentCodes())
                .map(learningPeriod -> date.isAfter(learningPeriod.getStartDateAsDateTime()))
                .orElse(true);
    }

    @JsonIgnore
    public State getStateForUser(User user, LocalDateTime completionDate) {
        return isDateAfterLearningPeriod(user, completionDate) ? State.COMPLETED : State.NULL;
    }

    @JsonIgnore
    public Collection<String> getRemainingModuleIdsForCompletion(Map<String, ModuleRecord> moduleRecordMap, User user) {
        log.debug(String.format("Getting learning period for course %s and department codes %s", this.getId(), user.getDepartmentCodes()));
        LearningPeriod learningPeriod = getLearningPeriodForDepartmentHierarchy(user.getDepartmentCodes()).orElse(null);
        log.debug(String.format("Selected learning period: %s", learningPeriod));
        Map<String, State> realModuleStates = new HashMap<>();
        moduleRecordMap.forEach((moduleId, moduleRecord) -> {
            State moduleRecordState = moduleRecord.getStateForLearningPeriod(learningPeriod);
            realModuleStates.put(moduleId, moduleRecordState);
        });
        return getRequiredModulesForCompletion().stream().filter(mod -> {
            State moduleState = realModuleStates.getOrDefault(mod.getId(), State.NULL);
            return !moduleState.equals(State.COMPLETED);
        }).map(Module::getId).collect(Collectors.toSet());
    }

    @Override
    @JsonIgnore
    public String getResourceId() {
        return id;
    }

    @Override
    @JsonIgnore
    public String getName() {
        return title;
    }

    @Override
    public LearningResourceType getType() {
        return LearningResourceType.COURSE;
    }

    @Override
    @JsonIgnore
    public Collection<Module> getChildren() {
        return this.modules;
    }

    @Override
    @JsonIgnore
    public String getCacheableId() {
        return id;
    }

    @JsonIgnore
    public boolean shouldBeDisplayed() {
        return (Objects.equals(PUBLISHED, getStatus()) && !getModules().isEmpty());
    }

    @JsonIgnore
    public Integer getCost() {
        return getModules().stream().mapToInt(m -> m.getCost().intValue()).sum();
    }
}
