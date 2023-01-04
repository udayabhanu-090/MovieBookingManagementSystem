package com.axis.MovieTicket.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.axis.MovieTicket.model.Token;
import com.axis.MovieTicket.model.User;
import com.axis.MovieTicket.repository.TokenRepository;
import com.axis.MovieTicket.repository.UserRepository;
import com.axis.MovieTicket.service.TokenService;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    @Override
    public String signUp(final String value) {
        final Token byValue = tokenRepository.findByValue(value);
        final User user = byValue.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        return "hello";
    }

    @Override
    public String welcome(final Principal principal, final Model model) {
        model.addAttribute("name", principal.getName());
        return "hello";
    }
}