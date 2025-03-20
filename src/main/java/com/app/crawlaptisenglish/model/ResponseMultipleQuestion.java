package com.app.crawlaptisenglish.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseMultipleQuestion {
    private String status;
    private ListenQuestionMultiple data;
}
