package com.cydeo.unit_test_review;

import com.cydeo.dto.RoleDTO;
import com.cydeo.dto.UserDTO;
import com.cydeo.entity.Role;
import com.cydeo.entity.User;
import com.cydeo.mapper.UserMapper;
import com.cydeo.repository.UserRepository;
import com.cydeo.service.KeycloakService;
import com.cydeo.service.ProjectService;
import com.cydeo.service.TaskService;
import com.cydeo.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ProjectService projectService;
    @Mock
    private TaskService taskService;
    @Mock
    private KeycloakService keycloakService;

    @InjectMocks
    private UserServiceImpl userService;

    @Spy
    private UserMapper userMapper = new UserMapper(new ModelMapper());

    User user;
    UserDTO userDTO;

    @BeforeEach
    void setUp(){
        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUserName("user");
        user.setPassWord("Abc1");
        user.setEnabled(true);
        user.setRole(new Role("Manager"));


        userDTO= new UserDTO();
        userDTO.setId(1L);
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        userDTO.setUserName("user");
        userDTO.setPassWord("Abc1");
        userDTO.setEnabled(true);

        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setDescription("Manager");

        userDTO.setRole(roleDTO);

    }

    // another way of creating data, we will call if we need them
    private List<User> getUsers(){
        User user2 = new User();
        user2.setId(2L);
        user2.setFirstName("Emily");
        return List.of(user, user2);
    }

    private List<UserDTO> getUserDTOs(){
        UserDTO userDTO2 = new UserDTO();
        userDTO2.setId(2L);
        userDTO2.setFirstName("Emily");
        return List.of(userDTO, userDTO2);
    }

    @Test
    void should_list_all_users(){
        //stub
        when(userRepository.findAllByIsDeletedOrderByFirstNameDesc(false)).thenReturn(getUsers());
        List<UserDTO> expectedList = getUserDTOs();

       // expectedList.sort(Comparator.comparing(UserDTO::getUserName).reversed());

        List<UserDTO> actualList = userService.listAllUsers();
       // assertEquals(expectedList, actualList);
            //AssertJ test dependency
        assertThat(actualList).usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(expectedList);

    }
    @Test
    void should_find_user_name(){
        when(userRepository.findByUserNameAndIsDeleted(anyString(), anyBoolean())).thenReturn(user);
        UserDTO actual = userService.findByUserName("user");
        assertThat(actual).usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(userDTO);

    }

    @Test
    void should_throw_exception_when_user_not_found(){

        Throwable throwable = catchThrowable(()->userService.findByUserName("SomeUsername"));
        assertInstanceOf(NoSuchElementException.class, throwable);
        assertEquals("User not found", throwable.getMessage());
    }





































}