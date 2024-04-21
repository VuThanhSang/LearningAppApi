package com.example.learning_api.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private final String userId;
    private final String accessToken;
    @JsonIgnore
    private final String refreshToken;
}
