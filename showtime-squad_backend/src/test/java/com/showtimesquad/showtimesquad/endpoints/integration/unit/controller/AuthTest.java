package com.showtimesquad.showtimesquad.endpoints.integration.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.showtimesquad.showtimesquad.controller.AuthController;
import com.showtimesquad.showtimesquad.repository.UserRepository;
import com.showtimesquad.showtimesquad.model.User;
import com.showtimesquad.showtimesquad.model.request.LoginRequest;
import com.showtimesquad.showtimesquad.model.request.SignupRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class AuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    @WithMockUser
    @Transactional
    @Rollback
    void creatingUserReturnsOK() throws Exception {

        /*
            This function creates a user through the /register endpoint
            and checks if the value is 200, indicating that the endpoint is working.
        */

        // Test Data
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser1");
        signupRequest.setEmail("testuser1@testuser1.com");
        signupRequest.setPassword(" testuser1");
        signupRequest.setRole(new HashSet<>(Arrays.asList("mod", "user")));

        // Converting SignupRequest to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonRequestSignUp = objectMapper.writeValueAsString(signupRequest);

        // Performs The Registration
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("http://localhost/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequestSignUp));
        result.andExpect(status().isOk());

        TestTransaction.flagForCommit();
        TestTransaction.end();


        Optional<User> userOptional = userRepository.findByUsername("testuser1");
        assertTrue(userOptional.isPresent(), "User should be present in the database");

        // Deletes the user from the database
        userRepository.delete(userOptional.get());
    }

    @Test
    @WithMockUser
    void userShouldExist() throws Exception {

        /*
            This function makes sure that an account exists in a database.
            The database has a test user called, "testuser".
            The Unit Test performs a POST request to the /signin endpoint,
            and returns the value 200 OK if it passes.
        */

        // Test Data
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("testuser");

        // Converting LoginRequest to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonRequestLogin = objectMapper.writeValueAsString(loginRequest);
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("http://localhost/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequestLogin));
        result.andExpect(status().isOk());
    }
}
