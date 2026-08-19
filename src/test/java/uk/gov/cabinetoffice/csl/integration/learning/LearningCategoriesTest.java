package uk.gov.cabinetoffice.csl.integration.learning;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.cabinetoffice.csl.domain.learnerrecord.record.LearnerRecordEventQuery;
import uk.gov.cabinetoffice.csl.integration.IntegrationTestBase;
import uk.gov.cabinetoffice.csl.util.TestDataService;
import uk.gov.cabinetoffice.csl.util.data.ArrayJsonContentBuilder;
import uk.gov.cabinetoffice.csl.util.data.catalogue.JsonCourseBuilder;
import uk.gov.cabinetoffice.csl.util.data.catalogue.JsonCourseDtoBuilder;
import uk.gov.cabinetoffice.csl.util.data.catalogue.JsonLearningTagBuilder;
import uk.gov.cabinetoffice.csl.util.data.learnerRecord.JsonModuleRecordBuilder;
import uk.gov.cabinetoffice.csl.util.stub.CSLStubService;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LearningCategoriesTest extends IntegrationTestBase {

    @Autowired
    private TestDataService testDataService;

    @Autowired
    private CSLStubService cslStubService;

    private final String learningTagsPagedResponse = new ArrayJsonContentBuilder<JsonLearningTagBuilder>()
            .addElements(
                    JsonLearningTagBuilder.create(1L, null, null, "2025-01-01T10:00:00").isCategory(),
                    JsonLearningTagBuilder.create(2L, 1L, "TagName1", "2025-01-01T10:00:00").isCategory(),
                    JsonLearningTagBuilder.create(3L, 2L, "TagName2", "2025-01-01T10:00:00").isCategory(),
                    JsonLearningTagBuilder.create(4L, null, null, "2025-01-01T10:00:00").isCategory(),
                    JsonLearningTagBuilder.create(5L, 1L, "TagName1", "2025-01-01T10:00:00").isCategory(),
                    JsonLearningTagBuilder.create(6L, null, null, "2025-01-01T10:00:00").isArchived()
            ).getAsPaginatedAndBuild(0, 5, 1);


    @Test
    public void testGetCategories() throws Exception {
        cslStubService.getLearningCatalogue().getLearningTags(learningTagsPagedResponse);
        mockMvc.perform(get("/learning/categories"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json("""
                        {
                          "categories": [
                            {
                              "title": "TagName1",
                              "description": "TagName1 description",
                              "url": "TAGN1",
                              "categories": []
                            },
                            {
                              "title": "TagName4",
                              "description": "TagName4 description",
                              "url": "TAGN4",
                              "categories": []
                            }
                          ]
                        }
                        """, true));
    }

    @Test
    public void testGetSubCategories() throws Exception {
        cslStubService.getCsrsStubService().getCivilServant("userId", testDataService.generateCivilServant());
        String response = ArrayJsonContentBuilder.create().getAsPaginatedAndBuild(0, 20, 1);
        cslStubService.getLearningCatalogue().getCoursesForLearningTag(2L, 0, 20, response);
        cslStubService.getLearningCatalogue().getLearningTags(learningTagsPagedResponse);
        mockMvc.perform(get("/learning/categories/TAGN2"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json("""
                        {
                            "categories": [
                                {
                                    "title": "TagName3",
                                    "description": "TagName3 description",
                                    "url": "TAGN3",
                                    "categories": []
                                }
                            ],
                            "title": "TagName2",
                            "description": "TagName2 description",
                            "parents": [
                                {
                                    "link": "TAGN1",
                                    "text": "TagName1"
                                }
                            ],
                            "courses": {
                                "results": [],
                                "page": 0,
                                "size": 0,
                                "totalResults": 0
                            }
                        }
                        """, true));
    }

    @Test
    public void testGetSubCategoriesDescendant() throws Exception {
        cslStubService.getLearningCatalogue().getLearningTags(learningTagsPagedResponse);
        cslStubService.getCsrsStubService().getCivilServant("userId", testDataService.generateCivilServant());
        String response = ArrayJsonContentBuilder.create().getAsPaginatedAndBuild(0, 20, 1);
        cslStubService.getLearningCatalogue().getCoursesForLearningTag(3L, 0, 20, response);
        mockMvc.perform(get("/learning/categories/TAGN3"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json("""
                        {
                            "categories": [],
                            "title": "TagName3",
                            "description": "TagName3 description",
                            "parents": [
                                {
                                    "link": "TAGN2",
                                    "text": "TagName2"
                                },
                                {
                                    "link": "TAGN1",
                                    "text": "TagName1"
                                }
                            ],
                            "courses": {
                                "results": [],
                                "page": 0,
                                "size": 0,
                                "totalResults": 0
                            }
                        }
                        """));
    }

    @Test
    public void testGetSubCategoriesParent() throws Exception {
        cslStubService.getCsrsStubService().getCivilServant("userId", testDataService.generateCivilServant());
        String response = ArrayJsonContentBuilder.create().getAsPaginatedAndBuild(0, 20, 1);
        cslStubService.getLearningCatalogue().getCoursesForLearningTag(1L, 0, 20, response);
        cslStubService.getLearningCatalogue().getLearningTags(learningTagsPagedResponse);
        mockMvc.perform(get("/learning/categories/TAGN1"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json("""
                        {
                            "categories": [
                                {
                                    "title": "TagName2",
                                    "description": "TagName2 description",
                                    "url": "TAGN2",
                                      "categories": [
                                          {
                                              "link": "TAGN3",
                                              "text": "TagName3"
                                          }
                                      ]
                                },
                                {
                                    "title": "TagName5",
                                    "description": "TagName5 description",
                                    "url": "TAGN5",
                                    "categories": []
                                }
                            ],
                            "title": "TagName1",
                            "description": "TagName1 description",
                            "parents": [],
                            "courses": {
                              "results":[],
                              "page":0,
                              "size":0,
                              "totalResults":0
                          }
                        }
                        """, true));
    }

    @Test
    public void testGetSubCategoriesParentWithCourses() throws Exception {
        cslStubService.getCsrsStubService().getCivilServant("userId", testDataService.generateCivilServant());
        cslStubService.getLearningCatalogue().getLearningTags(learningTagsPagedResponse);
        String response = ArrayJsonContentBuilder.create(
                JsonCourseDtoBuilder.create("course1", "Course 1"),
                JsonCourseDtoBuilder.create("course2", "Course 2"),
                JsonCourseDtoBuilder.create("course3", "Course 3")
        ).getAsPaginatedAndBuild(0, 20, 1);
        cslStubService.getLearningCatalogue().getCoursesForLearningTag(1L, 0, 20, response);

        String courses = ArrayJsonContentBuilder.create(
                JsonCourseBuilder.create("course1", "Course 1")
                        .addLinkModule("module1", "module1", false, 30)
                        .addDepartmentRequiredLearning("DWP", "2024-01-01T00:00:00Z", "P1Y")
                        .addDepartmentRequiredLearning("HMRC", "2023-01-01T00:00:00Z", "P1Y"),
                JsonCourseBuilder.create("course2", "Course 2")
                        .addLinkModule("module3", "module3", false, 0),
                JsonCourseBuilder.create("course3", "Course 3")
                        .addLinkModule("module5", "module5", false, 0)
                        .addFileModule("module6", "module6", false, 0)).build();
        cslStubService.getLearningCatalogue().getCourses(List.of("course1", "course2", "course3"), courses);
        String eventsResponse = """
                {
                    "content": [
                        {
                            "eventTimestamp": "2022-01-01T00:00:00Z",
                            "resourceId": "course1"
                        },
                        {
                            "eventTimestamp": "2022-01-01T00:00:00Z",
                            "resourceId": "course2"
                        }
                    ],
                    "totalPages": 1
                }
                """;
        cslStubService.getLearnerRecord().getLearnerRecordEvents(0, LearnerRecordEventQuery.builder().userId("userId").resourceIds(List.of("course1", "course2", "course3"))
                .eventTypes(List.of("COMPLETE_COURSE")).build(), eventsResponse);

        String moduleRecordResponse = ArrayJsonContentBuilder.create(
                JsonModuleRecordBuilder.create("module5", "course3", "userId", "link", "2024-01-01T10:00:00")
                        .addCompletionDate("2025-01-01T10:00:00", "2025-01-01T10:00:00").addState("IN_PROGRESS")
        ).getAsObjectList("moduleRecords").toString();
        cslStubService.getLearnerRecord().getModuleRecords(List.of("userId"), List.of("module5"), moduleRecordResponse);

        mockMvc.perform(get("/learning/categories/TAGN1"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json("""
                        {
                            "categories": [
                                {
                                    "title": "TagName2",
                                    "description": "TagName2 description",
                                    "url": "TAGN2",
                                    "categories": [
                                        {
                                            "link": "TAGN3",
                                            "text": "TagName3"
                                        }
                                    ]
                                },
                                {
                                    "title": "TagName5",
                                    "description": "TagName5 description",
                                    "url": "TAGN5",
                                    "categories": []
                                }
                            ],
                            "title": "TagName1",
                            "description": "TagName1 description",
                            "parents": [],
                            "courses": {
                                "results": [
                                    {
                                        "id": "course1",
                                        "title": "Course 1",
                                        "shortDescription": "Course 1 short description",
                                        "status": "NULL"
                                    },
                                    {
                                        "id": "course2",
                                        "title": "Course 2",
                                        "shortDescription": "Course 2 short description",
                                        "status": "COMPLETED"
                                    },
                                    {
                                        "id": "course3",
                                        "title": "Course 3",
                                        "shortDescription": "Course 3 short description",
                                        "status": "IN_PROGRESS"
                                    }
                                ],
                                "page": 0,
                                "size": 20,
                                "totalResults": null
                            }
                        }
                        """, true));
    }

}
