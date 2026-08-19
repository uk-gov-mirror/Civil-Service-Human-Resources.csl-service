package uk.gov.cabinetoffice.csl.util.stub;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.springframework.stereotype.Service;
import uk.gov.cabinetoffice.csl.domain.learnerrecord.ID.LearnerRecordResourceId;
import uk.gov.cabinetoffice.csl.domain.learnerrecord.booking.BookingDto;
import uk.gov.cabinetoffice.csl.domain.learnerrecord.record.LearnerRecordEventQuery;
import uk.gov.cabinetoffice.csl.domain.learnerrecord.record.LearnerRecordQuery;
import uk.gov.cabinetoffice.csl.util.CslTestUtil;

import java.util.List;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Service
public class LearnerRecordStubService {

    private final CslTestUtil utils;

    public LearnerRecordStubService(CslTestUtil utils) {
        this.utils = utils;
    }

    public StubMapping getModuleRecordsForUser(String userId, String response) {
        return getModuleRecords(List.of(userId), List.of(), response);
    }

    public StubMapping getModuleRecord(String moduleId, String userId, String getModuleRecordsResponse) {
        return getModuleRecords(List.of(userId), List.of(moduleId), getModuleRecordsResponse);
    }

    public StubMapping getModuleRecords(List<LearnerRecordResourceId> ids, String response) {
        return getModuleRecords(ids.stream().map(LearnerRecordResourceId::getLearnerId).toList(),
                ids.stream().map(LearnerRecordResourceId::getResourceId).toList(), response);
    }

    public StubMapping getModuleRecords(List<String> userIds, List<String> moduleIds, String response) {
        MappingBuilder mappingBuilder = WireMock.get(urlPathEqualTo("/learner_record_api/module_records"))
                .withQueryParam("userIds", including(userIds.toArray(String[]::new)));
        if (!moduleIds.isEmpty()) {
            mappingBuilder.withQueryParam("moduleIds", including(moduleIds.toArray(String[]::new)));
        }
        return stubFor(
                mappingBuilder
                        .withHeader("Authorization", equalTo("Bearer token"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(response))
        );
    }

    public StubMapping createModuleRecords(String expectedInput, String response) {
        return stubFor(
                WireMock.post(urlPathEqualTo("/learner_record_api/module_records/bulk"))
                        .withHeader("Authorization", equalTo("Bearer token"))
                        .withRequestBody(equalToJson(expectedInput, true, true))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(response))
        );
    }

