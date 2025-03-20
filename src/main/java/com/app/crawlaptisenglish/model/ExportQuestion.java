package com.app.crawlaptisenglish.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportQuestion {
    ExportQuestionListen listen;
    List<ReadingQuestion> readings;
    List<SpeakingQuestion> speakings;
    List<WritingQuestion> writings;
}
