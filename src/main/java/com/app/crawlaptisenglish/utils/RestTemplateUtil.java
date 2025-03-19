package com.app.crawlaptisenglish.utils;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@UtilityClass
public class RestTemplateUtil {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    FormHttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter();

    private final RestTemplate restTemplate = new RestTemplateBuilder()
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE, MediaType.APPLICATION_XHTML_XML_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.CACHE_CONTROL, "max-age=0")
            .defaultHeader(HttpHeaders.CONNECTION, "keep-alive")
            .defaultHeader("Keep-Alive", "timeout=5, max=100")
            .defaultHeader(HttpHeaders.ACCEPT_CHARSET, "ISO-8859-1,utf-8;q=0.7,*;q=0.3")
            .defaultHeader(HttpHeaders.ACCEPT_LANGUAGE, "es-ES,es;q=0.8")
            .defaultHeader(HttpHeaders.PRAGMA, "")
            .defaultHeader(HttpHeaders.COOKIE, "csrf_cookie_name=1a79e00c9080199a3cc80175dc5a9f45; ci_session=is7iniup8renh8iaksakff4bqd5jehj2")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOiI0MTk4NCIsInVzZXJuYW1lIjoiTkdVWVx1MWVjNE4gVEhcdTFlY2EgVEhVIEhPXHUwMGMwSSIsImZ1bGxuYW1lIjoiTkdVWVx1MWVjNE4gVEhcdTFlY2EgVEhVIEhPXHUwMGMwSSIsImVtYWlsIjoiaG9haW50dDEwMTk5My5jMWFzQG5naGVhbi5lZHUudm4iLCJuZ2F5X3NpbmgiOm51bGwsInBob25lIjoiMDk0MzAxODY2OSIsImNsYXNzX2lkIjoiMCIsImV4cCI6MTc0MjM5NjI5NCwiQVBJX1RJTUUiOjE3NDIzNTMwOTR9.G3t_u7WpqybwDbfuSYU1FWNiLxVvDHmcReDXH1RDWSs")
            .additionalMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper), formHttpMessageConverter)
            .build();


    public static <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType) {
        return restTemplate.postForEntity(url, request, responseType);
    }

    public static <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Object request) {
        return restTemplate.getForEntity(url, responseType, request);
    }
}
