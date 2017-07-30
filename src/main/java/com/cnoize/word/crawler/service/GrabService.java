package com.cnoize.word.crawler.service;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;

import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.cnoize.word.crawler.model.ShanbeiResponse;
import com.cnoize.word.crawler.model.ShanbeiWord;
import com.cnoize.word.crawler.model.ShangbeiExample;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by sixu on 17/6/21.
 */
@Component
public class GrabService {
    private final Logger logger = LoggerFactory.getLogger(GrabService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OkHttpClient okHttpClient;

    @Autowired
    @Qualifier("root")
    private Path root;

    @Autowired
    @Qualifier("source")
    private Path source;

    @Autowired
    @Qualifier("success")
    private Path success;

    @Autowired
    @Qualifier("error")
    private Path error;

    @Autowired
    private ObjectMapper objectMapper;

    public Optional<byte[]> downloadAudio(final String url) {
        final Request request = new Request.Builder().url(url).build();

        try (final Response response = this.okHttpClient.newCall(request).execute()) {
            final ResponseBody responseBody = response.body();
            final byte[] bytes = responseBody.bytes();
            return Optional.of(bytes);
        } catch (final IOException ioe) {
            logger.warn("url={}", url, ioe);
            return Optional.empty();
        }
    }

    private <T> Optional<T> shanbei(final String url, final ParameterizedTypeReference<ShanbeiResponse<T>> responseType, final Object... uriVariables) {
        final ResponseEntity<ShanbeiResponse<T>> responseEntity = this.restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, responseType, uriVariables);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            final ShanbeiResponse<T> shanbeiResponse = responseEntity.getBody();
            if (shanbeiResponse.isSuccessful()) {
                return Optional.of(shanbeiResponse.getData());
            }
        }
        return Optional.empty();
    }


    public List<ShangbeiExample> getExample(final long wordId) {
        final ParameterizedTypeReference<ShanbeiResponse<List<ShangbeiExample>>> ptr = new ParameterizedTypeReference<ShanbeiResponse<List<ShangbeiExample>>>() {
        };
        final Optional<List<ShangbeiExample>> result = this.shanbei("https://api.shanbay.com/bdc/example/?vocabulary_id={wordId}&type=sys",
                ptr, wordId);
        return result.orElseGet(() -> {
            return Collections.emptyList();
        });
    }

    public Optional<ShanbeiWord> getWord(final String word) {
        final ParameterizedTypeReference<ShanbeiResponse<ShanbeiWord>> ptr = new ParameterizedTypeReference<ShanbeiResponse<ShanbeiWord>>() {
        };
        return this.shanbei("https://api.shanbay.com/bdc/search/?word={word}", ptr, word);
    }

    @PostConstruct
    public void grab() {
        try (final Stream<String> lines = Files.lines(this.source)) {
            final List<String> result = new ArrayList<>();
            lines.forEach(word -> {
                final String wordContent = StringUtils.trimWhitespace(word);
                this.grabWord(wordContent);
                result.add(wordContent);
            });
            this.logger.info(this.objectMapper.writeValueAsString(result));
        } catch (final IOException ioe) {
            logger.error("", ioe);
        }
    }

    public Optional<Map<String, Object>> grabWord(final String word) {
        final Optional<ShanbeiWord> optional = this.getWord(word);
        try {
            if (optional.isPresent()) {
                final Path wordDir = this.createWordDir(word);

                final ShanbeiWord shanbeiWord = optional.get();

                final Map<String, Object> result = new HashMap<>();
                final long id = shanbeiWord.getId();
                result.put("id", id);
                result.put("content", word);
                result.put("examples", this.getExample(id));

                result.put("pronunciation", this.downloadAudio(shanbeiWord, wordDir));
                result.put("definition", this.downloadDefinition(shanbeiWord));

                Files.write(Paths.get(wordDir.toString(), word + ".json"), this.objectMapper.writeValueAsBytes(result));

                Files.write(this.success, ImmutableList.of(word), StandardOpenOption.APPEND);

                return Optional.of(result);
            }
            Files.write(this.error, ImmutableList.of(word), StandardOpenOption.APPEND);
        } catch (final IOException ioe) {
            logger.warn("", ioe);
        }
        return Optional.empty();
    }

    private Path createWordDir(final String word) {
        final Path result = Paths.get(this.root.toString(), word.substring(0, 1), word);

        try {
            Files.createDirectories(result);
        } catch (final IOException ioe) {
            logger.warn("", ioe);
        }

        return result;
    }

    private Map<String, Map<String, List<String>>> downloadDefinition(final ShanbeiWord shanbeiWord) {
        final Map<String, Map<String, List<String>>> result = new HashMap<>();

        if (!CollectionUtils.isEmpty(shanbeiWord.getEnDefinitions())) {
            result.put("en", shanbeiWord.getEnDefinitions());
        }

        final String cn = StringUtils.trimWhitespace(shanbeiWord.getCnDefinition().get("defn"));
        if (!StringUtils.isEmpty(cn)) {
            final Map<String, List<String>> cnMap = Splitter.on("\n")
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(cn)
                    .stream()
                    .collect(Collectors.toMap(
                            value -> {
                                final List<String> list = Splitter.on(".")
                                        .omitEmptyStrings()
                                        .trimResults()
                                        .splitToList(value);

                                final int size = list.size();
                                if (size <= 1) {
                                    return "";
                                }

                                return Joiner.on(".").join(list.subList(0, size - 1));
                            },
                            value -> {
                                final List<String> list = Splitter.on(".")
                                        .omitEmptyStrings()
                                        .trimResults()
                                        .splitToList(value);
                                final int size = list.size();
                                if (size <= 1) {
                                    return ImmutableList.of(value);
                                }
                                return ImmutableList.of(list.get(size - 1));
                            }
                    ));
            result.put("cn", cnMap);
        }

        return result;
    }

    private Map<String, Map<String, String>> downloadAudio(final ShanbeiWord shanbeiWord, final Path wordDir) throws IOException {
        final Map<String, Map<String, String>> result = new HashMap<>();

        final String word = shanbeiWord.getContent();

        final String ukAudio = shanbeiWord.getUkAudio();
        if (ukAudio != null) {
            final Optional<byte[]> optional = this.downloadAudio(ukAudio);
            if (optional.isPresent()) {
                final String audioName = word + "-uk.mp3";
                Files.write(Paths.get(wordDir.toString(), audioName), optional.get());
                result.put("uk", ImmutableMap.of(
                        "phonetic", shanbeiWord.getPronunciations().get("uk"),
                        "audio", audioName
                ));
            }
        }

        final String usAudio = shanbeiWord.getUsAudio();
        if (usAudio != null) {
            final Optional<byte[]> optional = this.downloadAudio(usAudio);
            if (optional.isPresent()) {
                final String audioName = word + "-us.mp3";
                Files.write(Paths.get(wordDir.toString(), audioName), optional.get());
                result.put("us", ImmutableMap.of(
                        "phonetic", shanbeiWord.getPronunciations().get("us"),
                        "audio", audioName
                ));
            }
        }

        return result;
    }
}
