package com.app.crawlaptisenglish.controller;

import com.app.crawlaptisenglish.model.AllTest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.java.Log;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStreamReader;
import java.util.List;

@Log
@RestController
@RequestMapping("/process")
public class CrawlController {
    private final Gson gson = new Gson();
    private static final String URL_LAY_CAI_HOI = "https://luyenthi.aptistests.vn/api/api/laycauhoi?typeExam={0}&isthitructuyen=1&userId=41984&IdDeThi={1}";

    @GetMapping
    public Object processCrawl() {
        try {
            var resource = new ClassPathResource("getalldethi.json");
            var reader = new InputStreamReader(resource.getInputStream());

            // Xác định kiểu danh sách
            var listType = new TypeToken<List<AllTest>>() {
            }.getType();

            // Parse JSON thành danh sách Exam
            List<AllTest> allTest = gson.fromJson(reader, listType);
            var firstTest = allTest.stream().findFirst().orElse(null);
            return firstTest;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "OK";
    }

}
