package com.mb.brokerageprovider.integration_tests.api.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.brokerageprovider.api.request.ApiOrderRequest;
import com.mb.brokerageprovider.api.response.ApiOrderResponse;
import com.mb.brokerageprovider.base.BaseUnitTest;
import com.mb.brokerageprovider.config.RestResponsePage;
import com.mb.brokerageprovider.config.annotation.EnableTestcontainers;
import com.mb.brokerageprovider.exception.BaseException;
import com.mb.brokerageprovider.exception.BrokerageProviderErrorCode;
import com.mb.brokerageprovider.mapper.OrderMapper;
import com.mb.brokerageprovider.service.OrderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EnableTestcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test-containers")
@TestMethodOrder(OrderAnnotation.class)
public class OrderControllerIntegrationTests extends BaseUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(value = 1)
    void testConnectionToDatabase() {
        Assertions.assertNotNull(orderService);
        Assertions.assertNotNull(orderMapper);
    }

    @Test
    @Order(value = 2)
    void testGetAllOrders() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .get("/orders/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status()
                        .isAccepted())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        Page<ApiOrderResponse> apiUserResponses = objectMapper.readValue(content, new TypeReference<RestResponsePage<ApiOrderResponse>>() {
        });

        then(apiUserResponses.getContent()).isEmpty();
        assertThat(apiUserResponses.getContent()).hasSizeGreaterThanOrEqualTo(0);
    }

    @Test
    @Order(value = 3)
    void testGetOrderById_ShouldFail_WhenOrderIsNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .get("/orders/0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status()
                        .isNotFound())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        BaseException baseException = objectMapper.readValue(content, BaseException.class);

        then(baseException).isNotNull();
        Assertions.assertEquals(BrokerageProviderErrorCode.ORDER_NOT_FOUND, baseException.getErrorCode());
    }

    @Test
    @Order(value = 4)
    void testBuyStockOrder() throws Exception {
        ApiOrderRequest apiOrderRequest = orderRequests.get(2);
        String order = objectMapper.writeValueAsString(apiOrderRequest);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .post("/orders/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(order))
                .andExpect(status()
                        .isAccepted())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        ApiOrderResponse apiOrderResponse = objectMapper.readValue(content, ApiOrderResponse.class);

        then(apiOrderResponse).isNotNull();
        Assertions.assertEquals(apiOrderResponse.getUser().getId(), apiOrderRequest.getUserId());
    }

    @Test
    @Order(value = 5)
    void testBuyStockOrder_ShouldFail_WhenUserIsNotFound() throws Exception {
        ApiOrderRequest apiOrderRequest = orderRequests.getFirst();
        String order = objectMapper.writeValueAsString(apiOrderRequest);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .post("/orders/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(order))
                .andExpect(status()
                        .isNotFound())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();


        BaseException baseException = objectMapper.readValue(content, BaseException.class);

        then(baseException).isNotNull();
        Assertions.assertEquals(BrokerageProviderErrorCode.USER_NOT_FOUND, baseException.getErrorCode());
    }

    @Test
    @Order(value = 6)
    void testSellStockOrder() throws Exception {
        ApiOrderRequest apiOrderRequest = orderRequests.get(1);
        String order = objectMapper.writeValueAsString(apiOrderRequest);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .post("/orders/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(order))
                .andExpect(status()
                        .isAccepted())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        ApiOrderResponse apiOrderResponse = objectMapper.readValue(content, ApiOrderResponse.class);

        then(apiOrderResponse).isNotNull();
        Assertions.assertEquals(apiOrderResponse.getUser().getId(), apiOrderRequest.getUserId());
    }

    @Test
    @Order(value = 7)
    void testSellStockOrder_ShouldFail_WhenUserIsNotFound() throws Exception {
        ApiOrderRequest apiOrderRequest = orderRequests.getFirst();
        String order = objectMapper.writeValueAsString(apiOrderRequest);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .post("/orders/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(order))
                .andExpect(status()
                        .isNotFound())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        BaseException baseException = objectMapper.readValue(content, BaseException.class);

        then(baseException).isNotNull();
        Assertions.assertEquals(BrokerageProviderErrorCode.USER_NOT_FOUND, baseException.getErrorCode());
    }

    @Test
    @Order(value = 8)
    void testCancelOrderById_ShouldFail_WhenOrderIsNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .put("/orders/cancel/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status()
                        .isNotFound())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        BaseException baseException = objectMapper.readValue(content, BaseException.class);

        then(baseException).isNotNull();
        Assertions.assertEquals(BrokerageProviderErrorCode.ORDER_NOT_FOUND, baseException.getErrorCode());
    }
}
