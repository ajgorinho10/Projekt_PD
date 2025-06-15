package projekt.PD.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.User_Service.UserService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/*
 * Klasa CustomUserDetailsService implementuje UserDetailsService i jest odpowiedzialna za
 * ładowanie danych użytkownika na podstawie loginu z bazy danych.
 */

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Autowired
    public CustomUserDetailsService(@Lazy UserService userService) {
        this.userService = userService;
    }

/*
 * Metoda loadUserByUsername jest odpowiedzialna za pobieranie użytkownika z bazy danych i ustalenie jego roli
 * na podstawie loginu. Jeśli użytkownik nie zostanie znaleziony, rzuca wyjątek UsernameNotFoundException.
 * 
 */

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findUserByLogin(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        String[] roles = {user.getRoles()};
        List<SimpleGrantedAuthority> authorities = Arrays.stream(roles)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                user.getLogin(),
                user.getPassword(),
                authorities
        );
    }
}
