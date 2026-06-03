package com.centralbank.eplatform.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import com.centralbank.eplatform.controller.admin.AdminOptionsController;
import com.centralbank.eplatform.dto.ApiResponse;
import com.centralbank.eplatform.dto.OptionsResponse;
import com.centralbank.eplatform.service.FixedOptionsService;

class AdminOptionsControllerTest
{
    @Test
    void returnsContractResponseWrapper()
    {
        AdminOptionsController controller = new AdminOptionsController(new FixedOptionsService());

        ApiResponse<OptionsResponse> response = controller.options();

        assertThat(response.code()).isEqualTo(200);
        assertThat(response.message()).isEqualTo("success");
        assertThat(response.data().offices()).hasSize(14);
        assertThat(response.data().banks()).hasSize(17);
    }
}
