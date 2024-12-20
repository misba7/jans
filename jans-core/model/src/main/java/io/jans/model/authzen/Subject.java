package io.jans.model.authzen;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serializable;

/**
 * @author Yuriy Z
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Subject implements Serializable {

    @JsonProperty("id")
    private String id;

    @JsonProperty("type")
    private String type;

    @JsonProperty("properties")
    private JsonNode properties;

    public String getId() {
        return id;
    }

    public Subject setId(String id) {
        this.id = id;
        return this;
    }

    public String getType() {
        return type;
    }

    public Subject setType(String type) {
        this.type = type;
        return this;
    }

    public JsonNode getProperties() {
        return properties;
    }

    public Subject setProperties(JsonNode properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public String toString() {
        return "Subject{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", properties=" + properties +
                '}';
    }
}
