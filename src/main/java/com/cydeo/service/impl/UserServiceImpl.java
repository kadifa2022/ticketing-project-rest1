package com.cydeo.service.impl;

import com.cydeo.annotation.DefaultExceptionMessage;
import com.cydeo.dto.ProjectDTO;
import com.cydeo.dto.TaskDTO;
import com.cydeo.dto.UserDTO;
import com.cydeo.entity.User;
import com.cydeo.exception.TicketingProjectException;
import com.cydeo.mapper.UserMapper;
import com.cydeo.repository.UserRepository;
import com.cydeo.service.ProjectService;
import com.cydeo.service.TaskService;
import com.cydeo.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ProjectService projectService;
    private final TaskService taskService;
    private final KeycloakServiceImpl keycloakService;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, @Lazy ProjectService projectService, @Lazy TaskService taskService, KeycloakServiceImpl keycloakService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.projectService = projectService;
        this.taskService = taskService;
        this.keycloakService = keycloakService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTO findByUserName(String username){
        User user = userRepository.findByUserNameAndIsDeleted(username, false);
       if(user == null)throw new NoSuchElementException("User not found");// if we can find the user we put exceptions
        return userMapper.convertToDto(user);
    }

    @Override
    public List<UserDTO> listAllUsers() {
        List<User> userList = userRepository.findAllByIsDeletedOrderByFirstNameDesc(false);
        return userList.stream().map(userMapper::convertToDto).collect(Collectors.toList());
    }

    @Override
    public UserDTO save(UserDTO user) {

        user.setEnabled(true); // is gonna take our userDTO and set the value to true we don't need to stub
        user.setPassWord(passwordEncoder.encode(user.getPassWord()));

        User obj = userMapper.convertToEntity(user);// if we don't have mapper with @Spy (userMapper) we will
        // need to stub this one, but since we have userMapper they will take userDTO and  convert to user entity

        User savedUser = userRepository.save(obj);// which is taking my entity and returning the entity/
        // then we need to stub this method

        keycloakService.userCreate(user); // don't need to stub keyckloak because is not returning anything  //after saving in db saved in keycloak
        return userMapper.convertToDto(savedUser);// re convert to DTO

    }

//    @Override
//    public void deleteByUserName(String username) {
//
//        userRepository.deleteByUserName(username);
//    }

    @Override
    public UserDTO update(UserDTO user) {

        //Find current user
        User user1 = userRepository.findByUserNameAndIsDeleted(user.getUserName(), false);  //has id
        //Encode password
        user.setPassWord(passwordEncoder.encode(user.getPassWord()));
        //Map update user dto to entity object
        User convertedUser = userMapper.convertToEntity(user);   // has id?
        //set id to the converted object
        convertedUser.setId(user1.getId());
        //save the updated user in the db
        userRepository.save(convertedUser);

        return findByUserName(user.getUserName());

    }

    @Override
    @DefaultExceptionMessage(defaultMessage = "Failed to delete user")
    public void delete(String username) throws TicketingProjectException{

        User user = userRepository.findByUserNameAndIsDeleted(username, false);

        if (checkIfUserCanBeDeleted(user)) {
            user.setIsDeleted(true);
            user.setUserName(user.getUserName() + "-" + user.getId());  // harold@manager.com-2
            userRepository.save(user);
        }else{
            throw new TicketingProjectException("User can not be deleted");
        }

    }


    @Override
    public List<UserDTO> listAllByRole(String role) {
        List<User> users = userRepository.findByRoleDescriptionIgnoreCaseAndIsDeleted(role, false);
        return users.stream().map(userMapper::convertToDto).collect(Collectors.toList());
    }

    private boolean checkIfUserCanBeDeleted(User user) {

        switch (user.getRole().getDescription()) {
            case "Manager":
                List<ProjectDTO> projectDTOList = projectService.listAllNonCompletedByAssignedManager(userMapper.convertToDto(user));
                return projectDTOList.size() == 0;
            case "Employee":
                List<TaskDTO> taskDTOList = taskService.listAllNonCompletedByAssignedEmployee(userMapper.convertToDto(user));
                return taskDTOList.size() == 0;
            default:
                return true;
        }

    }

}
