package com.example.userservice.service;

import com.example.userservice.dto.UserCreateDto;
import com.example.userservice.dto.UserDto;
import com.example.userservice.dto.UserUpdateDto;
import com.example.userservice.model.User;
import com.example.userservice.exception.ResourceNotFoundException;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setAge(30);
        user.setCreatedAt(LocalDateTime.now());

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("John Doe");
        userDto.setEmail("john@example.com");
        userDto.setAge(30);
        userDto.setCreatedAt(user.getCreatedAt());
    }

    @Test
    void findAll_returnsUserList() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(modelMapper.map(user, UserDto.class)).thenReturn(userDto);

        List<UserDto> result = userService.findAll();

        assertEquals(1, result.size());
        assertEquals(userDto, result.get(0));
    }

    @Test
    void findAll_emptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserDto> result = userService.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void findById_existingId_returnsUserDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserDto.class)).thenReturn(userDto);

        UserDto result = userService.findById(1L);

        assertEquals(userDto, result);
    }

    @Test
    void findById_nonExistingId_throwsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.findById(999L));
    }

    @Test
    void create_validDto_returnsUserDto() {
        UserCreateDto createDto = new UserCreateDto();
        createDto.setName("John Doe");
        createDto.setEmail("john@example.com");
        createDto.setAge(30);

        when(modelMapper.map(createDto, User.class)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, UserDto.class)).thenReturn(userDto);

        UserDto result = userService.create(createDto);

        assertEquals(userDto, result);
    }

    @Test
    void update_existingId_returnsUpdatedUserDto() {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("Jane Doe");
        updateDto.setEmail("jane@example.com");
        updateDto.setAge(31);

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("Jane Doe");
        updatedUser.setEmail("jane@example.com");
        updatedUser.setAge(31);
        updatedUser.setCreatedAt(user.getCreatedAt());

        UserDto updatedDto = new UserDto();
        updatedDto.setId(1L);
        updatedDto.setName("Jane Doe");
        updatedDto.setEmail("jane@example.com");
        updatedDto.setAge(31);
        updatedDto.setCreatedAt(user.getCreatedAt());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(modelMapper.map(updatedUser, UserDto.class)).thenReturn(updatedDto);

        UserDto result = userService.update(1L, updateDto);

        assertEquals(updatedDto, result);
    }

    @Test
    void update_nonExistingId_throwsException() {
        UserUpdateDto updateDto = new UserUpdateDto();
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.update(999L, updateDto));
    }

    @Test
    void delete_existingId_deletesUser() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void delete_nonExistingId_throwsException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userService.delete(999L));
    }
}
