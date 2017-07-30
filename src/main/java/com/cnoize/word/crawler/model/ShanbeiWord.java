package com.cnoize.word.crawler.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by sixu on 17/6/21.
 */
public class ShanbeiWord {
    @JsonProperty("id")
    private long id;

    @JsonProperty("pronunciations")
    private Map<String, String> pronunciations;

    @JsonProperty("en_definitions")
    private Map<String, List<String>> enDefinitions;

    @JsonProperty("cn_definition")
    private Map<String, String> cnDefinition;

    @JsonProperty("us_audio")
    private String usAudio;

    @JsonProperty("uk_audio")
    private String ukAudio;

    @JsonProperty("content")
    private String content;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Map<String, String> getPronunciations() {
        return pronunciations;
    }

    public void setPronunciations(final Map<String, String> pronunciations) {
        this.pronunciations = pronunciations;
    }

    public Map<String, List<String>> getEnDefinitions() {
        return enDefinitions;
    }

    public void setEnDefinitions(final Map<String, List<String>> enDefinitions) {
        this.enDefinitions = enDefinitions;
    }

    public Map<String, String> getCnDefinition() {
        return cnDefinition;
    }

    public void setCnDefinition(final Map<String, String> cnDefinition) {
        this.cnDefinition = cnDefinition;
    }

    public String getUsAudio() {
        return usAudio;
    }

    public void setUsAudio(final String usAudio) {
        this.usAudio = usAudio;
    }

    public String getUkAudio() {
        return ukAudio;
    }

    public void setUkAudio(final String ukAudio) {
        this.ukAudio = ukAudio;
    }

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }
}
