package com.brasoveandan.jobprocessing.core;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class JobProcessorService {
    private final TasksSorter tasksSorter;

    /**
     * @param job a list of unsorted tasks.
     * @return a list of ordered tasks
     */
    public List<Task> orderTasks(Job job) {
        return tasksSorter.sortTasks(job.tasks());
    }

    /**
     * Sorts the tasks and then returns a bash script containing the tasks commands in order.
     *
     * @param orderedTasks a list of sorted tasks.
     * @return a bash script.
     */
    public String generateBashScript(List<Task> orderedTasks) {
        final var bashScript = new StringBuilder("#!/usr/bin/env bash \n");
        for (Task task : orderedTasks) {
            bashScript.append(task.command()).append("\n");
        }
        return bashScript.toString();
    }
}
