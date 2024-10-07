package me.bannock.website.services.user.impl.hibernate;

import me.bannock.website.security.Roles;
import me.bannock.website.services.user.User;
import me.bannock.website.services.user.UserService;
import me.bannock.website.services.user.UserServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class HibernateUserServiceImpl implements UserService {

    @Autowired
    public HibernateUserServiceImpl(PasswordEncoder passwordEncoder, UserRepository userRepository){
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    private final Logger logger = LogManager.getLogger();
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Value("${bannock.hibernateUsers.registrationsEnabled}")
    private boolean registrationsEnabled;
    @Value("${bannock.hibernateUsers.dummyRegistrationsEnabled}")
    private boolean dummyRegistrationsEnabled;

    @Override
    public User getUserWithId(long id) throws UserServiceException {
        Optional<UserEntity> userEntity = userRepository.findById(id);

        if (userEntity.isEmpty()){
            logger.info("Attempted to get user but no user exists for the provided id, id={}", id);
            throw new UserServiceException("No user with that id exists.",
                    "Id \"%s\" does not exist".formatted(id));
        }

        logger.info("Successfully got user using their id, userEntity={}", userEntity.get());
        return toDto(userEntity.get());
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getManyUsersWithIds(List<Long> ids) {
        List<UserEntity> userEntity = userRepository.findByIdIsIn(ids);
        return userEntity.stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserWithNameAndPassword(String name, String password) throws UserServiceException {
        Optional<UserEntity> userEntity = userRepository.findByName(name);

        if (userEntity.isEmpty()){
            logger.info("Attempted to get user but no user exists for username, username={}", name);
            throw new UserServiceException("No user with that name exists.",
                    "Username \"%s\" does not exist".formatted(name));
        }
        else if (userEntity.get().isUnclaimed()){
            logger.info("Attempted to get user but user has not claimed their account, username={}", name);
            throw new UserServiceException("Your account has not yet been claimed. " +
                    "Please head to the register page to claim your account.",
                    "User has not claimed their account");
        }
        else if (userEntity.get().getPassword().isEmpty()){
            logger.info("Attempted to get user by password is not set for user, userEntity={}", userEntity.get());
            throw new UserServiceException("Your password has not been set up yet. Please reset your password.",
                    "User's password is not set");
        }
        else if (!passwordEncoder.matches(password, userEntity.get().getPassword().get())){
            logger.info("Attempted to get user but password does not match, userEntity={}", userEntity.get());
            throw new UserServiceException("Incorrect password.", "Incorrect password");
        }

        logger.info("Successfully got user using their name and password, userEntity={}", userEntity.get());
        return toDto(userEntity.get());
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserWithEmail(String email) throws UserServiceException {
        Optional<UserEntity> userEntity = userRepository.findByEmail(email);
        if (userEntity.isEmpty()){
            logger.info("Attempted to get user but email does not exist, email={}", email);
            throw new UserServiceException("No account exists with the given email.",
                    "Email \"%s\" does not exist".formatted(email));
        }
        logger.info("Successfully got user using their email, userEntity={}", userEntity.get());
        return toDto(userEntity.get());
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserWithName(String name) throws UserServiceException {
        Optional<UserEntity> userEntity = userRepository.findByName(name);

        if (userEntity.isEmpty()){
            logger.info("Attempted to get user but no user exists for username, username={}", name);
            throw new UserServiceException("No user with that name exists.",
                    "Username \"%s\" does not exist".formatted(name));
        }

        logger.info("Successfully got user using their name, userEntity={}", userEntity.get());
        return toDto(userEntity.get());
    }

    @Override
    @Transactional
    public User registerDummyUser(String name, String email, String ip) throws UserServiceException {
        if (!dummyRegistrationsEnabled)
            throw new UserServiceException("Failed to created account because registrations are closed at this time");

        if (!name.matches("[0-9A-Za-z_-]+")){
            logger.warn("Could not create dummy user because their name did not pass the validation regex");
            throw new UserServiceException("Username contained invalid characters. " +
                    "Please only use latin letters a-Z, numbers, underscores(_), and dashs(-)");
        }

        if (userRepository.findByName(name).isPresent()){
            logger.warn("Could not create dummy user because their desired username is taken, name={}", name);
            throw new UserServiceException("Username is already taken");
        }
        if (userRepository.findByEmail(email).isPresent()){
            logger.warn("Could not create dummy user because their email is already registered, email={}", email);
            throw new UserServiceException("Email is already registered");
        }

        UserEntity userEntity = new UserEntity(name, email, ip);
        userEntity.getRoles().addAll(List.of(Roles.DEFAULT_USER_ROLES));
        userRepository.save(userEntity);
        logger.info("Created new dummy user, user={}", userEntity);
        return toDto(userEntity);
    }

    @Override
    @Transactional
    public User registerUser(User user) throws UserServiceException {
        if (!registrationsEnabled)
            throw new UserServiceException("Failed to created account because registrations are closed at this time");
        // TODO: Validations
        UserEntity userEntity = toEntity(user);
        List<String> roles = new ArrayList<>(userEntity.getRoles());
        roles.addAll(List.of(Roles.DEFAULT_USER_ROLES));
        userEntity.setRoles(roles);
        userRepository.save(userEntity);
        return toDto(userEntity);
    }

    /**
     * Maps a given user entity to a dto
     * @param userEntity The user entity to map
     * @return The dto equivalent to the entity
     */
    private User toDto(UserEntity userEntity){
        Objects.requireNonNull(userEntity);
        return new User(
                userEntity.getId(), userEntity.getName(),
                userEntity.getEmail(), userEntity.getLastIp(),
                userEntity.getPassword().orElse(null), new ArrayList<>(userEntity.getRoles()),
                userEntity.isVerified(), userEntity.isDisabled(),
                userEntity.isShadowBanned(), userEntity.isUnclaimed()
        );
    }

    /**
     * Maps a given user to an entity; no information pulled from repository
     * @param user The user to map
     * @return The entity equivalent to the dto
     */
    public UserEntity toEntity(User user){
        Objects.requireNonNull(user);
        return new UserEntity(
                user.getName(), user.getEmail(),
                user.getLastIp(), user.getPassword().orElse(null),
                user.getRoles(), user.isEmailVerified(),
                user.isAccountDisabled(), user.isShadowBanned(),
                user.isUnclaimedAccount()
        );
    }

}
