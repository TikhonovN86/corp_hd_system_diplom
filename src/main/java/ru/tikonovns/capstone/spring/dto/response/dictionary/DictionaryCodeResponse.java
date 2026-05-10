package ru.tikonovns.capstone.spring.dto.response.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DictionaryCodeResponse {

    private String code;
    private String name;
}