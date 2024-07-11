package com.brasoveandan.jobprocessing.controller;

import com.brasoveandan.jobprocessing.core.Job;
import com.brasoveandan.jobprocessing.core.JobProcessingException;
import com.brasoveandan.jobprocessing.core.JobProcessorService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class JobProcessingController {
    private final JobProcessorService jobProcessorService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/orderedTasks", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
    @Operation(
            summary = "Process job and order tasks",
            description = "If the Accept header is text/plain, you will see the bash script; otherwise if the Accept header is application/json, the ordered tasks are returned. Select the 'Header' option from the 'Media type' dropdown in the responses tab.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = {
                                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MediaType.class)),
                                    @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, schema = @Schema(implementation = MediaType.class))
                            }),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<?> processJob(@RequestBody Job job, @RequestHeader(required = false, value = HttpHeaders.ACCEPT) String acceptHeader) {
        try {
            final var orderedTasks = jobProcessorService.orderTasks(job);
            return Optional.ofNullable(acceptHeader)
                    .filter(MediaType.TEXT_PLAIN_VALUE::equals)
                    .map(header -> ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN)
                            .body(jobProcessorService.generateBashScript(orderedTasks)))
                    .orElseGet(() -> {
                        try {
                            String json = objectMapper.writeValueAsString(orderedTasks);
                            return ResponseEntity.ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .body(json);
                        } catch (JsonProcessingException e) {
                            throw new JobProcessingException("Unparsable JSON response", e);
                        }
                    });
        } catch (JobProcessingException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
