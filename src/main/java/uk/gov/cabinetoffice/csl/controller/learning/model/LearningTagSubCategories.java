package uk.gov.cabinetoffice.csl.controller.learning.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.cabinetoffice.csl.controller.model.PagedResults;
import uk.gov.cabinetoffice.csl.domain.learning.learningPlan.BasicCourse;

import java.util.Collection;

@NoArgsConstructor
@Getter
@Setter
public class LearningTagSubCategories extends LearningTagCategories {

    private String title;
    private String description;
    private Collection<Link> parents;
    private PagedResults<BasicCourse> courses;

    public LearningTagSubCategories(Collection<LearningTagCategory> categories, String title, String description, Collection<Link> parents,
                                    PagedResults<BasicCourse> courses) {
        super(categories);
        this.title = title;
        this.description = description;
        this.parents = parents;
        this.courses = courses;
    }
}
