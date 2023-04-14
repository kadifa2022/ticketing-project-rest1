package com.cydeo.unit_test_review;

import com.cydeo.dto.ProjectDTO;
import com.cydeo.dto.RoleDTO;
import com.cydeo.dto.TaskDTO;
import com.cydeo.dto.UserDTO;
import com.cydeo.entity.Role;
import com.cydeo.entity.User;
import com.cydeo.exception.TicketingProjectException;
import com.cydeo.mapper.UserMapper;
import com.cydeo.repository.UserRepository;
import com.cydeo.service.KeycloakService;
import com.cydeo.service.ProjectService;
import com.cydeo.service.TaskService;
import com.cydeo.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;
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
    @Mock
    private PasswordEncoder passwordEncoder;

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
            //AssertJ test dependency is coming from another library
        assertThat(actualList).usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(expectedList);
        //usingRecursiveComparison() comparing field inside the object, it will tell if they are same
        //ignoringExpectedNullField from same library  //AssertJ Dependency
    }
    @Test
    void should_find_user_by_name(){
        when(userRepository.findByUserNameAndIsDeleted(anyString(), anyBoolean())).thenReturn(user);
        UserDTO actual = userService.findByUserName("user");
       // assertEquals(userDTO,actual);
        assertThat(actual).usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(userDTO);
        //fields and value inside the object should be equals in my userDto->and ignoring null fields
    }

    @Test
    void should_throw_exception_when_user_not_found(){ //can be done in 2 different way

        Throwable throwable = catchThrowable(()->userService.findByUserName("SomeUsername"));
        assertInstanceOf(NoSuchElementException.class, throwable);
        assertEquals("User not found", throwable.getMessage());


//        assertThrows(NoSuchElementException.class, ()->userService.findByUserName("SomeUsername"));
//        assertEquals("User not found", throwable.getMessage());
//        assertThrowsExactly(NoSuchElementException.class, ()->userService.findByUserName("SomeUsername"));
//        assertEquals("User not found", throwable.getMessage());
    }


    @Test
    void should_save_user() {

        when(userRepository.save(any())).thenReturn(user);

        UserDTO actualDTO = userService.save(userDTO);

        assertThat(actualDTO).usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(userDTO);

        verify(passwordEncoder).encode(anyString());
    }

    @Test
    void should_update_user(){

        when(userRepository.findByUserNameAndIsDeleted(anyString(), anyBoolean())).thenReturn(user);

        when(userRepository.save(any())).thenReturn(user);

        UserDTO actualDTO = userService.update(userDTO);

        verify(passwordEncoder).encode(anyString());

        assertThat(actualDTO).usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(userDTO);

    }
    @Test
    void should_delete_manager() throws TicketingProjectException {
        User managerUser = getUser("Manager");

        when(userRepository.findByUserNameAndIsDeleted(anyString(), anyBoolean())).thenReturn(managerUser);
        when(userRepository.save(any())).thenReturn(managerUser);
        when(projectService.listAllNonCompletedByAssignedManager(any())).thenReturn(new ArrayList<>());
        userService.delete(userDTO.getUserName());

        assertTrue(managerUser.getIsDeleted());
        assertNotEquals("user3", managerUser.getUserName());


    }

    @Test
    void should_delete_employee() throws TicketingProjectException {
        User employeeUser = getUser("Employee");

        when(userRepository.findByUserNameAndIsDeleted(anyString(), anyBoolean())).thenReturn(employeeUser);
        when(userRepository.save(any())).thenReturn(employeeUser);
        when(taskService.listAllNonCompletedByAssignedEmployee(any())).thenReturn(new ArrayList<>());
        userService.delete(userDTO.getUserName());

        assertTrue(employeeUser.getIsDeleted());
        assertNotEquals("user3", employeeUser.getUserName());


    }

    @Test
    void should_throw_exception_when_delete_manager() throws TicketingProjectException {
        User managerUser = getUser("Manager");

        when(userRepository.findByUserNameAndIsDeleted(anyString(), anyBoolean())).thenReturn(managerUser);

        when(projectService.listAllNonCompletedByAssignedManager(any())).thenReturn(List.of(new ProjectDTO(), new ProjectDTO()));

        Throwable throwable = catchThrowable(()->userService.delete(userDTO.getUserName()));

        assertInstanceOf(TicketingProjectException.class, throwable);
        assertEquals("User can not be deleted", throwable.getMessage());

        verify(userMapper, atLeastOnce()).convertToDto(any());


    }
    @ParameterizedTest
    @ValueSource(strings = {"Manager", "Employee"})
    void should_delete_user(String value) throws TicketingProjectException {
        User testUser = getUser(value);
        when(userRepository.findByUserNameAndIsDeleted(anyString(), anyBoolean())).thenReturn(testUser);
        when(userRepository.save(any())).thenReturn(testUser);
        lenient().when(projectService.listAllNonCompletedByAssignedManager(any())).thenReturn(new ArrayList<>());
        lenient().when(taskService.listAllNonCompletedByAssignedEmployee(any())).thenReturn(new ArrayList<>());
         userService.delete(userDTO.getUserName());

         assertTrue(testUser.getIsDeleted());
         assertNotEquals("user3", testUser.getUserName());
    }




    @Test
    void should_throw_exception_when_delete_employee() throws TicketingProjectException {
        User employeeUser = getUser("Employee");

        when(userRepository.findByUserNameAndIsDeleted(anyString(), anyBoolean())).thenReturn(employeeUser);

        when(taskService.listAllNonCompletedByAssignedEmployee(any())).thenReturn(List.of(new TaskDTO(), new TaskDTO()));

        Throwable throwable = catchThrowable(()->userService.delete(userDTO.getUserName()));

        assertInstanceOf(TicketingProjectException.class, throwable);
        assertEquals("User can not be deleted", throwable.getMessage());

        verify(userMapper, atLeastOnce()).convertToDto(any());


    }







    private User getUser(String role){
        User user3 = new User();
        user3.setEnabled(false);
        user3.setIsDeleted(false);
        user3.setRole(new Role(role));
        return user3;

}




































}
