package com.app.crawlaptisenglish.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpeakingQuestion {
    private Long id;
    private String title;
    private String des;
    private String des_cuoi;
    private String file;
    @JsonProperty("LstCauHoi")
    private List<GroupQuestion> LstCauHoi;
}
