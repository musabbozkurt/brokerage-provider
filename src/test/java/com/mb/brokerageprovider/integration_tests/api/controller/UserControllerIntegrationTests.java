package com.mb.brokerageprovider.integration_tests.api.controller;

import com.mb.brokerageprovider.api.request.ApiUserRequest;
import com.mb.brokerageprovider.api.response.ApiUserResponse;
import com.mb.brokerageprovider.base.BaseUnitTest;
import com.mb.brokerageprovider.config.annotation.EnableTestcontainers;
import com.mb.brokerageprovider.exception.BaseException;
import com.mb.brokerageprovider.exception.BrokerageProviderErrorCode;
import com.mb.brokerageprovider.mapper.UserMapper;
import com.mb.brokerageprovider.service.UserService;
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
public class UserControllerIntegrationTests extends BaseUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Test
    @Order(value = 1)
    void testConnectionToDatabase() {
        Assertions.assertNotNull(userService);
        Assertions.assertNotNull(userMapper);
    }

    @Test
    @Order(value = 2)
    void testGetUsers() {
        webTestClient.get().uri("/users/")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.content.length()").isNumber()
                .jsonPath("$.content").value(hasSize(greaterThanOrEqualTo(4)));
    }

    @Test
    @Order(value = 3)
    void testCreateUser() {
        ApiUserRequest apiUserRequest = getApiUserRequest();

        webTestClient.post().uri("/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(apiUserRequest), ApiUserRequest.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ApiUserResponse.class)
                .value(apiUserResponse -> {
                    Assertions.assertEquals(apiUserResponse.getName(), apiUserRequest.getName());
                    Assertions.assertEquals(apiUserResponse.getSurname(), apiUserRequest.getSurname());
                    Assertions.assertEquals(apiUserResponse.getEmail(), apiUserRequest.getEmail());
                });
    }

    @Test
    @Order(value = 4)
    void testCreateUser_ShouldFail_WhenPhoneNumberOrEmailIsAlreadyExists() {
        ApiUserRequest apiUserRequest = getApiUserRequest2();

        webTestClient.post().uri("/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(apiUserRequest), ApiUserRequest.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(BaseException.class)
                .value(baseException -> Assertions.assertEquals(BrokerageProviderErrorCode.UNEXPECTED_ERROR, baseException.getErrorCode()));
    }

    @Test
    @Order(value = 5)
    void testGetUserById() {
        webTestClient.get().uri("/users/2")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiUserResponse.class)
                .value(apiUserResponse -> {
                    Assertions.assertNotNull(apiUserResponse.getId());
                    Assertions.assertNotNull(apiUserResponse.getUsername());
                    Assertions.assertNotNull(apiUserResponse.getEmail());
                });
    }

    @Test
    @Order(value = 6)
    void testGetUserById_ShouldFail_WhenUserIsNotFound() {
        webTestClient.get().uri("/users/0")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(BaseException.class)
                .value(baseException -> Assertions.assertEquals(BrokerageProviderErrorCode.USER_NOT_FOUND, baseException.getErrorCode()));
    }

    @Test
    @Order(value = 7)
    void testUpdateUserById() {
        ApiUserRequest apiUserRequest = getApiUserRequest3();

        webTestClient.put().uri("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(apiUserRequest), ApiUserRequest.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiUserResponse.class)
                .value(apiUserResponse -> Assertions.assertEquals(apiUserResponse.getEmail(), apiUserRequest.getEmail()));
    }

    @Test
    @Order(value = 8)
    void testUpdateUserById_ShouldFail_WhenUserIsNotFound() {
        ApiUserRequest apiUserRequest = getApiUserRequest();

        webTestClient.put().uri("/users/0")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(apiUserRequest), ApiUserRequest.class)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(BaseException.class)
                .value(baseException -> Assertions.assertEquals(BrokerageProviderErrorCode.USER_NOT_FOUND, baseException.getErrorCode()));
    }

    @Test
    @Order(value = 9)
    void testDeleteUserById() {
        webTestClient.delete().uri("/users/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(content -> Assertions.assertEquals("User deleted successfully.", content));
    }

    @Test
    @Order(value = 10)
    void testDeleteUserById_ShouldFail_WhenUserIsNotFound() {
        webTestClient.delete().uri("/users/0")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(BaseException.class)
                .value(baseException -> Assertions.assertEquals(BrokerageProviderErrorCode.USER_NOT_FOUND, baseException.getErrorCode()));
    }

    @Test
    @Order(value = 11)
    void testGetAllOrdersByUserId_ShouldFail_WhenOrderIsNotFound() {
        webTestClient.get().uri("/users/1/orders")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(value = 12)
    void testGetAllOrdersByUserId_ShouldFail_WhenUserIsNotFound() {
        webTestClient.get().uri("/users/0/orders")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(BaseException.class)
                .value(baseException -> Assertions.assertEquals(BrokerageProviderErrorCode.USER_NOT_FOUND, baseException.getErrorCode()));
    }
}
