package com.brasoveandan.jobprocessing.core;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Component
public class TasksSorter {

    /**
     * Sorts the list of tasks based on their dependencies.
     *
     * @param tasks a list of unsorted tasks.
     * @return a list of ordered tasks.
     */
    public List<Task> sortTasks(List<Task> tasks) {
        log.debug("Starting task sorting process.");

        Map<String, Task> taskMap = createTaskMap(tasks);
        Map<String, List<String>> dependencyGraph = createDependencyGraph(tasks);
        Map<String, Integer> inDegree = calculateInDegrees(dependencyGraph);

        List<Task> sortedTasks = performTopologicalSort(taskMap, dependencyGraph, inDegree);

        log.debug("Task sorting process completed successfully.");
        return sortedTasks;
    }

    private Map<String, Task> createTaskMap(List<Task> tasks) {
        return tasks.stream()
                .collect(Collectors.toMap(Task::name, task -> task));
    }

    private Map<String, List<String>> createDependencyGraph(List<Task> tasks) {
        return tasks.stream()
                .collect(Collectors.toMap(
                        Task::name,
                        task -> tasks.stream()
                                .filter(t -> t.requires() != null && t.requires().contains(task.name()))
                                .map(Task::name)
                                .collect(Collectors.toList())
                ));
    }

    private Map<String, Integer> calculateInDegrees(Map<String, List<String>> dependencyGraph) {
        Map<String, Integer> inDegree = new HashMap<>();
        dependencyGraph.keySet().forEach(task -> inDegree.put(task, 0));

        dependencyGraph.values().stream()
                .flatMap(List::stream)
                .forEach(dependentTask -> inDegree.merge(dependentTask, 1, Integer::sum));

        return inDegree;
    }

    private List<Task> performTopologicalSort(Map<String, Task> taskMap,
                                              Map<String, List<String>> dependencyGraph,
                                              Map<String, Integer> inDegree) {
        log.debug("Performing topological sort.");

        Queue<String> queue = inDegree.entrySet().stream()
                .filter(entry -> entry.getValue() == 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedList::new));

        List<Task> sortedTasks = new ArrayList<>();

        while (!queue.isEmpty()) {
            String taskName = queue.poll();
            Task task = taskMap.get(taskName);
            sortedTasks.add(task);
            log.debug("Task {} added to sorted list.", taskName);

            dependencyGraph.getOrDefault(taskName, Collections.emptyList())
                    .forEach(dependentTask -> {
                        inDegree.put(dependentTask, inDegree.get(dependentTask) - 1);
                        if (inDegree.get(dependentTask) == 0) {
                            queue.add(dependentTask);
                        }
                    });
        }

        if (sortedTasks.size() != taskMap.size()) {
            log.error("Circular dependency detected.");
            throw new JobProcessingException("Circular dependency detected");
        }

        log.debug("Topological sort completed successfully.");
        return sortedTasks;
    }
}
