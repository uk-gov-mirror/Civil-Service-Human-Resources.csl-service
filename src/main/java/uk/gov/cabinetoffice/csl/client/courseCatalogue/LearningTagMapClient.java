package uk.gov.cabinetoffice.csl.client.courseCatalogue;

import org.springframework.stereotype.Service;
import uk.gov.cabinetoffice.csl.client.model.BulkUpdateResponse;
import uk.gov.cabinetoffice.csl.domain.learningcatalogue.CourseLearningTagSearchResults;
import uk.gov.cabinetoffice.csl.domain.learningcatalogue.learningTag.LearningTag;
import uk.gov.cabinetoffice.csl.domain.learningcatalogue.learningTag.LearningTagDTO;
import uk.gov.cabinetoffice.csl.domain.learningcatalogue.learningTag.LearningTagMap;
import uk.gov.cabinetoffice.csl.domain.learningcatalogue.learningTag.LearningTagStateUpdate;

import java.util.Collection;

@Service
public class LearningTagMapClient implements ILearningTagMapClient {

    private final ILearningCatalogueClient learningCatalogueClient;

    public LearningTagMapClient(ILearningCatalogueClient learningCatalogueClient) {
        this.learningCatalogueClient = learningCatalogueClient;
    }

    @Override
    public LearningTag create(LearningTagDTO dto) {
        return learningCatalogueClient.createLearningTag(dto);
    }

    @Override
    public LearningTag patch(Long id, LearningTagDTO dto) {
        return learningCatalogueClient.updateLearningTag(id, dto);
    }

    @Override
    public LearningTagMap fetch() {
        return LearningTagMap.buildFromList(learningCatalogueClient.getAllLearningTags());
    }

    public BulkUpdateResponse updateState(Collection<Long> ids, LearningTagStateUpdate stateUpdate) {
        return learningCatalogueClient.updateLearningTagState(ids, stateUpdate);
    }

    public CourseLearningTagSearchResults getCoursesForTag(Long id, Integer page, Integer size) {
        return learningCatalogueClient.getCoursesForLearningTag(id, page, size);
    }
}
