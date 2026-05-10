package ru.tikonovns.capstone.spring.dto.response.dictionary;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class DictionaryItemResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
}
