package com.app.crawlaptisenglish.controller;

import com.app.crawlaptisenglish.model.*;
import com.app.crawlaptisenglish.utils.RestTemplateUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Log
@RestController
@RequestMapping("/process")
public class CrawlController {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private static final String URL_LAY_CAI_HOI = "https://luyenthi.aptistests.vn/api/api/laycauhoi?typeExam={0}&isthitructuyen=1&userId=41984&IdDeThi={1}";
    private static final String URL_DETAIL_QUESTION = "https://luyenthi.aptistests.vn/api/api/detailcauhoi?id={0}&userId=41984";
    private static final String URL_DETAIL_QUESTION_GROUP = "https://luyenthi.aptistests.vn/api/api/getcauhoibyidnhom?id={0}&userId=41984&isthitructuyen=1";
    private static final List<String> EXCEL_HEADER_NGHE = List.of(
            "Description",
            "Question ID",
            "Content",
            "Audio",
            "Answer",
            "Script"
    );
    private static final String DEFAULT_WRITE = "src/main/resources/%s";

    @GetMapping("export")
    public Object export() throws IOException {
        var resource2 = new ClassPathResource("getalldethi.json");
        var reader2 = new InputStreamReader(resource2.getInputStream());
        var listType = new TypeToken<List<AllTest>>() {
        }.getType();

        // Parse JSON thành danh sách Exam
        List<AllTest> allTest = gson.fromJson(reader2, listType);
        allTest.forEach(firstTest -> {
            var folder = DEFAULT_WRITE.formatted(removeAccents(firstTest.getName()));
            var pdfName = "%d_%s.pdf".formatted(firstTest.getId(), removeAccents(firstTest.getName()));
            try (var document = new Document()) {
                Files.createDirectories(Paths.get(folder));
                String pdfPath = Paths.get(folder, pdfName).toString();
                PdfWriter.getInstance(document, new FileOutputStream(pdfPath));
                var resource = new ClassPathResource(MessageFormat.format(
                        "{0}_{1}.json",
                        firstTest.getId(),
                        removeAccents(firstTest.getName())
                ));
                var reader = new InputStreamReader(resource.getInputStream());

                ExportQuestion data = gson.fromJson(reader, ExportQuestion.class);
                document.open();
                Font titleFont = new Font(Font.HELVETICA, 14, Font.BOLD);
                Font normalFont = new Font(Font.HELVETICA, 12, Font.NORMAL);

                // NGHE
                document.add(new Paragraph("PHAN NGHE", titleFont));
                int questionIndex = 1;
                for (ListenQuestionSingle question : data.getListen().getSingles()) {
                    document.add(new Paragraph(
                            "Question " + questionIndex + " (File nghe " + question.getId() + "):",
                            titleFont
                    ));
                    document.add(new Paragraph(question.getVanbancauhoi(), normalFont));

                    if (question.getLstdapan() != null) {
                        for (int i = 0; i < question.getLstdapan().size(); i++) {
                            document.add(new Paragraph(
                                    (i + 1) + ". " + question.getLstdapan().get(i).getVanbanphanhoi(),
                                    normalFont
                            ));
                        }
                    }
                    document.add(new Paragraph("\n")); // Xuống dòng
                    questionIndex++;
                    saveFile(question.getFile(), folder, "%d.mp3".formatted(question.getId()));
                }

                for (var mq : data.getListen().getMultiples()) {
                    document.add(new Paragraph(
                            "Description: " + Jsoup.parse(StringEscapeUtils.unescapeJava(mq.getDes())).text(),
                            titleFont
                    ));
                    if (StringUtils.isNotEmpty(mq.getFile())) {
                        document.add(new Paragraph(
                                "Question " + questionIndex + " (File nghe " + mq.getId() + ")",
                                titleFont
                        ));
                    } else {
                        document.add(new Paragraph("Question " + questionIndex, titleFont));
                    }
                    for (var q : mq.getLstCauHoi()) {
                        var isHaveAudio = StringUtils.isNotEmpty(q.getFile());

                        document.add(new Paragraph(
                                Jsoup.parse(StringEscapeUtils.unescapeJava(q.getVanbancauhoi()))
                                     .text() + (isHaveAudio ? " (File nghe " + q.getId() + ")" : ""),
                                titleFont
                        ));
                        if (q.getLstdapan() != null) {
                            for (int i = 0; i < q.getLstdapan().size(); i++) {
                                document.add(new Paragraph(
                                        (i + 1) + ". " +
                                        Jsoup.parse(StringEscapeUtils.unescapeJava(
                                                     q.getLstdapan()
                                                      .get(i)
                                                      .getVanbanphanhoi()))
                                             .text(),
                                        normalFont
                                ));
                            }
                        }
                        document.add(new Paragraph("\n"));
                    }
                    questionIndex++;
                    document.add(new Paragraph("\n"));
                }

                // PHAN DOC
                document.add(new Paragraph("PHAN DOC", titleFont));
                for (var q : data.getReadings()) {
                    document.add(new Paragraph("Question " + questionIndex, titleFont));
                    document.add(new Paragraph("Description: " + Jsoup.parse(StringEscapeUtils.unescapeJava(q.getDes()))
                                                                      .text(), titleFont));
                    for (int qidx = 0; qidx < q.getLstCauHoi().size(); qidx++) {
                        document.add(new Paragraph(
                                Jsoup.parse(StringEscapeUtils.unescapeJava(q.getLstCauHoi()
                                                                            .get(qidx)
                                                                            .getVanbancauhoi())).text(),
                                titleFont
                        ));

                        for (int j = 0; j < q.getLstCauHoi().get(qidx).getLstdapan().size(); j++) {
                            document.add(new Paragraph(((j + 1) + ". " + q.getLstCauHoi()
                                                                          .get(qidx)
                                                                          .getLstdapan()
                                                                          .get(j)
                                                                          .getVanbanphanhoi())));
                        }
                        document.add(new Paragraph("\n"));
                    }
                    questionIndex++;
                    document.add(new Paragraph("\n"));
                }

                // PHAN SPEAK
                document.add(new Paragraph("PHAN NOI", titleFont));
                for (var q : data.getSpeakings()) {
                    document.add(new Paragraph(
                            "Question " + questionIndex + " (File nghe %s)".formatted(q.getId()),
                            titleFont
                    ));
                    document.add(new Paragraph("Description: %s".formatted(Jsoup.parse(StringEscapeUtils.unescapeJava(q.getDes()))
                                                                                .text()), normalFont));
                    document.add(new Paragraph("\n"));
                    for (int i = 0; i < q.getLstCauHoi().size(); i++) {
                        var q1 = q.getLstCauHoi().get(i);
                        var html = Jsoup.parse(StringEscapeUtils.unescapeJava(q1.getVanbancauhoi()));
                        document.add(new Paragraph(
                                html.text(),
                                titleFont
                        ));
                        var imgTags = html.select("img");
                        for (var imgTag : imgTags) {
                            String imgUrl = imgTag.attr("src");
                            if (!imgUrl.isEmpty()) {
                                try {
                                    String localPath = downloadImage(imgUrl, folder);

                                    // Thêm ảnh vào PDF từ file
                                    Image image = Image.getInstance(localPath);
                                    image.scaleToFit(400, 300); // Resize ảnh
                                    document.add(image);
                                } catch (Exception e) {
                                    document.add(new Paragraph("Error loading image: " + imgUrl, normalFont));
                                }
                            }
                        }
                        document.add(new Paragraph("File nghe: " + q1.getId(), titleFont));
                        document.add(new Paragraph("Suggest: " + Jsoup.parse(StringEscapeUtils.unescapeJava(q1.getVanbangiaithich()))
                                                                      .text(), normalFont));
                        document.add(new Paragraph("\n"));
                    }
                }

                // PHAN VIET
                document.add(new Paragraph("WRITING", titleFont));
                document.add(new Paragraph("\n"));
                for (var q : data.getWritings()) {
                    document.add(new Paragraph("Description: %s".formatted(Jsoup.parse(StringEscapeUtils.unescapeJava(q.getDes()))
                                                                                .text()), normalFont));
                    document.add(new Paragraph("\n"));

                    for (int i = 0; i < q.getLstCauHoi().size(); i++) {
                        var q1 = q.getLstCauHoi().get(i);
                        document.add(new Paragraph(
                                Jsoup.parse(StringEscapeUtils.unescapeJava(q1.getVanbancauhoi())).text(),
                                titleFont
                        ));
                        for (int j = 0; j < q1.getLstdapan().size(); j++) {
                            var q3 = q1.getLstdapan().get(j);
                            document.add(new Paragraph("Suggest: %s".formatted(Jsoup.parse(StringEscapeUtils.unescapeJava(
                                                                                            q3.getVanbanphanhoi()))
                                                                                    .text()), normalFont));
                        }
                    }
                    document.add(new Paragraph("\n"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return "OK";
    }

    private void saveFile(String url, String folder, String fileName) {
        String filePath = folder + "/" + fileName;
        try (var in = new URL(url).openStream()) {
            Files.copy(
                    in,
                    Paths.get(filePath),
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String downloadImage(String imageUrl, String folderPath) throws IOException {
        Files.createDirectories(Paths.get(folderPath)); // Tạo thư mục nếu chưa có

        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        String filePath = folderPath + "/" + fileName;

        try (var in = new URL(imageUrl).openStream()) {
            Files.copy(in, Paths.get(filePath));
        }

        return filePath;
    }


    private void exportExcelListen(Workbook workbook, ExportQuestionListen data) {
        var sheetNghe = workbook.createSheet(TypeExam.NGHE.name());
        var headerRow = sheetNghe.createRow(0);
        var cellStyle = workbook.createCellStyle();
        var font = workbook.createFont();
        font.setBold(true);
        cellStyle.setFont(font);
        for (int i = 0; i < EXCEL_HEADER_NGHE.size(); i++) {
            var cell = headerRow.createCell(i);
            cell.setCellValue(EXCEL_HEADER_NGHE.get(i));
            cell.setCellStyle(cellStyle);
        }
        data.getSingles().forEach(e -> {
            var row = sheetNghe.createRow(sheetNghe.getLastRowNum() + 1);
            row.createCell(0).setCellValue("");
            row.createCell(1).setCellValue(e.getId());
            row.createCell(2).setCellValue(e.getVanbancauhoi());
            row.createCell(3).setCellValue(e.getFile());
            row.createCell(4).setCellValue(getAnswer(e.getLstdapan()));
        });

        data.getMultiples().forEach(e -> {
            var row = sheetNghe.createRow(sheetNghe.getLastRowNum() + 1);
            row.createCell(0).setCellValue(Jsoup.parse(StringEscapeUtils.unescapeJava(e.getDes())).text());
            row.createCell(1).setCellValue("");
            row.createCell(2).setCellValue("");
            if (StringUtils.isNotEmpty(e.getFile())) {
                row.createCell(3).setCellValue(e.getFile());
            }
            row.createCell(4).setCellValue("");
            e.getLstCauHoi().forEach(el -> {
                var row1 = sheetNghe.createRow(sheetNghe.getLastRowNum() + 1);
                row1.createCell(0).setCellValue(el.getDes());
                row1.createCell(1).setCellValue(el.getId());
                row1.createCell(2).setCellValue(Jsoup.parse(el.getVanbancauhoi()).text());
                row1.createCell(3).setCellValue(el.getFile());
                row1.createCell(4).setCellValue(getAnswer(el.getLstdapan()));
                row1.createCell(5)
                    .setCellValue(Jsoup.parse(StringEscapeUtils.unescapeJava(el.getVanbangiaithich())).text());
            });
        });
        EXCEL_HEADER_NGHE.forEach(e -> sheetNghe.autoSizeColumn(EXCEL_HEADER_NGHE.indexOf(e)));
    }

    private String getAnswer(List<DapAn> dapAns) {
        return IntStream.range(0, dapAns.size())
                        .mapToObj(i -> (i + 1) + ". " + dapAns.get(i).getVanbanphanhoi())
                        .collect(Collectors.joining("\n"));
    }

    @GetMapping
    public Object processCrawl(@RequestParam(value = "stt", defaultValue = "1") Long stt) {
        var dataExport = new ExportQuestion();
        try (var workbook = new XSSFWorkbook()) {
            var resource = new ClassPathResource("getalldethi.json");
            var reader = new InputStreamReader(resource.getInputStream());
            var listType = new TypeToken<List<AllTest>>() {
            }.getType();

            // Parse JSON thành danh sách Exam
            List<AllTest> allTest = gson.fromJson(reader, listType);
            var firstTest = allTest.stream().filter(e -> Objects.equals(e.getStt(), stt)).findFirst().orElseThrow();

            // Chạy song song cả hai hàm crawlListen và crawlReading
            CompletableFuture<ExportQuestionListen> listenFuture = CompletableFuture.supplyAsync(
                    () -> crawlListen(firstTest.getId()), executor);
            CompletableFuture<List<ReadingQuestion>> readingFuture = CompletableFuture.supplyAsync(
                    () -> crawlReading(firstTest.getId()), executor);
            CompletableFuture<List<SpeakingQuestion>> speakingFuture = CompletableFuture.supplyAsync(
                    () -> crawlSpeaking(firstTest.getId()), executor);
            CompletableFuture<List<WritingQuestion>> writingFuture = CompletableFuture.supplyAsync(
                    () -> crawlWriting(firstTest.getId()), executor);

            // Chờ cả hai task hoàn thành trước khi tiếp tục
            CompletableFuture.allOf(listenFuture, readingFuture, speakingFuture, writingFuture).join();

            // Lấy kết quả sau khi tất cả task hoàn thành
            dataExport.setListen(listenFuture.get());
            dataExport.setReadings(readingFuture.get());
            dataExport.setSpeakings(speakingFuture.get());
            dataExport.setWritings(writingFuture.get());
            // Kiểm tra dữ liệu trước khi ghi
            if (dataExport.getListen() == null || dataExport.getReadings() == null) {
                throw new IllegalStateException("DataExport chưa có dữ liệu đầy đủ!");
            }
            try (var writer = new FileWriter(MessageFormat.format(
                    "src/main/resources/{0}_{1}.json",
                    firstTest.getId(),
                    removeAccents(firstTest.getName())
            ))) {
                gson.toJson(dataExport, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "OK";
    }

    private List<WritingQuestion> crawlWriting(Long testId) {
        List<WritingQuestion> writings = Collections.synchronizedList(new ArrayList<>());
        ResponseEntity<TakeQuestion> listCauHoi = RestTemplateUtil.getForEntity(
                MessageFormat.format(URL_LAY_CAI_HOI, TypeExam.VIET.name(), testId),
                TakeQuestion.class, null
        );

        listCauHoi.getBody();
        var listIdQuestion = listCauHoi.getBody().getDataId();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        listIdQuestion.forEach(e -> {
            if (e instanceof Map<?, ?> m) {
                var idQ = m.get("idnhom");
                if (idQ != null) {
                    CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
                        ResponseEntity<ResponseWriting> response = RestTemplateUtil.getForEntity(
                                MessageFormat.format(URL_DETAIL_QUESTION_GROUP, idQ.toString()),
                                ResponseWriting.class, null
                        );
                        response.getBody();
                        return response.getBody().getData();
                    }, executor).thenAccept(question -> {
                        if (question != null) {
                            writings.add(question);
                        }
                    });
                    futures.add(future);
                }
            }
        });

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return writings;
    }

    private List<SpeakingQuestion> crawlSpeaking(Long testId) {
        List<SpeakingQuestion> speakingQuestions = Collections.synchronizedList(new ArrayList<>());
        ResponseEntity<TakeQuestion> listCauHoi = RestTemplateUtil.getForEntity(
                MessageFormat.format(URL_LAY_CAI_HOI, TypeExam.NOI.name(), testId),
                TakeQuestion.class, null
        );

        listCauHoi.getBody();
        var listIdQuestion = listCauHoi.getBody().getDataId();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        listIdQuestion.forEach(e -> {
            if (e instanceof Map<?, ?> m) {
                var idQ = m.get("idnhom");
                if (idQ != null) {
                    CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
                        ResponseEntity<ResponseSpeakingQuestion> response = RestTemplateUtil.getForEntity(
                                MessageFormat.format(URL_DETAIL_QUESTION_GROUP, idQ.toString()),
                                ResponseSpeakingQuestion.class, null
                        );
                        response.getBody();
                        return response.getBody().getData();
                    }, executor).thenAccept(question -> {
                        if (question != null) {
                            speakingQuestions.add(question);
                        }
                    });
                    futures.add(future);
                }
            }
        });

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return speakingQuestions;
    }

    // ========== FIX crawlReading ==========
    private List<ReadingQuestion> crawlReading(Long testId) {
        List<ReadingQuestion> readingQuestions = Collections.synchronizedList(new ArrayList<>());

        ResponseEntity<TakeQuestion> listCauHoi = RestTemplateUtil.getForEntity(
                MessageFormat.format(URL_LAY_CAI_HOI, TypeExam.DOC.name(), testId),
                TakeQuestion.class, null
        );

        listCauHoi.getBody();
        var listIdQuestion = listCauHoi.getBody().getDataId();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        listIdQuestion.forEach(e -> {
            if (e instanceof Map<?, ?> m) {
                var idQ = m.get("idnhom");
                if (idQ != null) {
                    CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
                        ResponseEntity<ResponseReadingQuestion> response = RestTemplateUtil.getForEntity(
                                MessageFormat.format(URL_DETAIL_QUESTION_GROUP, idQ.toString()),
                                ResponseReadingQuestion.class, null
                        );
                        response.getBody();
                        return response.getBody().getData();
                    }, executor).thenAccept(question -> {
                        if (question != null) {
                            readingQuestions.add(question);
                        }
                    });

                    futures.add(future);
                }
            }
        });

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return readingQuestions;
    }

    // ========== FIX crawlListen ==========
    private ExportQuestionListen crawlListen(Long testId) {
        var dataListen = new ExportQuestionListen();
        List<ListenQuestionSingle> singleQuestions = Collections.synchronizedList(new ArrayList<>());
        List<ListenQuestionMultiple> multipleQuestions = Collections.synchronizedList(new ArrayList<>());

        ResponseEntity<TakeQuestion> listCauHoi = RestTemplateUtil.getForEntity(
                MessageFormat.format(URL_LAY_CAI_HOI, TypeExam.NGHE.name(), testId),
                TakeQuestion.class, null
        );

        listCauHoi.getBody();
        var listIdQuestion = listCauHoi.getBody().getDataId();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        listIdQuestion.forEach(e -> {
            if (e instanceof String single) {
                CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
                    ResponseEntity<ResponseSingleQuestion> response = RestTemplateUtil.getForEntity(
                            MessageFormat.format(URL_DETAIL_QUESTION, single),
                            ResponseSingleQuestion.class, null
                    );
                    response.getBody();
                    return response.getBody().getData();
                }, executor).thenAccept(question -> {
                    if (question != null) {
                        singleQuestions.add(question);
                    }
                });

                futures.add(future);
            } else if (e instanceof Map<?, ?> m) {
                var idQ = m.get("idnhom");
                if (idQ != null) {
                    CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
                        ResponseEntity<ResponseMultipleQuestion> response = RestTemplateUtil.getForEntity(
                                MessageFormat.format(URL_DETAIL_QUESTION_GROUP, idQ.toString()),
                                ResponseMultipleQuestion.class, null
                        );
                        response.getBody();
                        return response.getBody().getData();
                    }, executor).thenAccept(multipleQuestion -> {
                        if (multipleQuestion != null) {
                            multipleQuestions.add(multipleQuestion);
                        }
                    });

                    futures.add(future);
                }
            }
        });

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        dataListen.setSingles(singleQuestions);
        dataListen.setMultiples(multipleQuestions);
        return dataListen;
    }

    public String removeAccents(String input) {
        if (input == null) return null;

        // Chuẩn hóa chuỗi về dạng NFD (tách ký tự có dấu thành ký tự gốc + dấu)
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);

        // Xóa tất cả các dấu bằng regex (chỉ giữ lại ký tự gốc)
        String noAccents = normalized.replaceAll("\\p{M}", "");

        // Chuyển đổi riêng 'Đ' và 'đ' (vì Normalizer không đổi)
        noAccents = noAccents.replace("Đ", "D").replace("đ", "d");

        // Xóa khoảng trắng
        return noAccents.replaceAll("\\s+", "");
    }


    @GetMapping("t")
    public Object test() {
        return RestTemplateUtil.getForEntity(
                MessageFormat.format(URL_DETAIL_QUESTION_GROUP, 633),
                ResponseMultipleQuestion.class, null
        );
    }


}
