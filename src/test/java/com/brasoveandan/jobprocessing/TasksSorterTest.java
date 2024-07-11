package com.brasoveandan.jobprocessing;

import com.brasoveandan.jobprocessing.core.Task;
import com.brasoveandan.jobprocessing.core.TasksSorter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.brasoveandan.jobprocessing.UnitTestsUtils.*;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TasksSorterTest {
    private final TasksSorter subject = new TasksSorter();

    @Test
    public void testSortTasks_case1() {
        final var request = List.of(
                new Task(TASK_1_NAME, TASK_1_COMMAND, null),
                new Task(TASK_2_NAME, TASK_2_COMMAND, List.of(TASK_1_NAME, TASK_3_NAME)),
                new Task(TASK_3_NAME, TASK_3_COMMAND, List.of(TASK_1_NAME))
        );

        final var response = subject.sortTasks(request);
        assertEquals(3, response.size());
        assertEquals(TASK_1_NAME, response.get(0).name());
        assertEquals(TASK_3_NAME, response.get(1).name());
        assertEquals(TASK_2_NAME, response.get(2).name());
    }

    @Test
    public void testSortTasks_case2() {
        final var request = List.of(
                new Task(TASK_1_NAME, TASK_1_COMMAND, null),
                new Task(TASK_2_NAME, TASK_2_COMMAND, List.of(TASK_1_NAME, TASK_3_NAME)),
                new Task(TASK_3_NAME, TASK_3_COMMAND, List.of(TASK_1_NAME)),
                new Task(TASK_4_NAME, TASK_4_COMMAND, null)
        );

        final var response = subject.sortTasks(request);
        assertEquals(4, response.size());
        assertEquals(TASK_4_NAME, response.get(0).name());
        assertEquals(TASK_1_NAME, response.get(1).name());
        assertEquals(TASK_3_NAME, response.get(2).name());
        assertEquals(TASK_2_NAME, response.get(3).name());
    }

    @Test
    public void testSortTasks_circularDependency() {
        final var request = List.of(
                new Task(TASK_1_NAME, TASK_1_COMMAND, List.of(TASK_3_NAME)),
                new Task(TASK_2_NAME, TASK_2_COMMAND, List.of(TASK_1_NAME, TASK_3_NAME)),
                new Task(TASK_3_NAME, TASK_3_COMMAND, List.of(TASK_1_NAME))
        );

        assertThatCode(() -> subject.sortTasks(request)).hasMessage("Circular dependency detected");
    }
}
