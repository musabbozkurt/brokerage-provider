package com.mb.brokerageprovider.api.controller;

import com.mb.brokerageprovider.api.request.ApiUserRequest;
import com.mb.brokerageprovider.api.response.ApiOrderResponse;
import com.mb.brokerageprovider.api.response.ApiUserResponse;
import com.mb.brokerageprovider.mapper.OrderMapper;
import com.mb.brokerageprovider.mapper.UserMapper;
import com.mb.brokerageprovider.service.UserService;
import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;

    @GetMapping("/")
    @Observed(name = "getUsers")
    @Operation(description = "Get all users.")
    public ResponseEntity<Page<ApiUserResponse>> getUsers(Pageable pageable) {
        log.info("Received a request to get all users. getAllUsers.");
        return new ResponseEntity<>(userMapper.map(userService.getAllUsers(pageable)), HttpStatus.OK);
    }

    @PostMapping("/")
    @Observed(name = "createUser")
    @Operation(description = "Create user.")
    public ResponseEntity<ApiUserResponse> createUser(@RequestBody ApiUserRequest apiUserRequest) {
        log.info("Received a request to create user. createUser - apiUserRequest: {}", apiUserRequest);
        return new ResponseEntity<>(userMapper.map(userService.createUser(userMapper.map(apiUserRequest))), HttpStatus.CREATED);
    }

    @GetMapping("/{userId}")
    @Observed(name = "getUserById")
    @Operation(description = "Get user by id.")
    public ResponseEntity<ApiUserResponse> getUserById(@PathVariable Long userId) {
        log.info("Received a request to get user by id. getUserById - userId: {}", userId);
        return new ResponseEntity<>(userMapper.map(userService.getUserById(userId)), HttpStatus.OK);
    }

    @PutMapping("/{userId}")
    @Observed(name = "updateUserById")
    @Operation(description = "Update user.")
    public ResponseEntity<ApiUserResponse> updateUserById(@PathVariable Long userId, @RequestBody ApiUserRequest apiUserRequest) {
        log.info("Received a request to update user by id. updateUserById - userId: {}, apiUserRequest: {}", userId, apiUserRequest);
        return new ResponseEntity<>(userMapper.map(userService.updateUserById(userId, userMapper.map(apiUserRequest))), HttpStatus.OK);
    }

    @DeleteMapping("/{userId}")
    @Observed(name = "deleteUserById")
    @Operation(description = "Delete user by id.")
    public ResponseEntity<String> deleteUserById(@PathVariable Long userId) {
        log.info("Received a request to delete user by id. deleteUserById - userId: {}", userId);
        userService.deleteUserById(userId);
        return new ResponseEntity<>("User deleted successfully.", HttpStatus.OK);
    }

    @GetMapping("/{userId}/orders")
    @Observed(name = "getAllOrdersByUserId")
    @Operation(description = "Get user orders by userId.")
    public List<ApiOrderResponse> getAllOrdersByUserId(@PathVariable Long userId) {
        log.info("Received a request to get all orders by user id. getAllOrdersByUserId - userId: {}.", userId);
        return orderMapper.map(userService.getAllOrdersByUserId(userId));
    }
}
