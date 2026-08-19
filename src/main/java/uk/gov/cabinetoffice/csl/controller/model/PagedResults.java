package uk.gov.cabinetoffice.csl.controller.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PagedResults<T> extends Results<T> {
    protected Integer page;
    protected Integer size;
    protected Integer totalResults;

    public PagedResults(List<T> results, Integer page, Integer size, Integer totalResults) {
        super(results);
        this.page = page;
        this.size = size;
        this.totalResults = totalResults;
    }

    public static <R> PagedResults<R> emptyResults() {
        return new PagedResults<>(List.of(), 0, 0, 0);
    }
}
