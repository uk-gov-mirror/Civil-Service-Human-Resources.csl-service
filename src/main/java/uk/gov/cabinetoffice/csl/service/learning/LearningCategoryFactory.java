package uk.gov.cabinetoffice.csl.service.learning;

import org.springframework.stereotype.Service;
import uk.gov.cabinetoffice.csl.controller.learning.model.LearningTagCategories;
import uk.gov.cabinetoffice.csl.controller.learning.model.LearningTagCategory;
import uk.gov.cabinetoffice.csl.controller.learning.model.LearningTagSubCategories;
import uk.gov.cabinetoffice.csl.controller.learning.model.Link;
import uk.gov.cabinetoffice.csl.controller.model.PagedResults;
import uk.gov.cabinetoffice.csl.domain.learnerrecord.State;
import uk.gov.cabinetoffice.csl.domain.learning.LearningTagTaxonomy;
import uk.gov.cabinetoffice.csl.domain.learning.learningPlan.BasicCourse;
import uk.gov.cabinetoffice.csl.domain.learningcatalogue.CourseLearningTagSearchResults;
import uk.gov.cabinetoffice.csl.domain.learningcatalogue.learningTag.LearningTag;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class LearningCategoryFactory {

    public LearningTagCategories buildCategories(Collection<LearningTag> tierOneTags) {
        Collection<LearningTagCategory> categories = tierOneTags
                .stream().map(lt -> new LearningTagCategory(lt.getName(), lt.getDescription(), lt.getUrlSlug(), List.of()))
                .sorted(Comparator.comparing(LearningTagCategory::getTitle))
                .toList();
        return new LearningTagCategories(categories);
    }

    public LearningTagSubCategories buildSubCategories(LearningTagTaxonomy taxonomy) {
        Collection<Link> parentLinks = taxonomy.parents().stream()
                .map(lt -> new Link(lt.getUrlSlug(), lt.getName())).toList();
        Collection<LearningTagCategory> categories = taxonomy.children()
                .stream().map(lt -> new LearningTagCategory(lt.category().getName(), lt.category().getDescription(), lt.category().getUrlSlug(),
                        lt.children().stream().map(descLt -> new Link(descLt.category().getUrlSlug(), descLt.category().getName()))
                                .sorted(Comparator.comparing(Link::getText))
                                .toList()))
                .sorted(Comparator.comparing(LearningTagCategory::getTitle))
                .toList();
        return new LearningTagSubCategories(categories, taxonomy.category().getName(),
                taxonomy.category().getDescription(), parentLinks, PagedResults.emptyResults());
    }

    public LearningTagSubCategories buildSubCategories(LearningTagTaxonomy taxonomy, CourseLearningTagSearchResults courses, Map<String, State> states) {
        LearningTagSubCategories learningTagSubCategories = buildSubCategories(taxonomy);
        List<BasicCourse> formattedCourses = courses.getResults().stream().map(c -> new BasicCourse(c.getId(), c.getTitle(), c.getShortDescription(), states.get(c.getId()))).toList();
        PagedResults<BasicCourse> results = new PagedResults<>(formattedCourses, courses.getPage(), courses.getSize(), courses.getTotalResults());
        learningTagSubCategories.setCourses(results);
        return learningTagSubCategories;
    }
}
