package com.emobile.springtodo.dto.mapper;

import com.emobile.springtodo.dto.request.CreateUserRequest;
import com.emobile.springtodo.entity.ServiceUser;
import com.emobile.springtodo.security.SecurityRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Aleksey Shvariov
 */


@SpringBootTest
public class ServiceUserMapperTest {

    @Autowired
    private ServiceUserMapper serviceUserMapper;

    @Test
    public void testMapFrom_whenParamIsValidCreateUserRequest_thenReturnValidServiceUser() {
        final Set<SecurityRole> expectedRoles = Set.of(SecurityRole.ROLE_USER, SecurityRole.ROLE_ADMIN);
        CreateUserRequest request = CreateUserRequest.builder()
                .username("username33")
                .email("user33@email.com")
                .password("password33")
                .roleNames(expectedRoles.stream().map(SecurityRole::name).collect(Collectors.toSet()))
                .build();

        ServiceUser user = serviceUserMapper.mapFrom(request);

        Assertions.assertEquals(user.getEmail(), request.getEmail());
        Assertions.assertEquals(user.getPassword(), request.getPassword());
        Assertions.assertEquals(expectedRoles, user.getRoles());
    }

}
