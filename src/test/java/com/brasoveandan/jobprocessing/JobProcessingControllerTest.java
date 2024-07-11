package com.brasoveandan.jobprocessing;

import com.brasoveandan.jobprocessing.controller.JobProcessingController;
import com.brasoveandan.jobprocessing.core.Job;
import com.brasoveandan.jobprocessing.core.JobProcessingException;
import com.brasoveandan.jobprocessing.core.JobProcessorService;
import com.brasoveandan.jobprocessing.core.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.brasoveandan.jobprocessing.UnitTestsUtils.*;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobProcessingController.class)
class JobProcessingControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private JobProcessorService jobProcessorService;

    private final String json = """
            {
              "tasks": [
                {"name": "task-1", "command": "touch /tmp/file1"},
                {"name": "task-2", "command": "cat /tmp/file1", "requires": ["task-3"]},
                {"name": "task-3", "command": "echo 'Hello World!' > /tmp/file1", "requires": ["task-1"]},
                {"name": "task-4", "command": "rm /tmp/file1", "requires": ["task-2", "task-3"]}
              ]
            }""";
    private final Job job = new Job(List.of(
            new Task(TASK_1_NAME, TASK_1_COMMAND, null),
            new Task(TASK_2_NAME, TASK_2_COMMAND, List.of(TASK_3_NAME)),
            new Task(TASK_3_NAME, TASK_3_COMMAND, List.of(TASK_1_NAME)),
            new Task(TASK_4_NAME, TASK_4_COMMAND, List.of(TASK_2_NAME, TASK_3_NAME))
    ));
    private final List<Task> orderedTasks = List.of(
            new Task(TASK_1_NAME, TASK_1_COMMAND, null),
            new Task(TASK_3_NAME, TASK_3_COMMAND, List.of(TASK_1_NAME)),
            new Task(TASK_2_NAME, TASK_2_COMMAND, List.of(TASK_3_NAME)),
            new Task(TASK_4_NAME, TASK_4_COMMAND, List.of(TASK_2_NAME, TASK_3_NAME))
    );

    @Test
    public void testProcessJob_Success() throws Exception {
        when(jobProcessorService.orderTasks(job)).thenReturn(orderedTasks);

        mockMvc.perform(post("/api/orderedTasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is(TASK_1_NAME)))
                .andExpect(jsonPath("$[1].name", is(TASK_3_NAME)))
                .andExpect(jsonPath("$[2].name", is(TASK_2_NAME)))
                .andExpect(jsonPath("$[3].name", is(TASK_4_NAME)));
    }

    @Test
    public void testProcessJob_BashScript() throws Exception {
        final var expectedScript = """
                touch /tmp/file1
                echo 'Hello World!' > /tmp/file1
                cat /tmp/file1
                rm /tmp/file1
                """;
        when(jobProcessorService.orderTasks(job)).thenReturn(orderedTasks);
        when(jobProcessorService.generateBashScript(orderedTasks)).thenReturn(expectedScript);

        mockMvc.perform(post("/api/orderedTasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_PLAIN)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string(is(expectedScript)));
    }

    @Test
    void testProcessJob_Exception() throws Exception {
        final var json = """
                {
                  "tasks": [
                    {"name": "task-1", "command": "touch /tmp/file1", "requires": ["task-3"]},
                    {"name": "task-2", "command": "cat /tmp/file1", "requires": ["task-3"]},
                    {"name": "task-3", "command": "echo 'Hello World!' > /tmp/file1", "requires": ["task-1"]},
                    {"name": "task-4", "command": "rm /tmp/file1", "requires": ["task-2", "task-3"]}
                  ]
                }""";

        when(jobProcessorService.orderTasks(any())).thenThrow(new JobProcessingException("Circular dependency detected"));

        mockMvc.perform(post("/api/orderedTasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_PLAIN)
                        .content(json))
                .andExpect(status().isInternalServerError());
    }
}
