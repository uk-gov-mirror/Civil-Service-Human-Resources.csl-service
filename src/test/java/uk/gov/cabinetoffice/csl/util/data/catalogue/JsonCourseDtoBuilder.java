package uk.gov.cabinetoffice.csl.util.data.catalogue;

import uk.gov.cabinetoffice.csl.util.data.BaseJsonBuilder;

public class JsonCourseDtoBuilder extends BaseJsonBuilder {

    private final String courseId;

    public JsonCourseDtoBuilder(String courseId) {
        this.courseId = courseId;
    }

    public static JsonCourseDtoBuilder create(String courseId, String title) {
        JsonCourseDtoBuilder builder = new JsonCourseDtoBuilder(courseId);
        builder.root.put("id", courseId);
        builder.root.put("title", title);
        builder.root.put("shortDescription", String.format("%s short description", title));
        builder.root.put("status", "Published");
        return builder;
    }

}
