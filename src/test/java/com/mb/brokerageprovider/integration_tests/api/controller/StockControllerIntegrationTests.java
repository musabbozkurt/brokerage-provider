package com.mb.brokerageprovider.integration_tests.api.controller;

import com.mb.brokerageprovider.api.request.ApiStockRequest;
import com.mb.brokerageprovider.api.response.ApiStockResponse;
import com.mb.brokerageprovider.base.BaseUnitTest;
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
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@EnableTestcontainers
@AutoConfigureWebTestClient
@ActiveProfiles("test-containers")
@TestMethodOrder(OrderAnnotation.class)
public class StockControllerIntegrationTests extends BaseUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private StockService stockService;

    @Autowired
    private StockMapper stockMapper;

    @Test
    @Order(value = 1)
    void testConnectionToDatabase() {
        Assertions.assertNotNull(stockService);
        Assertions.assertNotNull(stockMapper);
    }

    @Test
    @Order(value = 2)
    void testGetAllStocks() {
        webTestClient.get().uri("/stocks/")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.content.length()").isNumber()
                .jsonPath("$.content").value(hasSize(greaterThanOrEqualTo(2)));
    }

    @Test
    @Order(value = 3)
    void testGetStockById() {
        webTestClient.get().uri("/stocks/5")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiStockResponse.class)
                .value(apiStockResponse -> Assertions.assertEquals("APPLE", apiStockResponse.getProductCode()));
    }

    @Test
    @Order(value = 4)
    void testGetStockById_ShouldFail_WhenStockIsNotFound() {
        webTestClient.get().uri("/stocks/0")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(BaseException.class)
                .value(baseException -> Assertions.assertEquals(BrokerageProviderErrorCode.STOCK_NOT_FOUND, baseException.getErrorCode()));
    }

    @Test
    @Order(value = 5)
    void testCreateStock() {
        ApiStockRequest apiStockRequest = getApiStockRequest();

        webTestClient.post().uri("/stocks/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(apiStockRequest), ApiStockRequest.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ApiStockResponse.class)
                .value(apiStockResponse -> {
                    Assertions.assertEquals(apiStockResponse.getQuantity(), apiStockRequest.getQuantity());
                    Assertions.assertEquals(apiStockResponse.getProductCode(), apiStockRequest.getProductCode());
                });
    }

    @Test
    @Order(value = 6)
    void testCreateStock_ShouldFail_WhenProductCodeIsAlreadyExists() {
        ApiStockRequest apiStockRequest = getApiStockRequest2();

        webTestClient.post().uri("/stocks/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(apiStockRequest), ApiStockRequest.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(BaseException.class)
                .value(baseException -> Assertions.assertEquals(BrokerageProviderErrorCode.UNEXPECTED_ERROR, baseException.getErrorCode()));
    }

    @Test
    @Order(value = 7)
    void testDeleteStock() {
        webTestClient.delete().uri("/stocks/5")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(content -> Assertions.assertEquals("Stock deleted successfully.", content));
    }

    @Test
    @Order(value = 8)
    void testDeleteStock_ShouldFail_WhenStockIsNotFound() {
        webTestClient.delete().uri("/stocks/1")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(BaseException.class)
                .value(baseException -> Assertions.assertEquals(BrokerageProviderErrorCode.STOCK_NOT_FOUND, baseException.getErrorCode()));
    }
}
