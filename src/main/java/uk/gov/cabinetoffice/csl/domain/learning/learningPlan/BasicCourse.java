package uk.gov.cabinetoffice.csl.domain.learning.learningPlan;

import lombok.*;
import uk.gov.cabinetoffice.csl.domain.learnerrecord.State;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class BasicCourse {
    private String id;
    private String title;
    private String shortDescription;
    private State status;
}
