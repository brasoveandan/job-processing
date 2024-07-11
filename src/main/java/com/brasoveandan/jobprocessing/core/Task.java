package com.brasoveandan.jobprocessing.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Task(String name, String command,
                   @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) List<String> requires) {
}
