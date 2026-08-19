package uk.gov.cabinetoffice.csl.controller.learning;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uk.gov.cabinetoffice.csl.controller.learning.model.LearningTagCategories;
import uk.gov.cabinetoffice.csl.controller.learning.model.LearningTagSubCategories;
import uk.gov.cabinetoffice.csl.service.auth.IUserAuthService;
import uk.gov.cabinetoffice.csl.service.learning.LearningCategoryService;

@RestController
@RequestMapping("learning/categories")
@Slf4j
public class LearningCategoriesController {

    private final LearningCategoryService learningCategoryService;
    private final IUserAuthService iUserAuthService;

    public LearningCategoriesController(LearningCategoryService learningCategoryService, IUserAuthService iUserAuthService) {
        this.learningCategoryService = learningCategoryService;
        this.iUserAuthService = iUserAuthService;
    }

    @GetMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public LearningTagCategories getCategories() {
        return learningCategoryService.getCategories();
    }

    @GetMapping("/{url}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public LearningTagSubCategories getSubCategories(@PathVariable String url, @PageableDefault(size = 20, direction = Sort.Direction.ASC) Pageable pageableParams) {
        return learningCategoryService.getCategories(iUserAuthService.getUsername(), url, pageableParams);
    }

}