    public StubMapping updateModuleRecords(String expectedInput, String response) {
        return stubFor(
                WireMock.put(urlPathEqualTo("/learner_record_api/module_records/bulk"))
                        .withHeader("Authorization", equalTo("Bearer token"))
                        .withRequestBody(equalToJson(expectedInput, true, true))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(response))
        );
    }

    public StubMapping getLearnerRecords(LearnerRecordQuery query, Integer page, String response) {
        MappingBuilder mappingBuilder = WireMock.get(urlPathEqualTo("/learner_record_api/learner_records"))
                .withQueryParam("size", equalTo("50"))
                .withQueryParam("page", equalTo(page.toString()));
        if (query.getLearnerIds() != null) {
            mappingBuilder.withQueryParam("learnerIds", including(query.getLearnerIds().toArray(String[]::new)));
        }
        if (query.getResourceIds() != null) {
            mappingBuilder.withQueryParam("resourceIds", including(query.getResourceIds().toArray(String[]::new)));
        }
        if (query.getLearnerRecordTypes() != null) {
            mappingBuilder.withQueryParam("learnerRecordTypes", including(query.getLearnerRecordTypes().toArray(String[]::new)));
        }
        if (query.getNotEventTypes() != null) {
            mappingBuilder.withQueryParam("notEventTypes", including(query.getNotEventTypes().toArray(String[]::new)));
        }
        return stubFor(
                mappingBuilder
                        .withHeader("Authorization", equalTo("Bearer token"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(response))
        );
    }

    public StubMapping getLearnerRecords(String userId, int page, String response) {
        LearnerRecordQuery query = LearnerRecordQuery.builder()
                .learnerIds(Set.of(userId))
                .build();
        return getLearnerRecords(query, page, response);
    }

    public StubMapping getLearnerRecords(String userId, String courseId, int page, String response) {
        LearnerRecordQuery query = LearnerRecordQuery.builder()
                .learnerIds(Set.of(userId)).resourceIds(Set.of(courseId))
                .build();
        return getLearnerRecords(query, page, response);
    }

    public StubMapping createLearnerRecords(String expectedEventPOST, String response) {
        return stubFor(
                WireMock.post(urlPathEqualTo("/learner_record_api/learner_records/bulk"))
                        .withRequestBody(equalToJson(expectedEventPOST))
                        .withHeader("Authorization", equalTo("Bearer token"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(response))
        );
    }

    public StubMapping createLearnerRecordEvent(String expectedEventPOST, String response) {
        return stubFor(
                WireMock.post(urlPathEqualTo("/learner_record_api/learner_record_events"))
                        .withRequestBody(equalToJson(expectedEventPOST))
                        .withHeader("Authorization", equalTo("Bearer token"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(response))
        );
    }

    public StubMapping bookEvent(String eventId, String expectedInput, BookingDto response) {
        return stubFor(
                WireMock.post(urlPathEqualTo(String.format("/learner_record_api/event/%s/booking/", eventId)))
                        .withHeader("Authorization", equalTo("Bearer token"))
                        .withRequestBody(equalToJson(
                                expectedInput
                        ))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(utils.toJson(response)))
        );
    }

    public StubMapping cancelBooking(String eventId, String userId, String expectedInput, String response) {
        return stubFor(
                WireMock.patch(urlPathEqualTo(String.format("/learner_record_api/event/%s/learner/%s", eventId, userId)))
                        .withHeader("Authorization", equalTo("Bearer token"))
                        .withRequestBody(equalToJson(expectedInput))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(response))
        );
    }

    public StubMapping updateBookingWithId(String eventId, Integer bookingId, String expectedInput, String response) {
        return stubFor(
                WireMock.patch(urlPathEqualTo(String.format("/learner_record_api/event/%s/booking/%s", eventId, bookingId)))
                        .withHeader("Authorization", equalTo("Bearer token"))
                        .withRequestBody(equalToJson(expectedInput))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(response))
        );
    }

    public StubMapping cancelEvent(String eventId, String expectedInput) {
        return stubFor(
                WireMock.patch(urlPathEqualTo(String.format("/learner_record_api/event/%s", eventId)))
                        .withHeader("Authorization", equalTo("Bearer token"))
                        .withRequestBody(equalToJson(expectedInput))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json"))
        );
    }

    public StubMapping createInvite(String eventId, String expectedInviteDto) {
        return stubFor(
                WireMock.post(urlPathEqualTo(String.format("/learner_record_api/event/%s/invitee", eventId)))
                        .withHeader("Authorization", equalTo("Bearer token"))
                        .withRequestBody(equalToJson(expectedInviteDto))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json"))
        );
    }

    public StubMapping getBookings(String eventId, String response) {
        return stubFor(
                WireMock.get(urlPathEqualTo(String.format("/learner_record_api/event/%s/booking", eventId)))
                        .withHeader("Authorization", equalTo("Bearer token"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(response))
        );
    }

    public StubMapping getLearnerRecordEvents(Integer page, LearnerRecordEventQuery query, String response) {
        MappingBuilder mappingBuilder = WireMock.get(urlPathEqualTo("/learner_record_api/learner_record_events"))
                .withQueryParam("userId", equalTo(query.getUserId()))
                .withQueryParam("size", equalTo("50"))
                .withQueryParam("page", equalTo(page.toString()));

        if (query.getEventTypes() != null) {
            mappingBuilder.withQueryParam("eventTypes", equalTo(String.join(",", query.getEventTypes())));
        }
        if (query.getResourceIds() != null) {
            query.getResourceIds().forEach(resourceId -> mappingBuilder.withQueryParam("resourceIds", equalTo(resourceId)));
//            mappingBuilder.withQueryParam("resourceIds", equalTo(String.join(",", query.getResourceIds())));
        }
        return stubFor(
                mappingBuilder
                        .withHeader("Authorization", equalTo("Bearer token"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(response))
        );
    }

    public StubMapping findEvent(String eventId, Boolean getBookings,
                                 Boolean getInvites, String response) {
        return stubFor(WireMock.get(urlPathEqualTo("/learner_record_api/event/" + eventId))
                .withQueryParam("getBookings", equalTo(getBookings.toString()))
                .withQueryParam("getInvites", equalTo(getInvites.toString()))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(response)));
    }

    public StubMapping getLearnerRecordPage(LearnerRecordQuery query, Integer page, Integer size, String response) {
        MappingBuilder mappingBuilder = WireMock.get(urlPathEqualTo("/learner_record_api/learner_records"))
                .withQueryParam("size", equalTo(size.toString()))
                .withQueryParam("page", equalTo(page.toString()));
        if (query.getLearnerIds() != null && !query.getLearnerIds().isEmpty()) {
            mappingBuilder.withQueryParam("learnerIds", including(query.getLearnerIds().toArray(String[]::new)));
        }
        if (query.getResourceIds() != null && !query.getResourceIds().isEmpty()) {
            mappingBuilder.withQueryParam("resourceIds", including(query.getResourceIds().toArray(String[]::new)));
        }
        if (query.getLearnerRecordTypes() != null && !query.getLearnerRecordTypes().isEmpty()) {
            mappingBuilder.withQueryParam("learnerRecordTypes", including(query.getLearnerRecordTypes().toArray(String[]::new)));
        }
        if (query.getNotEventTypes() != null && !query.getNotEventTypes().isEmpty()) {
            mappingBuilder.withQueryParam("notEventTypes", including(query.getNotEventTypes().toArray(String[]::new)));
        }
        if (query.getNotResourceIds() != null && !query.getNotResourceIds().isEmpty()) {
            mappingBuilder.withQueryParam("notResourceIds", including(query.getNotResourceIds().toArray(String[]::new)));
        }
        return stubFor(
                mappingBuilder
                        .withHeader("Authorization", equalTo("Bearer token"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(response))
        );
    }

    public StubMapping getLearnerRecordResourceIds(LearnerRecordQuery query, Integer page, Integer size, String response) {
        MappingBuilder mappingBuilder = WireMock.get(urlPathEqualTo("/learner_record_api/learner_records/resource_ids"))
                .withQueryParam("size", equalTo(size.toString()))
                .withQueryParam("page", equalTo(page.toString()));
        if (query.getLearnerIds() != null && !query.getLearnerIds().isEmpty()) {
            mappingBuilder.withQueryParam("learnerIds", including(query.getLearnerIds().toArray(String[]::new)));
        }
        if (query.getNotResourceIds() != null && !query.getNotResourceIds().isEmpty()) {
            mappingBuilder.withQueryParam("notResourceIds", including(query.getNotResourceIds().toArray(String[]::new)));
        }
        return stubFor(
                mappingBuilder
                        .withHeader("Authorization", equalTo("Bearer token"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(response))
        );
    }
}
