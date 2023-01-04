package com.axis.MovieTicket.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.axis.MovieTicket.model.Token;
import com.axis.MovieTicket.model.User;
import com.axis.MovieTicket.repository.TokenRepository;
import com.axis.MovieTicket.repository.UserRepository;
import com.axis.MovieTicket.service.MailService;
//import com.axis.MovieTicket.service.MailService;
//import com.axis.MovieTicket.service.MailService;
//import com.axis.MovieTicket.service.UserService;
//import com.axis.MovieTicket.service.UserService;
import com.axis.MovieTicket.service.UserService;

import javax.mail.MessagingException;
import java.security.Principal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final TokenRepository tokenRepository;
    private final MailService mailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public String login(final Principal principal) {
        if (principal != null && ((Authentication) principal).isAuthenticated()) {
            return "forward:/";
        } else {
            return "login";
        }
    }

    @Override
    public String register(final Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @Override
    public String registerSuccessfully(final User user, final BindingResult bindingResult) {
        if (userNameExists(user.getUsername())) {
            bindingResult.addError(new FieldError
                    ("user", "username", "The login you entered already exists"));
        }
        if (userNameExists(user.getEmail())) {
            bindingResult.addError(new FieldError
                    ("user", "email", "The email address you entered already exists"));
        }
        if (bindingResult.hasErrors()) {
            return "register";
        } else {
            addUser(user);
            return "redirect:register?success";
        }
    }

	private boolean userNameExists(String username ) {
		// TODO Auto-generated method stub
		return userNameExists(username);
	}

	private void addUser(final User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        userRepository.save(user);
        sendToken(user);
    }

    private void sendToken(final User user) {
        final String tokenValue = UUID.randomUUID().toString();
        final Token token = new Token();
        token.setValue(tokenValue);
        token.setUser(user);
        tokenRepository.save(token);
        String url = "localhost:8080/token?value=" + tokenValue;
        try {
            mailService.sendMail(user.getEmail(), "Register", url, false);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

}