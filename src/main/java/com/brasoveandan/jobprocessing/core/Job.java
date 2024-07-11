package com.brasoveandan.jobprocessing.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Job(@JsonProperty("tasks") List<Task> tasks) {
}
