package com.cydeo.service.impl;
import com.cydeo.config.KeycloakProperties;
import com.cydeo.dto.UserDTO;
import com.cydeo.service.KeycloakService;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import javax.ws.rs.core.Response;
import java.util.List;
import static java.util.Arrays.asList;
import static org.keycloak.admin.client.CreatedResponseUtil.getCreatedId;

@Service
public class  KeycloakServiceImpl implements KeycloakService {
    private final KeycloakProperties keycloakProperties;// using variables

    public KeycloakServiceImpl(KeycloakProperties keycloakProperties) {
        this.keycloakProperties = keycloakProperties;
    }
    @Override
    public Response userCreate(UserDTO userDTO) {

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setTemporary(false);//if is true we need to reset password
        credential.setValue(userDTO.getPassWord());

        // we created new class from UserRepresentation class which is coming from keycloak
        UserRepresentation keycloakUser = new UserRepresentation();
        keycloakUser.setUsername(userDTO.getUserName());
        keycloakUser.setFirstName(userDTO.getFirstName());
        keycloakUser.setLastName(userDTO.getLastName());
        keycloakUser.setEmail(userDTO.getUserName());
        keycloakUser.setCredentials(asList(credential));
        keycloakUser.setEmailVerified(true);
        keycloakUser.setEnabled(true);

        // any action in keycloak need to open instance first in keycloak first-> same>concept as DB
        Keycloak keycloak = getKeycloakInstance();

        RealmResource realmResource = keycloak.realm(keycloakProperties.getRealm());//realm Cydeo-dev field
        UsersResource usersResource = realmResource.users();

        // This create() method is creating  Keycloak user
        Response result = usersResource.create(keycloakUser);

        String userId = getCreatedId(result);// when we create user in keycloak, user is creating id for the user
        ClientRepresentation appClient = realmResource.clients()
                .findByClientId(keycloakProperties.getClientId()).get(0);

        RoleRepresentation userClientRole = realmResource.clients().get(appClient.getId()) //user has a role, and is going to keycloak and is checking roles one by one
                .roles().get(userDTO.getRole().getDescription()).toRepresentation();//finding role and assigning to user

        realmResource.users().get(userId).roles().clientLevel(appClient.getId())
                .add(List.of(userClientRole));

        keycloak.close();
        return result;
    }
    @Override
    public void delete(String userName) { //delete()  based on Unique Id

        Keycloak keycloak = getKeycloakInstance();
        RealmResource realmResource = keycloak.realm(keycloakProperties.getRealm());
        UsersResource usersResource = realmResource.users();

        List<UserRepresentation> userRepresentations = usersResource.search(userName);
        String uid = userRepresentations.get(0).getId();
        usersResource.delete(uid);
        keycloak.close();
    }
    private Keycloak getKeycloakInstance(){// this is ready method from documentation
        return Keycloak.getInstance(keycloakProperties.getAuthServerUrl(), // these instances are coming from keycloak
                keycloakProperties.getMasterRealm(), keycloakProperties.getMasterUser()
                , keycloakProperties.getMasterUserPswd(), keycloakProperties.getMasterClient());
    }
}