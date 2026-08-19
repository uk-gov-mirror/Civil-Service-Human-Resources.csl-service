package uk.gov.cabinetoffice.csl.service.learning;

import org.springframework.stereotype.Service;
import uk.gov.cabinetoffice.csl.domain.learnerrecord.ModuleRecord;
import uk.gov.cabinetoffice.csl.domain.learnerrecord.State;
import uk.gov.cabinetoffice.csl.domain.learning.learningPlan.BookedLearningPlanCourse;
import uk.gov.cabinetoffice.csl.domain.learning.learningPlan.EventModule;
import uk.gov.cabinetoffice.csl.domain.learning.learningPlan.LearningPlanCourse;
import uk.gov.cabinetoffice.csl.domain.learningcatalogue.Course;
import uk.gov.cabinetoffice.csl.domain.learningcatalogue.Module;
import uk.gov.cabinetoffice.csl.domain.learningcatalogue.event.Event;
import uk.gov.cabinetoffice.csl.util.IUtilService;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class LearningPlanFactory {

    private final IUtilService utilService;

    public LearningPlanFactory(IUtilService utilService) {
        this.utilService = utilService;
    }

    private EventModule buildEventModule(Module module, Event event, ModuleRecord moduleRecord) {
        return new EventModule(module.getId(), module.getTitle(), event.getId(), moduleRecord.getEventDate(),
                event.getDateRangesAsDates(), moduleRecord.getState());
    }

    public Optional<BookedLearningPlanCourse> getBookedLearningPlanCourse(Course course, ModuleRecordCollection moduleRecordCollection) {
        return moduleRecordCollection.getModuleRecord().map(moduleRecord -> {
            Optional<BookedLearningPlanCourse> bookedLearningPlanCourse = Optional.empty();
            Module module = course.getModule(moduleRecord.getModuleId());
            Event event = module.getEvent(moduleRecord.getEventId());
            List<String> idsLeftForCompletion = moduleRecordCollection.getRequiredIdsLeftForCompletion(course.getRequiredModuleIdsForCompletion());
            boolean isLastModuleToComplete = idsLeftForCompletion.size() == 1 && idsLeftForCompletion.get(0).equals(moduleRecord.getModuleId());
            if (event != null) {
                EventModule eventModule = buildEventModule(module, event, moduleRecord);
                boolean canBeMovedToLearningPlan = utilService.getNowDateTime().isAfter(moduleRecord.getEventDate().atTime(LocalTime.MIN)) &&
                        isLastModuleToComplete && moduleRecord.equalsStates(State.APPROVED);
                bookedLearningPlanCourse = Optional.of(new BookedLearningPlanCourse(course.getId(), course.getTitle(),
                        course.getShortDescription(), course.getCourseType(), course.getDurationInSeconds(),
                        course.getModules().size(), course.getCost(), State.NULL, eventModule, canBeMovedToLearningPlan));
            }
            return bookedLearningPlanCourse;
        }).orElse(Optional.empty());
    }

    public LearningPlanCourse getLearningPlanCourse(Course course, State state) {
        return new LearningPlanCourse(course.getId(), course.getTitle(),
                course.getShortDescription(), state, course.getCourseType(), course.getDurationInSeconds(),
                course.getModules().size(), course.getCost());
    }
}
