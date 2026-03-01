package com.personal.store_api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaginatedResponse<T> {
    List<T> items;
    int page;
    int size;
    long totalItems;
    int totalPages;
    boolean isFirst;
    boolean isLast;
    boolean hasNext;
    boolean hasPrevious;
}
