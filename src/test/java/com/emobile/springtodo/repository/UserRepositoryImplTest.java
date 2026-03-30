package com.emobile.springtodo.repository;

import com.emobile.springtodo.entity.ServiceUser;
import com.emobile.springtodo.repository.impl.UserRepositoryImpl;
import com.emobile.springtodo.repository.impl.mapper.ServiceUserRowMapper;
import com.emobile.springtodo.security.SecurityRole;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryImplTest {

        @Autowired
        private JdbcTemplate jdbcTemplate;

        @DynamicPropertySource
        private static void setProperties(DynamicPropertyRegistry registry) {
                registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
                registry.add("spring.datasource.username", POSTGRES::getUsername);
                registry.add("spring.datasource.password", POSTGRES::getPassword);
                registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
        }

        private final static PostgreSQLContainer<?> POSTGRES =
                new PostgreSQLContainer<>("postgres");

        @BeforeAll
        public static void startDatabase() {
                POSTGRES.start();
        }


        @AfterAll
        public static void stopDatabase() {
                POSTGRES.stop();
        }



        @Nested
        class FindAllTests {

            @Test
            @Sql("test_data.sql")
            @DisplayName("Test findAll when database not empty then return list of users")
            public void testFindAll_whenDbNotEmpty_thenReturnUser() {
                UserRepository userRepository = new UserRepositoryImpl(jdbcTemplate, new ServiceUserRowMapper());
                List<ServiceUser> serviceUsers = userRepository.findAll();
                serviceUsers.forEach(System.out::println);
                Assertions.assertFalse(serviceUsers.isEmpty());
                Set<SecurityRole> roles = serviceUsers.stream().findFirst().orElseThrow().getRoles();
                Assertions.assertFalse(roles.isEmpty());
            }


            @Test
            @DisplayName("Test findAll when database is empty then return empty list")
            public void testFindAll_whenDbIsEmpty_thenReturnEmptyList() {
                UserRepository userRepository = new UserRepositoryImpl(jdbcTemplate, new ServiceUserRowMapper());
                List<ServiceUser> serviceUsers = userRepository.findAll();
                Assertions.assertTrue(serviceUsers.isEmpty());
            }
        }

        @Nested
        class FindAllPageableTests {
            @Test
            @Sql("test_data.sql")
            @DisplayName("Test findAll with pageable when database not empty then return page of users")
            public void testPageableFindAll_whenDatabaseNotEmpty_thenReturnPageOfUsers() {
                UserRepository userRepository = new UserRepositoryImpl(jdbcTemplate, new ServiceUserRowMapper());
                final int pageNum = 0;
                final int pageSize = 2;
                final int expectedTotalElements = 3;
                Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.ASC, "email"));
                Page<ServiceUser> usersPage = userRepository.findAll(pageable);
                usersPage.get().forEach(System.out::println);
                Assertions.assertEquals(pageSize, usersPage.get().count());
                Assertions.assertEquals(expectedTotalElements, usersPage.getTotalElements());
                Set<SecurityRole> roles = usersPage.get().findFirst().orElseThrow().getRoles();
                Assertions.assertFalse(roles.isEmpty());
            }


            @Test
            @DisplayName("Test findAll with pageable when database is empty then return empty page")
            public void testPageableFindAll_whenDatabaseIsEmpty_thenEmptyPage() {
                UserRepository userRepository = new UserRepositoryImpl(jdbcTemplate, new ServiceUserRowMapper());
                final int pageNum = 0;
                final int pageSize = 2;
                final int expectedTotalElements = 0;
                Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.ASC, "email"));
                Page<ServiceUser> usersPage = userRepository.findAll(pageable);
                Assertions.assertTrue(usersPage.isEmpty());
                Assertions.assertEquals(expectedTotalElements, usersPage.getTotalElements());
            }
        }

        @Nested
        class FindByIdTests {

            @Test
            @Sql("test_data.sql")
            @DisplayName("Test findById when user is found then return Optional of user")
            public void testFindById_whenUserIsFound_thenReturnOptionalOfUser() {
                long userid = 1L;
                UserRepository userRepository = new UserRepositoryImpl(jdbcTemplate, new ServiceUserRowMapper());
                Optional<ServiceUser> userOptional = userRepository.findById(userid);
                Assertions.assertTrue(userOptional.isPresent());
                Assertions.assertEquals(userid, userOptional.get().getId());
            }

            @Test
            @Sql("test_data.sql")
            @DisplayName("Test findById when user is not found then return empty optional")
            public void testFindById_whenUserIsNotFound_thenReturnOptionalOfUser() {
                long userid = 1000L;
                UserRepository userRepository = new UserRepositoryImpl(jdbcTemplate, new ServiceUserRowMapper());
                Optional<ServiceUser> userOptional = userRepository.findById(userid);
                Assertions.assertTrue(userOptional.isEmpty());
            }
        }

    @Nested
    class ExistByEmailTests {

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test existByEmail when user is found then return true")
        public void testExistByEmail_whenUserIsFound_thenReturnTrue() {
            String email = "user3@email.com";
            UserRepository userRepository = new UserRepositoryImpl(jdbcTemplate, new ServiceUserRowMapper());
            Assertions.assertTrue(userRepository.existByEmail(email));
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test existByEmail when user is not found then return false")
        public void testExistByEmail_whenUserIsNotFound_thenReturnFalse() {
            String email = "noUserWithThisEmail@email.com";
            UserRepository userRepository = new UserRepositoryImpl(jdbcTemplate, new ServiceUserRowMapper());
            Assertions.assertFalse(userRepository.existByEmail(email));
        }
    }

    @Nested
    class ExistByUsernameTests {

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test existByUsername when user is found then return true")
        public void testExistByUsername_whenUserIsFound_thenReturnTrue() {
            String username = "user2";
            UserRepository userRepository = new UserRepositoryImpl(jdbcTemplate, new ServiceUserRowMapper());
            Assertions.assertTrue(userRepository.existByUsername(username));
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test existByUsername when user is not found then return false")
        public void testExistByUsername_whenUserIsNotFound_thenReturnFalse() {
            String username = "notFoundUsername";
            UserRepository userRepository = new UserRepositoryImpl(jdbcTemplate, new ServiceUserRowMapper());
            Assertions.assertFalse(userRepository.existByUsername(username));
        }
    }



    @Nested
    class FindByUsernameOrEmailTests {

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test findByUsernameAndEmail when user is found by username then return Optional of user")
        public void testFindByUsernameAndEmail_whenUserIsFoundByUsername_thenReturnOptionalOfUser() {
            String username = "user3";
            UserRepository userRepository = new UserRepositoryImpl(jdbcTemplate, new ServiceUserRowMapper());
            Optional<ServiceUser> userOptional = userRepository.findByUsernameOrEmail(username, username);
            Assertions.assertTrue(userOptional.isPresent());
            Assertions.assertEquals(username, userOptional.get().getUsername());
            Set<SecurityRole> roles = userOptional.orElseThrow().getRoles();
            Assertions.assertFalse(roles.isEmpty());
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test findByUsernameAndEmail when user is found by email then return Optional of user")
        public void testFindByUsernameAndEmail_whenUserIsFoundByEmail_thenReturnOptionalOfUser() {
            String email = "testuser@email.com";
            UserRepository userRepository = new UserRepositoryImpl(jdbcTemplate, new ServiceUserRowMapper());
            Optional<ServiceUser> userOptional = userRepository.findByUsernameOrEmail(email, email);
            Assertions.assertTrue(userOptional.isPresent());
            Assertions.assertEquals(email, userOptional.get().getEmail());
            Set<SecurityRole> roles = userOptional.orElseThrow().getRoles();
            Assertions.assertFalse(roles.isEmpty());
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test findByUsernameAndEmail when user is not found then return empty optional")
        public void testFindByUsernameAndEmail_whenUserIsNotFound_thenReturnOptionalOfUser() {
            String username = "userNotFound";
            UserRepository userRepository = new UserRepositoryImpl(jdbcTemplate, new ServiceUserRowMapper());
            Optional<ServiceUser> userOptional = userRepository.findByUsernameOrEmail(username, username);
            Assertions.assertTrue(userOptional.isEmpty());
        }
    }

        @Nested
        class SaveTests {

            @Test
            @Sql("test_data.sql")
            @DisplayName("Test save when add new user then return saved entity")
            public void testSave_whenAddNewUser_thenReturnSavedUser() {
                ServiceUser expectedUser = ServiceUser.builder()
                        .email("expectedUser@mail.com")
                        .username("addusername")
                        .password("adduserpassword")
                        .roles(Set.of(SecurityRole.ROLE_ADMIN))
                        .build();
                UserRepository userRepository = new UserRepositoryImpl(jdbcTemplate, new ServiceUserRowMapper());
                ServiceUser savedUser = userRepository.save(expectedUser);
                Assertions.assertNotNull(savedUser);
                Assertions.assertNotNull(savedUser.getId());
                Assertions.assertEquals(expectedUser.getEmail(), savedUser.getEmail());
                Assertions.assertEquals(expectedUser.getUsername(), savedUser.getUsername());
                Assertions.assertEquals(expectedUser.getRoles(), savedUser.getRoles());
            }

            @Test
            @Sql("test_data.sql")
            @DisplayName("Test save when update user then return saved entity")
            public void testSave_whenUpdateUserData_thenReturnUpdatedUser() {
                UserRepository userRepository = new UserRepositoryImpl(jdbcTemplate, new ServiceUserRowMapper());
                Long userId = 2L;
                ServiceUser expectedUser = userRepository.findById(userId).orElseThrow();
                expectedUser.setEmail("adduser@mail.com");
                expectedUser.setRoles(Set.of(SecurityRole.ROLE_ADMIN, SecurityRole.ROLE_USER));

                ServiceUser userFromDb = userRepository.save(expectedUser);

                Assertions.assertNotNull(userFromDb);
                Assertions.assertEquals(userId, userFromDb.getId());
                Assertions.assertEquals(expectedUser.getEmail(), userFromDb.getEmail());
                Assertions.assertNotNull(userFromDb.getUsername());
                Assertions.assertEquals(expectedUser.getRoles(), userFromDb.getRoles());
            }
        }

        @Nested
        class DeleteMethodsTests {

            @Test
            @Sql("test_data.sql")
            @DisplayName("Test deleteAll when call method then table is cleared")
            public void testDeleteALl_whenCallMethod_thenTableCleared() {
                final UserRepository userRepository = new UserRepositoryImpl(jdbcTemplate, new ServiceUserRowMapper());
                long beforeCount = userRepository.count();
                userRepository.deleteAll();
                Assertions.assertTrue(beforeCount > 0);
                Assertions.assertEquals(0, userRepository.count());
            }

            @Test
            @Sql("test_data.sql")
            @DisplayName("Test deleteById when call method then user is deleted")
            public void testDeleteById_whenCallMethod_thenUserIsDeleted() {
                final UserRepository userRepository = new UserRepositoryImpl(jdbcTemplate, new ServiceUserRowMapper());
                long beforeCount = userRepository.count();
                userRepository.deleteById(2L);
                long afterCount = userRepository.count();
                Assertions.assertTrue(beforeCount > afterCount);

            }
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test count when table not empty then return valid value")
        public void testCount_whenTableNotEmpty_thenReturnValidValue() {
                long expectedCount = 3;
                UserRepository userRepository = new UserRepositoryImpl(jdbcTemplate, new ServiceUserRowMapper());
                long count = userRepository.count();
                Assertions.assertEquals(expectedCount, count);
        }


}
