package com.app.crawlaptisenglish.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TakeQuestion {
    private String status;
    private List<Object> dataId;
}
