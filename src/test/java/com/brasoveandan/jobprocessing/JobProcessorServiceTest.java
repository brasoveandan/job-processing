package com.brasoveandan.jobprocessing;

import com.brasoveandan.jobprocessing.core.Job;
import com.brasoveandan.jobprocessing.core.JobProcessorService;
import com.brasoveandan.jobprocessing.core.Task;
import com.brasoveandan.jobprocessing.core.TasksSorter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static com.brasoveandan.jobprocessing.UnitTestsUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobProcessorServiceTest {
    private final TasksSorter tasksSorter = mock(TasksSorter.class);
    private final JobProcessorService subject = new JobProcessorService(tasksSorter);
    private Task task1;
    private Task task2;

    @BeforeEach
    void setUp() {
        task1 = new Task(TASK_1_NAME, TASK_1_COMMAND, null);
        task2 = new Task(TASK_2_NAME, TASK_2_COMMAND, List.of(TASK_1_NAME));
    }

    @Test
    void testOrderTasks() {
        final var unsortedTasks = Arrays.asList(task2, task1);
        final var job = new Job(unsortedTasks);

        final var sortedTasks = Arrays.asList(task1, task2);
        when(tasksSorter.sortTasks(unsortedTasks)).thenReturn(sortedTasks);

        final var result = subject.orderTasks(job);
        assertEquals(sortedTasks, result);
    }

    @Test
    void testGenerateBashScript() {
        final var sortedTasks = Arrays.asList(task1, task2);
        final var expectedScript = """
                touch /tmp/file1
                cat /tmp/file1
                """;
        final var result = subject.generateBashScript(sortedTasks);
        assertEquals(expectedScript, result);
    }
}
