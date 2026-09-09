package com.retailer.rewards.controller;

import com.retailer.rewards.dto.CustomerRewardsResponse;
import com.retailer.rewards.dto.MonthlyRewardDto;
import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.service.RewardsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RewardsController.class)
class RewardsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RewardsService rewardsService;

    @Test
    void returnsRewardsForKnownCustomer() throws Exception {
        CustomerRewardsResponse response = new CustomerRewardsResponse(
                "C001", "Alice Johnson",
                LocalDate.of(2026, 6, 9), LocalDate.of(2026, 9, 8),
                90, Collections.singletonList(new MonthlyRewardDto("2026-06", 90, Collections.emptyList())));

        when(rewardsService.getRewardsForCustomer(eq("C001"), anyInt(), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/rewards/customers/C001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("C001"))
                .andExpect(jsonPath("$.customerName").value("Alice Johnson"))
                .andExpect(jsonPath("$.totalPointsEarned").value(90))
                .andExpect(jsonPath("$.monthlyBreakdown[0].month").value("2026-06"));
    }

    @Test
    void returns404ForUnknownCustomer() throws Exception {
        when(rewardsService.getRewardsForCustomer(eq("UNKNOWN"), anyInt(), any()))
                .thenThrow(new CustomerNotFoundException("UNKNOWN"));

        mockMvc.perform(get("/api/v1/rewards/customers/UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void returns400ForInvalidMonthsParameter() throws Exception {
        mockMvc.perform(get("/api/v1/rewards/customers/C001").param("months", "0"))
                .andExpect(status().isBadRequest());
    }
}
