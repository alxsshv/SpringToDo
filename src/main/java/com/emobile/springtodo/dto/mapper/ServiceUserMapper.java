package com.emobile.springtodo.dto.mapper;


import com.emobile.springtodo.dto.request.CreateUserRequest;
import com.emobile.springtodo.dto.request.RegisterUserRequest;
import com.emobile.springtodo.dto.response.ServiceUserResponse;
import com.emobile.springtodo.entity.ServiceUser;
import com.emobile.springtodo.security.SecurityRole;
import org.mapstruct.*;

import java.util.Set;

/**
 * Интерфейс для преобразования сущности {@link ServiceUser} в объекты передачи данных и наоборот.
 * @author Shvariov Alexei
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ServiceUserMapper {

    /** Преобразование DTO {@link CreateUserRequest} в сущность {@link ServiceUser}
     * @param request  - объект передачи данных {@link CreateUserRequest}.
     * @return возвращает сущность {@link ServiceUser}.
     */
    @Mapping(target = "roles", qualifiedByName = "getRolesFromRoleNames", source = "roleNames")
    ServiceUser mapFrom(CreateUserRequest request);

    /** Преобразование DTO {@link RegisterUserRequest} в сущность {@link ServiceUser}
     * @param request - объект передачи данных {@link RegisterUserRequest}.
     * @return возвращает сущность {@link ServiceUser} */
    ServiceUser mapFrom(RegisterUserRequest request);

    /** Преобразование сущности {@link ServiceUser} в DTO {@link ServiceUserResponse}
     * @param user - объект класса {@link ServiceUser}, который необходимо преобразовать.
     * @return возвращает объект передачи данных {@link ServiceUserResponse} */
    ServiceUserResponse mapToServiceUserResponse(ServiceUser user);


    /** Именованный дефолтный метод, определяющий логику получения набора ролей пользователя
     * из набора имён ролей пользователя.
     * @param roleNames - набор имён ролей пользователя.
     * @return возвращает набор ролей пользователя из enum {@link SecurityRole} */
    @Named("getRolesFromRoleNames")
    default Set<SecurityRole> getRolesFromRoleNames(Set<String> roleNames) {
        return SecurityRole.valuesOfNames(roleNames);
    }
}
