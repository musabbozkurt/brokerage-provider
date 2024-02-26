package com.mb.brokerageprovider.integration_tests.api.controller;

import com.mb.brokerageprovider.api.request.ApiOrderRequest;
import com.mb.brokerageprovider.api.response.ApiOrderResponse;
import com.mb.brokerageprovider.base.BaseUnitTest;
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
public class OrderControllerIntegrationTests extends BaseUnitTest {

    @Autowired
    protected WebTestClient webTestClient;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMapper orderMapper;

    @Test
    @Order(value = 1)
    void testConnectionToDatabase() {
        Assertions.assertNotNull(orderService);
        Assertions.assertNotNull(orderMapper);
    }

    @Test
    @Order(value = 2)
    void testGetAllOrders() {
        webTestClient.get().uri("/orders/")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.content.length()").isEqualTo(0)
                .jsonPath("$.content").value(hasSize(greaterThanOrEqualTo(0)))
                .jsonPath("$.sort.empty").isEqualTo(true);
    }

    @Test
    @Order(value = 3)
    void testGetOrderById_ShouldFail_WhenOrderIsNotFound() {
        webTestClient.get().uri("/orders/0")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(BaseException.class)
                .value(baseException -> Assertions.assertEquals(BrokerageProviderErrorCode.ORDER_NOT_FOUND, baseException.getErrorCode()));
    }

    @Test
    @Order(value = 4)
    void testBuyStockOrder() {
        ApiOrderRequest apiOrderRequest = orderRequests.get(2);

        webTestClient.post().uri("/orders/buy")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(apiOrderRequest), ApiOrderRequest.class)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody(ApiOrderResponse.class)
                .value(apiOrderResponse -> Assertions.assertEquals(apiOrderResponse.getUser().getId(), apiOrderRequest.getUserId()));
    }

    @Test
    @Order(value = 5)
    void testBuyStockOrder_ShouldFail_WhenUserIsNotFound() {
        ApiOrderRequest apiOrderRequest = orderRequests.getFirst();

        webTestClient.post().uri("/orders/buy")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(apiOrderRequest), ApiOrderRequest.class)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(BaseException.class)
                .value(baseException -> Assertions.assertEquals(BrokerageProviderErrorCode.USER_NOT_FOUND, baseException.getErrorCode()));
    }

    @Test
    @Order(value = 6)
    void testSellStockOrder() {
        ApiOrderRequest apiOrderRequest = orderRequests.get(1);

        webTestClient.post().uri("/orders/sell")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(apiOrderRequest), ApiOrderRequest.class)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody(ApiOrderResponse.class)
                .value(apiOrderResponse -> Assertions.assertEquals(apiOrderResponse.getUser().getId(), apiOrderRequest.getUserId()));
    }

    @Test
    @Order(value = 7)
    void testSellStockOrder_ShouldFail_WhenUserIsNotFound() {
        ApiOrderRequest apiOrderRequest = orderRequests.getFirst();

        webTestClient.post().uri("/orders/sell")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(apiOrderRequest), ApiOrderRequest.class)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(BaseException.class)
                .value(baseException -> Assertions.assertEquals(BrokerageProviderErrorCode.USER_NOT_FOUND, baseException.getErrorCode()));
    }

    @Test
    @Order(value = 8)
    void testCancelOrderById_ShouldFail_WhenOrderIsNotFound() {
        webTestClient.put().uri("/orders/cancel/2")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(BaseException.class)
                .value(baseException -> Assertions.assertEquals(BrokerageProviderErrorCode.ORDER_NOT_FOUND, baseException.getErrorCode()));
    }
}
