package com.mb.brokerageprovider.integration_tests.api.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.brokerageprovider.api.request.ApiUserRequest;
import com.mb.brokerageprovider.api.response.ApiUserResponse;
import com.mb.brokerageprovider.base.BaseUnitTest;
import com.mb.brokerageprovider.config.RestResponsePage;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EnableTestcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test-containers")
@TestMethodOrder(OrderAnnotation.class)
public class UserControllerIntegrationTests extends BaseUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(value = 1)
    void testConnectionToDatabase() {
        Assertions.assertNotNull(userService);
        Assertions.assertNotNull(userMapper);
    }

    @Test
    @Order(value = 2)
    void testGetUsers() throws Exception {
        MvcResult mvcResult = mockMvc
                .perform(MockMvcRequestBuilders.get("/users/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        Page<ApiUserResponse> apiUserResponses = objectMapper.readValue(content, new TypeReference<RestResponsePage<ApiUserResponse>>() {
        });

        then(apiUserResponses.getContent()).isNotEmpty();
        assertThat(apiUserResponses.getContent()).hasSizeGreaterThanOrEqualTo(4);
    }

    @Test
    @Order(value = 3)
    void testCreateUser() throws Exception {
        ApiUserRequest apiUserRequest = getApiUserRequest();
        String userRequest = objectMapper.writeValueAsString(apiUserRequest);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status()
                        .isCreated())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        ApiUserResponse apiUserResponse = objectMapper.readValue(content, ApiUserResponse.class);

        then(apiUserResponse).isNotNull();
        Assertions.assertEquals(apiUserResponse.getName(), apiUserRequest.getName());
        Assertions.assertEquals(apiUserResponse.getSurname(), apiUserRequest.getSurname());
        Assertions.assertEquals(apiUserResponse.getEmail(), apiUserRequest.getEmail());
    }

    @Test
    @Order(value = 4)
    void testCreateUser_ShouldFail_WhenPhoneNumberOrEmailIsAlreadyExists() throws Exception {
        ApiUserRequest apiUserRequest = getApiUserRequest2();
        String userRequest = objectMapper.writeValueAsString(apiUserRequest);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status()
                        .isBadRequest())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        BaseException baseException = objectMapper.readValue(content, BaseException.class);

        then(baseException).isNotNull();
        Assertions.assertEquals(BrokerageProviderErrorCode.UNEXPECTED_ERROR, baseException.getErrorCode());
    }

    @Test
    @Order(value = 5)
    void testGetUserById() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status()
                        .isOk())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        ApiUserResponse apiUserResponse = objectMapper.readValue(content, ApiUserResponse.class);

        then(apiUserResponse).isNotNull();
        then(apiUserResponse.getId()).isNotNull();
        then(apiUserResponse.getUsername()).isNotNull();
        then(apiUserResponse.getEmail()).isNotNull();
    }

    @Test
    @Order(value = 6)
    void testGetUserById_ShouldFail_WhenUserIsNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status()
                        .isNotFound())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        BaseException baseException = objectMapper.readValue(content, BaseException.class);

        then(baseException).isNotNull();
        Assertions.assertEquals(BrokerageProviderErrorCode.USER_NOT_FOUND, baseException.getErrorCode());
    }

    @Test
    @Order(value = 7)
    void testUpdateUserById() throws Exception {
        ApiUserRequest apiUserRequest = getApiUserRequest3();
        String userRequest = objectMapper.writeValueAsString(apiUserRequest);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status()
                        .isOk())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        ApiUserResponse apiUserResponse = objectMapper.readValue(content, ApiUserResponse.class);

        then(apiUserResponse).isNotNull();
        Assertions.assertEquals(apiUserResponse.getEmail(), apiUserRequest.getEmail());
    }

    @Test
    @Order(value = 8)
    void testUpdateUserById_ShouldFail_WhenUserIsNotFound() throws Exception {
        ApiUserRequest apiUserRequest = getApiUserRequest();
        String userRequest = objectMapper.writeValueAsString(apiUserRequest);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status()
                        .isNotFound())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        BaseException baseException = objectMapper.readValue(content, BaseException.class);

        then(baseException).isNotNull();
        Assertions.assertEquals(BrokerageProviderErrorCode.USER_NOT_FOUND, baseException.getErrorCode());
    }

    @Test
    @Order(value = 9)
    void testDeleteUserById() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .delete("/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status()
                        .isOk())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        then(content).isNotNull();
        Assertions.assertEquals("User deleted successfully.", content);
    }

    @Test
    @Order(value = 10)
    void testDeleteUserById_ShouldFail_WhenUserIsNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .delete("/users/0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status()
                        .isNotFound())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        BaseException baseException = objectMapper.readValue(content, BaseException.class);

        then(baseException).isNotNull();
        Assertions.assertEquals(BrokerageProviderErrorCode.USER_NOT_FOUND, baseException.getErrorCode());
    }

    @Test
    @Order(value = 11)
    void testGetAllOrdersByUserId_ShouldFail_WhenOrderIsNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/1/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status()
                        .isNotFound())
                .andReturn();

        then(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @Order(value = 12)
    void testGetAllOrdersByUserId_ShouldFail_WhenUserIsNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/0/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status()
                        .isNotFound())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        BaseException baseException = objectMapper.readValue(content, BaseException.class);

        then(baseException).isNotNull();
        Assertions.assertEquals(BrokerageProviderErrorCode.USER_NOT_FOUND, baseException.getErrorCode());
    }
}
