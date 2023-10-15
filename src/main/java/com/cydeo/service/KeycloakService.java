package com.cydeo.service;

import com.cydeo.dto.UserDTO;

import javax.ws.rs.core.Response; // Response class -> access User in keycloak

public interface KeycloakService {


    Response userCreate(UserDTO dto);
    void delete(String username);

}
