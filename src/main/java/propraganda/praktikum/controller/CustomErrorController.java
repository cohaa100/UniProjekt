package propraganda.praktikum.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;


@Slf4j
@ControllerAdvice
public class CustomErrorController {


    @ExceptionHandler(Exception.class)
    public String handleCityNotFoundException(final @AuthenticationPrincipal OAuth2User principal, final Model model,
                                              final Exception exception) {
        model.addAttribute("user",
                principal != null ?
                        principal.getAttribute("login") : null
        );

        model.addAttribute("timestamp", LocalDateTime.now());
        model.addAttribute("message", exception.getMessage());

        return "error";
    }


}
