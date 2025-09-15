package com.example.userservice.controller;

import com.example.userservice.dto.UserCreateDto;
import com.example.userservice.dto.UserDto;
import com.example.userservice.dto.UserUpdateDto;
import com.example.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllUsers_returnsListOfUsers() throws Exception {
        UserDto userDto = createUserDto();
        List<UserDto> users = List.of(userDto);
        when(userService.findAll()).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("John Doe"));

        verify(userService).findAll();
    }

    @Test
    void getAllUsers_emptyList() throws Exception {
        when(userService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(userService).findAll();
    }

    @Test
    void getUserById_returnsUser() throws Exception {
        UserDto userDto = createUserDto();
        when(userService.findById(1L)).thenReturn(userDto);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(userService).findById(1L);
    }

    @Test
    void createUser_validDto_returnsCreatedUser() throws Exception {
        UserCreateDto createDto = new UserCreateDto();
        createDto.setName("John Doe");
        createDto.setEmail("john@example.com");
        createDto.setAge(30);

        UserDto created = createUserDto();
        when(userService.create(any(UserCreateDto.class))).thenReturn(created);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(userService).create(any(UserCreateDto.class));
    }

    @Test
    void createUser_invalidDto_returnsBadRequest() throws Exception {
        UserCreateDto invalidDto = new UserCreateDto();
        invalidDto.setName("");
        invalidDto.setEmail("invalid");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any(UserCreateDto.class));
    }

    @Test
    void updateUser_validDto_returnsUpdatedUser() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("Jane Doe");
        updateDto.setEmail("jane@example.com");
        updateDto.setAge(31);

        UserDto updated = createUserDto();
        updated.setName("Jane Doe");
        updated.setEmail("jane@example.com");
        updated.setAge(31);

        when(userService.update(eq(1L), any(UserUpdateDto.class))).thenReturn(updated);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Doe"));

        verify(userService).update(eq(1L), any(UserUpdateDto.class));
    }

    @Test
    void deleteUser_existingId_returnsNoContent() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).delete(1L);
    }

    private UserDto createUserDto() {
        UserDto dto = new UserDto();
        dto.setId(1L);
        dto.setName("John Doe");
        dto.setEmail("john@example.com");
        dto.setAge(30);
        dto.setCreatedAt(LocalDateTime.now());
        return dto;
    }
}