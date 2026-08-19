package uk.gov.cabinetoffice.csl.service.learningCatalogue;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;
import uk.gov.cabinetoffice.csl.client.courseCatalogue.LearningTagMapClient;
import uk.gov.cabinetoffice.csl.client.model.BulkUpdateResponse;
import uk.gov.cabinetoffice.csl.controller.learning.model.LearningTagOverview;
import uk.gov.cabinetoffice.csl.domain.learning.LearningTagTaxonomy;
import uk.gov.cabinetoffice.csl.domain.learningcatalogue.CourseLearningTagSearchResults;
import uk.gov.cabinetoffice.csl.domain.learningcatalogue.learningTag.*;
import uk.gov.cabinetoffice.csl.domain.taxonomy.FormattedTaxonomyItem;
import uk.gov.cabinetoffice.csl.domain.taxonomy.FormattedTaxonomyItems;
import uk.gov.cabinetoffice.csl.service.CachedTaxonomyMapService;
import uk.gov.cabinetoffice.csl.service.ITaxonomyItemFactory;
import uk.gov.cabinetoffice.csl.util.IUtilService;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class LearningTagMapService extends CachedTaxonomyMapService<LearningTag, LearningTagTreeNode, LearningTagMap, LearningTagDTO, LearningTagOverview> {

    private final Integer maxUrlSlugSize;
    private final IUtilService utilService;
    private final LearningTagMapClient client;

    public LearningTagMapService(@Qualifier("learningCatalogue") Cache cache,
                                 ITaxonomyItemFactory<LearningTag, LearningTagOverview> taxonomyItemFactory,
                                 LearningTagMapClient client,
                                 @Value("${learningCatalogue.validation.learningTag.maxUrlSize}") Integer maxUrlSlugSize, IUtilService utilService) {
        super(cache, "learningTagMap", LearningTagMap.class, taxonomyItemFactory, client);
        this.maxUrlSlugSize = maxUrlSlugSize;
        this.utilService = utilService;
        this.client = client;
    }

    @Override
    public LearningTagOverview create(LearningTagDTO dto) {
        if (dto.getUrlSlug() == null) {
            String slug = utilService.generateUrlSlugFromString(dto.getName(), maxUrlSlugSize);
            dto.setUrlSlug(slug);
        }
        return super.create(dto);
    }

    public FormattedTaxonomyItems<FormattedTaxonomyItem> getFormattedNames() {
        return new FormattedTaxonomyItems<>(get().values().stream()
                .map(o -> new FormattedTaxonomyItem(o.getId(), o.getFormattedName(), o.getCode()))
                .sorted(Comparator.comparing(FormattedTaxonomyItem::getName, String::compareToIgnoreCase))
                .toList());
    }

    @Override
    public LearningTagOverview update(Long id, LearningTagDTO dto) {
        if (dto.getUrlSlug() == null) {
            String slug = utilService.generateUrlSlugFromString(dto.getName(), maxUrlSlugSize);
            dto.setUrlSlug(slug);
        }
        LearningTagMap map = get();
        LearningTag object = map.get(id);
        map.validateUpdate(id, dto.getParentId());
        client.patch(id, dto);
        if (!Objects.equals(object.getParentId(), dto.getParentId())) {
            object = map.updateParent(object, dto.getParentId());
        }
        updateObjectWithDto(object, dto);
        map.rebuildHierarchy(object);
        put(map);
        return taxonomyItemFactory.createOverview(object);
    }

    @Override
    protected void updateObjectWithDto(LearningTag object, LearningTagDTO dto) {
        super.updateObjectWithDto(object, dto);
        object.setUrlSlug(dto.getUrlSlug());
        object.setCategory(dto.isCategory());
        object.setDescription(dto.getDescription());
    }

    public LearningTagOverview updateState(Long learningTagId, LearningTagStateUpdate update) {
        LearningTagMap learningTagMap = get();
        Collection<Long> ids = learningTagMap.getMultipleAsIds(List.of(learningTagId), true);
        BulkUpdateResponse result = client.updateState(ids, update);
        result.getSuccessfulUpdates().forEach(id -> learningTagMap.update(id, learningTag -> {
            learningTag.setArchived(update.equals(LearningTagStateUpdate.ARCHIVE));
            return learningTag;
        }));
        put(learningTagMap);
        return taxonomyItemFactory.createOverview(learningTagMap.get(learningTagId));
    }

    public Collection<LearningTag> getTierOneUnarchivedHomepageTags() {
        return get().values()
                .stream().filter(lt -> lt.showOnHomepage() && lt.getParentId() == null)
                .toList();
    }

    public LearningTagTaxonomy getUnarchivedHomepageTagsWithUrl(String urlSlug) {
        return get().getFullTaxonomyFromUrl(urlSlug);
    }

    public CourseLearningTagSearchResults getCourses(Long learningTagId, Integer page, Integer size) {
        return this.client.getCoursesForTag(learningTagId, page, size);
    }

}
