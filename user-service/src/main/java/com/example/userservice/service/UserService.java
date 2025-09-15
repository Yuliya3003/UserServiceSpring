package com.example.userservice.service;

import com.example.userservice.dto.UserCreateDto;
import com.example.userservice.dto.UserDto;
import com.example.userservice.dto.UserEventDto;
import com.example.userservice.dto.UserUpdateDto;
import com.example.userservice.model.User;
import com.example.userservice.exception.ResourceNotFoundException;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final KafkaTemplate<String, UserEventDto> kafkaTemplate;

    private static final String USER_TOPIC = "user-events";

    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public UserDto findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToDto(user);
    }

    public UserDto create(UserCreateDto createDto) {
        User user = modelMapper.map(createDto, User.class);
        User saved = userRepository.save(user);
        sendKafkaMessage("CREATE", saved.getEmail());
        return mapToDto(saved);
    }

    public UserDto update(Long id, UserUpdateDto updateDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        if (updateDto.getName() != null) user.setName(updateDto.getName());
        if (updateDto.getEmail() != null) user.setEmail(updateDto.getEmail());
        if (updateDto.getAge() != null) user.setAge(updateDto.getAge());
        User updated = userRepository.save(user);
        return mapToDto(updated);
    }

    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        sendKafkaMessage("DELETE", user.getEmail());
        userRepository.deleteById(id);
    }

    private void sendKafkaMessage(String operation, String email) {
        UserEventDto event = new UserEventDto();
        event.setOperation(operation);
        event.setEmail(email);
        kafkaTemplate.send(USER_TOPIC, event);
    }
    private UserDto mapToDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }

}
