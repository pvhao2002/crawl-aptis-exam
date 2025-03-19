package com.app.crawlaptisenglish.controller;

import com.app.crawlaptisenglish.model.AllTest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jsoup.Jsoup;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStreamReader;
import java.util.List;

@RestController
@RequestMapping("exam")
public class ExamController {
    private final Gson gson = new Gson();

    @GetMapping("test")
    public Object test() {
        String test = """
                <strong><strong>Read the email from Anna to her friend, Tom. Choose one word from the list for each gap. The first one is done for you.<br></strong></strong>\\r\\n<p>Dear Tom,</p>\\r\\n<p>I hope you are <em><strong>well (example)</strong></em>!</p>
                """;
        var ele = Jsoup.parse(test);
        return ele.text();
    }

    @GetMapping
    public Object init() {
        try {
            // Đọc file từ resources
            var resource = new ClassPathResource("getalldethi.json");
            var reader = new InputStreamReader(resource.getInputStream());

            // Xác định kiểu danh sách
            var listType = new TypeToken<List<AllTest>>() {
            }.getType();

            // Parse JSON thành danh sách Exam
            return gson.fromJson(reader, listType);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi đọc file JSON", e);
        }
    }
}
