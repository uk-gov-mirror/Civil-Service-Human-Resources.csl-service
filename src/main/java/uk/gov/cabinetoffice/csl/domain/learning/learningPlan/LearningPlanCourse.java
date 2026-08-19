package uk.gov.cabinetoffice.csl.domain.learning.learningPlan;

import lombok.*;
import uk.gov.cabinetoffice.csl.domain.learnerrecord.State;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class LearningPlanCourse extends BasicCourse {
    private String type;
    private Integer duration;
    private Integer moduleCount;
    private Integer costInPounds;

    public LearningPlanCourse(String id, String title, String shortDescription, State status, String type, Integer duration, Integer moduleCount, Integer costInPounds) {
        super(id, title, shortDescription, status);
        this.type = type;
        this.duration = duration;
        this.moduleCount = moduleCount;
        this.costInPounds = costInPounds;
    }

}
