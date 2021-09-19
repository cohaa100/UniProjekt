package propraganda.praktikum.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import propraganda.praktikum.logic.aggregate.uebung.Uebung;

import propraganda.praktikum.logic.services.uebung.UebungsService;

import java.util.*;

@Controller
@Slf4j
public class UebungController {

    @Autowired
    private transient UebungsService uebungsService;
    @Secured({"ROLE_STUDENT", "ROLE_TUTOR", "ROLE_ORGANISATOR"})
    @GetMapping("/uebungen")
    public String uebersicht(final @AuthenticationPrincipal OAuth2User principal, final Model model) {
        model.addAttribute("user",
                principal != null ?
                        principal.getAttribute("login") : null
        );

        final List<Uebung> uebungsList = new ArrayList<>(100);
        uebungsList.addAll(uebungsService.getAll());
        model.addAttribute("uebungsList", uebungsList);

        return "uebung/uebung";
    }


}
