package uk.gov.cabinetoffice.csl.service.learning;

import org.springframework.stereotype.Service;
import uk.gov.cabinetoffice.csl.domain.User;
import uk.gov.cabinetoffice.csl.domain.learnerrecord.State;
import uk.gov.cabinetoffice.csl.domain.learningcatalogue.Course;
import uk.gov.cabinetoffice.csl.service.LearnerRecordDataUtils;
import uk.gov.cabinetoffice.csl.service.learningCatalogue.LearningCatalogueService;
import uk.gov.cabinetoffice.csl.service.user.UserDetailsService;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CourseStatusService {

    private final UserDetailsService userDetailsService;
    private final LearningCatalogueService learningCatalogueService;
    private final LearnerRecordDataUtils learnerRecordDataUtils;

    public CourseStatusService(UserDetailsService userDetailsService, LearningCatalogueService learningCatalogueService, LearnerRecordDataUtils learnerRecordDataUtils) {
        this.userDetailsService = userDetailsService;
        this.learningCatalogueService = learningCatalogueService;
        this.learnerRecordDataUtils = learnerRecordDataUtils;
    }

    public Map<String, State> getStateForCourses(String userId, List<String> courseIds) {
        Map<String, State> results = new HashMap<>();
        User user = userDetailsService.getUserWithUid(userId);
        Map<String, LocalDateTime> completionDatesForCourses = learnerRecordDataUtils.getCompletionDatesForCourses(userId, courseIds);
        List<Course> nonCompletedCourses = learningCatalogueService.getCourses(courseIds)
                .stream().map(course -> {
                    State state = Optional.ofNullable(completionDatesForCourses.get(course.getId()))
                            .map(completionDate -> course.getStateForUser(user, completionDate))
                            .orElse(State.NULL);
                    results.put(course.getId(), state);
                    if (state == State.NULL) {
                        return course;
                    } else {
                        return null;
                    }
                }).filter(Objects::nonNull).toList();
        if (!nonCompletedCourses.isEmpty()) {
            Map<String, ModuleRecordCollection> moduleRecordsForCourses = learnerRecordDataUtils.getModuleRecordsForCourses(userId, nonCompletedCourses);
            nonCompletedCourses.forEach(course -> Optional.ofNullable(moduleRecordsForCourses.get(course.getId()))
                    .ifPresent(modules -> {
                        if (course.isDateAfterLearningPeriod(user, modules.getLatestUpdatedDate())) {
                            results.put(course.getId(), State.IN_PROGRESS);
                        }
                    }));
        }
        return results;
    }
}
