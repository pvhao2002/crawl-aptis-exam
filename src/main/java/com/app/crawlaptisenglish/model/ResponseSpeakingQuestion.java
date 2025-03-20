package com.app.crawlaptisenglish.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseSpeakingQuestion {
    private String status;
    private SpeakingQuestion data;
}
