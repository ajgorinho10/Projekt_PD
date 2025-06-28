package projekt.PD.DataBase.DB_User.User_Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.UserRepository;
import projekt.PD.Security.Auth.AuthRegister;
import projekt.PD.Security.Auth.AuthRequest;
import projekt.PD.Security.RestExceptions.Exceptions.LoginAlreadyExistException;
import projekt.PD.Util.TotpUtil;

import java.util.List;
import java.util.Optional;

/** Implementacja metod związanych z zarządzaniem użytkownikami */

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, SecurityContextRepository securityContextRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User findUserByLogin(String login) {
        Optional<User> user = userRepository.findByLogin(login);
        return user.orElse(null);
    }

    @Override
    public User findUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElse(null);
    }

    @Override
    public User createUser(AuthRegister input) {

        User user = new User();
        user.setLogin(input.getLogin());
        user.setPassword(passwordEncoder.encode(input.getPassword()));
        user.setFirstName(input.getFirstName());
        user.setLastName(input.getLastName());
        user.setRoles("ROLE_USER");

        if(userRepository.existsByLogin(user.getLogin())) {
            throw new LoginAlreadyExistException("Login już istnieje !");
        }

        return userRepository.save(user);
    }

    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public Boolean deleteUser(Long id) {
       userRepository.deleteById(id);

       return true;
    }

    @Override
    public void changeRole(Long id, String role) {
        Optional<User> user = userRepository.findById(id);
        user.ifPresent(value -> value.setRoles(role));
    }

    @Override
    public boolean ifUserExists(String login) {
        return userRepository.existsByLogin(login);
    }

    @Override
    public boolean ifUserExists(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    public List<User> getUsersByRole(String role) {
        return userRepository.getUsersByRoles(role);
    }


    /**
     * Attempts to authenticate a user with username, password, and (optionally) a TOTP code if 2FA is enabled.
     *
     * @param request      the authentication request containing username, password, and totpCode
     * @param httpRequest  the current HTTP request (used to access session and security context)
     * @param httpResponse the current HTTP response (used to store security context)
     * @return true if authentication succeeds (including 2FA if required), false otherwise
     */
    @Override
    public boolean loginWith2FA(AuthRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Optional<User> user = userRepository.findByLogin(request.getLogin());

        if (user.isEmpty()) {
            return false;
        }

        HttpSession session = httpRequest.getSession();
        if(user.get().isMfaEnabled()){
            session.setAttribute("totpCode", request.getTotpCode());
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getLogin(),request.getPassword()
                    )
            );

            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authentication);

            securityContextRepository.saveContext(securityContext, httpRequest, httpResponse);

            session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);


            return true;

        } catch (AuthenticationException e) {
            //e.printStackTrace();
            return false;
        }
    }

    /**
     * Generates a new TOTP secret for the given user, and stores it in the user's account.
     * This does not activate 2FA yet — verification is still required.
     *
     * @param userId the ID of the user for whom the secret is being generated
     * @return the newly generated Base32-encoded TOTP secret, or null if the user doesn't exist
     */
    @Override
    public String generateMfaSecret(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return null;
        }

        String secret = TotpUtil.generateSecret();

        user.get().setMfaSecret(secret);
        user.get().setMfaEnabled(false);
        user.get().setMfaVerified(false);
        userRepository.save(user.get());
        return secret;
    }

    /**
     * Verifies the provided TOTP code against the user's secret and enables 2FA if the code is valid.
     *
     * @param userId   the ID of the user attempting to verify and activate 2FA
     * @param totpCode the TOTP code provided by the user from their authenticator app
     * @return true if the code is valid and 2FA was successfully activated; false otherwise
     */
    @Override
    public boolean verifyAndEnableMfa(Long userId, String totpCode) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty() || user.get().isMfaEnabled() || user.get().getMfaSecret() == null) {
            return false;
        }
        // Weryfikuj kod TOTP
        boolean isValid = TotpUtil.verifyCode(user.get().getMfaSecret(),
                totpCode);
        if (isValid) {
            // Aktywuj 2FA
            user.get().setMfaEnabled(true);
            user.get().setMfaVerified(true);
            userRepository.save(user.get());
        }
        return isValid;
    }

    /**
     * Verifies a TOTP code against the user's active secret (only if 2FA is already enabled).
     *
     * @param userId   the ID of the user attempting verification
     * @param totpCode the code to be verified
     * @return true if the code is valid; false if invalid, expired, or user is not 2FA-enabled
     */
    @Override
    public boolean verifyTotp(Long userId, String totpCode) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty() || !user.get().isMfaEnabled() || user.get().getMfaSecret() == null) {
            return false;
        }
        return TotpUtil.verifyCode(user.get().getMfaSecret(), totpCode);

    }

    @Override
    public boolean disableMfa(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return false;
        }

        user.get().setMfaEnabled(false);
        user.get().setMfaSecret(null);
        user.get().setMfaVerified(false);
        userRepository.save(user.get());
        return true;
    }

}
