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
import org.springframework.web.bind.annotation.PostMapping;
import propraganda.praktikum.logic.aggregate.benutzer.Benutzer;
import propraganda.praktikum.logic.aggregate.uebung.Uebung;
import propraganda.praktikum.logic.aggregate.uebung.Zeitslot;
import propraganda.praktikum.logic.services.benutzer.BenutzerService;
import propraganda.praktikum.logic.services.uebung.UebungsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@Slf4j
public class TutorController {

    @Autowired
    transient UebungsService uebungsService;
    @Autowired
    transient BenutzerService benutzerService;


    @Secured({"ROLE_TUTOR"})
    @GetMapping("/tutor/{uebungId}")
    public String zeitslotUebersicht(final @AuthenticationPrincipal OAuth2User principal, final @PathVariable("uebungId") long uebungId, final Model model) {
        model.addAttribute("user",
                principal != null ?
                        principal.getAttribute("login") : null
        );

        try {
            final Uebung uebung = uebungsService.getUebung(uebungId);
            if (principal != null && principal.getAttribute("login") != null) {
                final Optional<Benutzer> benutzer = benutzerService.findBenutzerByName((String) principal.getAttribute("login"));
                final Optional<Long> benutzerId = Optional.of(benutzer.get().getBenutzerId());
                model.addAttribute("benutzerId", benutzerId.orElse(0L));
            }
            final Set<Zeitslot> zeitslotList = uebung.getZeitslots();
            model.addAttribute("zeitslotList", zeitslotList);
            model.addAttribute("uebungId", uebungId);
            model.addAttribute("uebung", uebung);
        } catch (IllegalStateException illegalStateException) {
            log.error(illegalStateException.getMessage());
        }
        return "benutzer/tutor";
    }
    @Secured({"ROLE_TUTOR"})
    @GetMapping("/tutor/zeitslots/{id}")
    public String uebersicht(final @AuthenticationPrincipal OAuth2User principal, final @PathVariable("id") long zeitslotId, final Model model) {
        model.addAttribute("user",
                principal != null ?
                        principal.getAttribute("login") : null
        );

        assert principal != null;
        final Benutzer benutzer = benutzerService.findBenutzerByGithubId(Long.parseLong(principal.getName())).get();
        final Uebung uebung = uebungsService.findUebungByZeitslotId(zeitslotId);
        final Zeitslot zeitslot = uebungsService.findZeitslotById(uebung, zeitslotId);
        if(zeitslot.getTutorId().size() == zeitslot.getGroesse() && !zeitslot.getTutorId().contains(benutzer.getBenutzerId())) {
            return "redirect:/tutor/" + uebung.getUebungId() ;
        }

        final List<Benutzer> benutzerList = new ArrayList<>();

        zeitslot.getTutorId().forEach(id ->{

            benutzerList.add(benutzerService.findByBenutzerId(id).get());
                });
        model.addAttribute("benutzerId" , benutzer.getBenutzerId());
        model.addAttribute("zeitslot", zeitslot);
        model.addAttribute("benutzerList", benutzerList);



        return "benutzer/tutorZeitslots";
    }

    @Secured("ROLE_TUTOR")
    @PostMapping("/tutor/zeitslot/{id}/beitreten")
    public String zeitslotBeitreten(final @AuthenticationPrincipal OAuth2User principal, final Model model, @PathVariable("id") final Long zeitslotId){
        if(principal == null || principal.getAttribute("login")==null) {
            return "redirect:/tutor";
        }
        model.addAttribute("user",
                principal.getAttribute("login")
        );

        log.info("Test");

        final Uebung uebung = uebungsService.findUebungByZeitslotId(zeitslotId);
        final Zeitslot zeitslot = uebungsService.findZeitslotById(uebung, zeitslotId);

        final Optional<Benutzer> benutzer = benutzerService.findBenutzerByGithubId(Long.parseLong(principal.getName()));

        if(zeitslot.getGroesse() > zeitslot.getTutorId().size()) {
            zeitslot.getTutorId().add(benutzer.get().getBenutzerId());
        }

       uebungsService.save(uebung);

        return "redirect:/tutor/"+uebung.getUebungId();
    }

    @Secured("ROLE_TUTOR")
    @PostMapping("/tutor/zeitslot/{id}/verlassen")
    public String zeitslotVerlassen(final @AuthenticationPrincipal OAuth2User principal, final Model model, @PathVariable("id") final Long zeitslotId){
        if(principal == null || principal.getAttribute("login")==null) {
            return "redirect:/tutor";
        }
        model.addAttribute("user",
                principal.getAttribute("login")
        );



        final Uebung uebung = uebungsService.findUebungByZeitslotId(zeitslotId);
        final Zeitslot zeitslot = uebungsService.findZeitslotById(uebung, zeitslotId);

        final Optional<Benutzer> benutzer = benutzerService.findBenutzerByGithubId(Long.parseLong(principal.getName()));

        if(zeitslot.getTutorId().contains(benutzer.get().getBenutzerId())) {
            zeitslot.getTutorId().remove(benutzer.get().getBenutzerId());
        }

        uebungsService.save(uebung);

        return "redirect:/tutor/"+uebung.getUebungId();
    }



}
