package com.app.crawlaptisenglish.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportQuestionListen {
    List<ListenQuestionSingle> singles = new ArrayList<>();
    List<ListenQuestionMultiple> multiples = new ArrayList<>();
}
