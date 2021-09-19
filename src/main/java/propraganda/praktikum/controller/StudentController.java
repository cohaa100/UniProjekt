package propraganda.praktikum.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import propraganda.praktikum.logic.aggregate.uebung.Termin;
import propraganda.praktikum.logic.aggregate.uebung.Uebung;
import propraganda.praktikum.logic.services.uebung.UebungsService;

import java.util.Set;

@Controller
@Slf4j
@Secured({"ROLE_STUDENT", "ROLE_ORGANISATOR"})
public class StudentController {

    @Autowired
    private transient UebungsService uebungsService;


    @GetMapping("/student/{uebungId}")
    public String terminUebersicht(final @AuthenticationPrincipal OAuth2User principal, final Model model, final @PathVariable("uebungId") long uebungId) {
        model.addAttribute("user",
                principal != null ?
                        principal.getAttribute("login") : null
        );

        try {
            final Uebung uebung = uebungsService.getUebung(uebungId);
            final Set<Termin> terminList = uebung.getTermin();
            model.addAttribute("terminList", terminList);
            model.addAttribute("uebungId", uebungId);
            model.addAttribute("uebung", uebung);
        } catch (IllegalStateException illegalStateException) {
            log.error(illegalStateException.getMessage());
        }
        return "benutzer/student";
    }

}
