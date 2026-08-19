package uk.gov.cabinetoffice.csl.domain.learning.requiredLearning;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.cabinetoffice.csl.domain.learnerrecord.State;
import uk.gov.cabinetoffice.csl.domain.learning.learningPlan.LearningPlanCourse;
import uk.gov.cabinetoffice.csl.domain.learningcatalogue.LearningPeriod;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RequiredLearningCourse extends LearningPlanCourse {

    public RequiredLearningCourse(String id, String title, String shortDescription, String type, Integer duration,
                                  Integer moduleCount, Integer costInPounds, State status, LearningPeriod learningPeriod) {
        super(id, title, shortDescription, status, type, duration, moduleCount, costInPounds);
        this.learningPeriod = learningPeriod;
    }

    @JsonIgnore
    private LearningPeriod learningPeriod;

    public LocalDate getDueBy() {
        return learningPeriod.getEndDate();
    }

    @JsonIgnore
    public String getShortString() {
        return String.format("[Id: '%s', Title: '%s', Learning period: '%s']", getId(), getTitle(), learningPeriod);
    }

}
