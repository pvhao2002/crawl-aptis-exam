package com.app.crawlaptisenglish.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResponseSingleQuestion {
    private String status;
    private ListenQuestionSingle data;
}
