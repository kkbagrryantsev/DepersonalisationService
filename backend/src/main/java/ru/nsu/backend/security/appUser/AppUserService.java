package ru.nsu.backend.security.appUser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.nsu.backend.exceptions.ResponseException;
import ru.nsu.backend.security.role.Role;
import ru.nsu.backend.security.role.RoleRepository;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AppUserService {
    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;

    public AppUser saveUser(AppUser user) throws ResponseException {
        checkUser(user);
        log.info("Save new user with name {}", user.getUsername());
        return userRepository.save(user);
    }


    public Role saveRole(Role role) {
        log.info("Save new role with name {}", role.getName());
        return roleRepository.save(role);
    }

    public AppUser createAppUser(String userName, String password, Collection<Role> roles) throws ResponseException {
        if (roles == null) {
            roles = new ArrayList<>();
        }
        return createAppUser(userName, password, roles.stream().map(Role::getName).collect(Collectors.toSet()));
    }

    public AppUser createAppUser(String userName, String password, Set<String> roles) throws ResponseException {
        if (roles == null) {
            roles = new HashSet<>();
        }
        checkPassword(password);
        checkUserName(userName);
        log.info("Adding new user {} with {}", userName, roles);
        List<String> notExists = new ArrayList<>();
        Set<Role> exists = new HashSet<>();
        for (String role : roles) {
            var foundedRole = roleRepository.findByName(role);
            foundedRole.ifPresent(exists::add);
            if (foundedRole.isEmpty()) {
                notExists.add(role);
            }
        }
        if (!notExists.isEmpty()) {
            log.error("for {}: Roles not exists {}", userName, notExists);
            throw new ResponseException(HttpStatus.BAD_REQUEST, "Roles not found: " + notExists);
        }
        AppUser newUser = new AppUser();
        newUser.setRoles(exists);
        newUser.setUsername(userName);
        newUser.setPassword(encoder.encode(password));
        log.info("Added new user {} with roles {}", userName, roles);
        return userRepository.save(newUser);
    }

    public AppUser createAppUser(String userName, String password) throws ResponseException {
        return createAppUser(userName, password, (Set<String>) null);
    }

    public AppUser createAppUser(AppUser appUser) throws ResponseException {
        return createAppUser(appUser.getUsername(), appUser.getPassword(), (List<Role>) null);
    }

    public AppUser addRoleToUser(String userName, String roleName) throws ResponseException {
        checkUserName(userName);
        log.info("Start adding role {} to user {}", roleName, userName);
        var user = userRepository.findByUsername(userName);
        if (user.isEmpty()) {
            log.warn("User {} not found", userName);
            throw new ResponseException(HttpStatus.NOT_FOUND, "User not found: " + userName);
        }
        var role = roleRepository.findByName(roleName);
        if (role.isEmpty()) {
                log.warn("Role {} not found", roleName);
            throw new ResponseException(HttpStatus.NOT_FOUND, "Role not found: " + roleName);
        }
        log.info("Added role {} to user {}", roleName, userName);
        user.get().getRoles().add(role.get());
        return user.get();
    }

    public void deleteRoleFromUser(String userName, String roleName) throws ResponseException {
        checkUserName(userName);
        log.info("Start delete role {} from user {}", roleName, userName);
        var user = userRepository.findByUsername(userName);
        if (user.isEmpty()) {
            log.warn("User {} not found", userName);
            throw new ResponseException(HttpStatus.NOT_FOUND, "User not found: " + userName);
        }
        var role = roleRepository.findByName(roleName);
        if (role.isEmpty()) {
            log.warn("Role {} not found", roleName);
            throw new ResponseException(HttpStatus.NOT_FOUND, "Role not found: " + roleName);
        }
        user.get().getRoles().remove(role.get());
        log.info("Deleted role {} from user {}", roleName, userName);
    }

    public AppUser getUser(String username) throws ResponseException {
        log.info("Getting user {}", username);
        var user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new ResponseException(HttpStatus.NOT_FOUND, "User " + username + " not found");
        }
    }

    public List<AppUser> getUsers() {
        log.info("Get users {}", SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        return userRepository.findAll();
    }

    public List<Role> getRoles() {
        return roleRepository.findAll();
    }


    public static void checkUserName(String username) throws ResponseException {
        if (username == null || username.isBlank()) {
            log.error("Null or empty username");
            throw new ResponseException(HttpStatus.BAD_REQUEST, "There is no username");
        }
        if (!username.matches("(\\w)+")) {
            log.error("Bad username {}", username);
            throw new ResponseException(HttpStatus.BAD_REQUEST, "Bad username. Username must contains" +
                    "only digits letters");
        }
    }

    public static void checkPassword(String password) throws ResponseException {
        if (password == null || password.isBlank()) {
            log.error("Null password in checking");
            throw new ResponseException(HttpStatus.BAD_REQUEST, "There is no password");
        }
        if (!password.matches("(\\S)+")) {
//            log.error("Bad password {}", password);
            throw new ResponseException(HttpStatus.BAD_REQUEST, "Bad password. Password must contains" +
                    "only digits letters and signs");
        }
    }

    public static void checkUser(AppUser user) throws ResponseException {
        checkUserName(user.getUsername());
        checkPassword(user.getPassword());
    }

    public static void checkRole(Role role) throws ResponseException {
        if (role == null) {
            log.error("Null role in checking");
            throw new ResponseException(HttpStatus.BAD_REQUEST, "There is no role");
        }
        if (!role.getName().matches("(\\S)+")) {
            log.error("Bad role name {}", role.getName());
            throw new ResponseException(HttpStatus.BAD_REQUEST, "Bad roleName. roleName must contains" +
                    "only digits letters");
        }
    }

    public boolean roleExists(Role role) {
        return roleExists(role.getName());
    }

    public boolean roleExists(String roleName) {
        return roleRepository.findByName(roleName).isPresent();
    }

}