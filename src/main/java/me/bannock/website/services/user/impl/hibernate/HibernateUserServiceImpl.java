package me.bannock.website.services.user.impl.hibernate;

import me.bannock.website.services.user.User;
import me.bannock.website.services.user.UserService;
import me.bannock.website.services.user.UserServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
        UserEntity userEntity = new UserEntity(name, email, ip);
        userRepository.save(userEntity);
        return toDto(userEntity);
    }

    @Override
    @Transactional
    public User registerUser(User user) throws UserServiceException {
        UserEntity userEntity = toEntity(user);
        userRepository.saveAndFlush(userEntity);
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
