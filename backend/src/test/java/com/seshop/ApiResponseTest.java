package com.seshop;

import static org.assertj.core.api.Assertions.assertThat;

import com.seshop.shared.api.ApiResponse;
import org.junit.jupiter.api.Test;

class ApiResponseTest {

    @Test
    void successWrapsDataAndTraceId() {
        ApiResponse<String> response = ApiResponse.success("ok", "trace-1");

        assertThat(response.data()).isEqualTo("ok");
        assertThat(response.meta().traceId()).isEqualTo("trace-1");
    }
}
