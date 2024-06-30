package com.example.learning_api.dto.request.test;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class UpdateTestRequest {
    @NotBlank
    private String id;
    private String name;
    private String description;
    private MultipartFile source;
    private int duration;
    private String startTime;
    private String endTime;
    private String showResultType;
}
