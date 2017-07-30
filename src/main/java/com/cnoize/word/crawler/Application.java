package com.cnoize.word.crawler;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import okhttp3.OkHttpClient;

@SpringBootApplication
public class Application {

    public static void main(final String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public RestTemplate restTemplate(final RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public OkHttpClient okHttpClient() {
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(1L, TimeUnit.MINUTES)
                .readTimeout(30L, TimeUnit.SECONDS)
                .build();
        return okHttpClient;
    }

    @Bean("root")
    public Path getRoot() {
        return Paths.get("C:\\Users\\sixu\\Documents\\words");
    }

    @Bean("source")
    public Path getSource() {
        return Paths.get(getRoot().toString(), "cet4.txt");
    }

    @Bean("success")
    public Path getSuccess() throws IOException {
        final Path result = Paths.get(getRoot().toString(), "success.txt");
        Files.deleteIfExists(result);
        Files.createFile(result);
        return result;
    }

    @Bean("error")
    public Path getError() throws IOException {
        final Path result = Paths.get(getRoot().toString(), "error.txt");
        Files.deleteIfExists(result);
        Files.createFile(result);
        return result;
    }
}