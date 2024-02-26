package com.mb.brokerageprovider.integration_tests.api.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.brokerageprovider.api.request.ApiStockRequest;
import com.mb.brokerageprovider.api.response.ApiStockResponse;
import com.mb.brokerageprovider.base.BaseUnitTest;
import com.mb.brokerageprovider.config.RestResponsePage;
import com.mb.brokerageprovider.config.annotation.EnableTestcontainers;
import com.mb.brokerageprovider.exception.BaseException;
import com.mb.brokerageprovider.exception.BrokerageProviderErrorCode;
import com.mb.brokerageprovider.mapper.StockMapper;
import com.mb.brokerageprovider.service.StockService;
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
public class StockControllerIntegrationTests extends BaseUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StockService stockService;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(value = 1)
    void testConnectionToDatabase() {
        Assertions.assertNotNull(stockService);
        Assertions.assertNotNull(stockMapper);
    }

    @Test
    @Order(value = 2)
    void testGetAllStocks() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .get("/stocks/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status()
                        .isOk())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        Page<ApiStockResponse> apiUserResponses = objectMapper.readValue(content, new TypeReference<RestResponsePage<ApiStockResponse>>() {
        });

        then(apiUserResponses.getContent()).isNotEmpty();
        assertThat(apiUserResponses.getContent()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @Order(value = 3)
    void testGetStockById() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .get("/stocks/5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status()
                        .isOk())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        ApiStockResponse apiStockResponse = objectMapper.readValue(content, ApiStockResponse.class);

        then(apiStockResponse).isNotNull();
        Assertions.assertEquals("APPLE", apiStockResponse.getProductCode());
    }

    @Test
    @Order(value = 4)
    void testGetStockById_ShouldFail_WhenStockIsNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .get("/stocks/0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status()
                        .isNotFound())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        BaseException baseException = objectMapper.readValue(content, BaseException.class);

        then(baseException).isNotNull();
        Assertions.assertEquals(BrokerageProviderErrorCode.STOCK_NOT_FOUND, baseException.getErrorCode());
    }

    @Test
    @Order(value = 5)
    void testCreateStock() throws Exception {
        ApiStockRequest apiStockRequest = getApiStockRequest();
        String stock = objectMapper.writeValueAsString(apiStockRequest);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .post("/stocks/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stock))
                .andExpect(status()
                        .isCreated())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        ApiStockResponse apiStockResponse = objectMapper.readValue(content, ApiStockResponse.class);

        then(apiStockResponse).isNotNull();
        Assertions.assertEquals(apiStockResponse.getQuantity(), apiStockRequest.getQuantity());
        Assertions.assertEquals(apiStockResponse.getProductCode(), apiStockRequest.getProductCode());
    }

    @Test
    @Order(value = 6)
    void testCreateStock_ShouldFail_WhenProductCodeIsAlreadyExists() throws Exception {
        ApiStockRequest apiStockRequest = getApiStockRequest2();
        String stock = objectMapper.writeValueAsString(apiStockRequest);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .post("/stocks/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stock))
                .andExpect(status()
                        .isBadRequest())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        BaseException baseException = objectMapper.readValue(content, BaseException.class);

        then(baseException).isNotNull();
        Assertions.assertEquals(BrokerageProviderErrorCode.UNEXPECTED_ERROR, baseException.getErrorCode());
    }

    @Test
    @Order(value = 7)
    void testDeleteStock() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .delete("/stocks/5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status()
                        .isOk())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        then(content).isNotNull();
        Assertions.assertEquals("Stock deleted successfully.", content);
    }

    @Test
    @Order(value = 8)
    void testDeleteStock_ShouldFail_WhenStockIsNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .delete("/stocks/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status()
                        .isNotFound())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        BaseException baseException = objectMapper.readValue(content, BaseException.class);

        then(baseException).isNotNull();
        Assertions.assertEquals(BrokerageProviderErrorCode.STOCK_NOT_FOUND, baseException.getErrorCode());
    }
}
