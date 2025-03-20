package com.app.crawlaptisenglish.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListenQuestionSingle {
    private Long id;
    private String vanbancauhoi;
    private String vanbangiaithich;
    private String des;
    private String file;
    private String code;
    private String ma;
    private List<DapAn> lstdapan;

}
