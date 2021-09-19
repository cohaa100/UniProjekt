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
import org.springframework.web.bind.annotation.RequestParam;
import propraganda.praktikum.logic.aggregate.benutzer.Benutzer;
import propraganda.praktikum.logic.aggregate.uebung.Termin;
import propraganda.praktikum.logic.aggregate.uebung.AnmeldeTyp;
import propraganda.praktikum.logic.aggregate.uebung.Uebung;
import propraganda.praktikum.logic.services.benutzer.BenutzerService;
import propraganda.praktikum.logic.services.uebung.GruppenService;
import propraganda.praktikum.logic.services.uebung.TerminService;
import propraganda.praktikum.logic.services.uebung.UebungsService;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@Slf4j
@Secured("ROLE_ORGANISATOR")
public class TerminController {
    @Autowired
    private transient UebungsService uebungsService;
    @Autowired
    private transient GruppenService gruppenService;
    @Autowired
    private transient BenutzerService benutzerService;
    @Autowired
    private transient TerminService terminService;

    @Secured({"ROLE_STUDENT", "ROLE_TUTOR", "ROLE_ORGANISATOR"})
    @GetMapping("/termin/{terminId}")
    public String uebersicht(final @AuthenticationPrincipal OAuth2User principal, @PathVariable("terminId") final Long terminId, final Model model) {
        model.addAttribute("user",
                principal != null ?
                        principal.getAttribute("login") : null
        );

        assert principal != null;
        final Benutzer benutzer = benutzerService.findBenutzerByGithubId(Long.parseLong(principal.getName())).get();
        final Optional<Uebung> uebung = uebungsService.findUebungByTerminId(terminId);
        final boolean gruppenAnmeldung = uebung.get().getAnmeldeTyp() == AnmeldeTyp.GRUPPE;



        final Optional<Termin> termin = terminService.idZuTermin(uebung.get(), terminId);
        final Set<Long> benutzerIdListe = new HashSet<>(termin.get().getGruppe().getMitglieder());

        final Optional<Benutzer> tutor = benutzerService.findByBenutzerId(terminService.getTerminTutor(uebung.get(), terminId).orElse(0L));
        final Set<Benutzer> benutzerListe = benutzerService.findBenutzer(benutzerIdListe);

        model.addAttribute("benutzerId" , benutzer.getBenutzerId());
        model.addAttribute("termin",termin.get());
        model.addAttribute("tutor", tutor.orElse(null));
        model.addAttribute("benutzerListe", benutzerListe);
        model.addAttribute("gruppenAnmeldung", gruppenAnmeldung);
        //Refactored imAnmeldeZeitraum due to DD
        if(uebung.isPresent()) {
            model.addAttribute("imAnmeldeZeitraum", uebung.get().getAnmeldezeitraum().getZeitraumStart().isBefore(LocalDateTime.now()) &&
                    uebung.get().getAnmeldezeitraum().getZeitraumEnde().isAfter(LocalDateTime.now()));
        }else{
            model.addAttribute("imAnmeldeZeitraum", false);
        }

        return "termin/termin";
    }
    @Secured("ROLE_STUDENT")
    @PostMapping("/termin/{terminId}/belegen")
    public String terminBelegen(final @AuthenticationPrincipal OAuth2User principal, final Model model, @RequestParam final String gruppenName, @RequestParam final Set<String> gitnamen, @PathVariable("terminId") final Long terminId){
        if(principal == null || principal.getAttribute("login")==null) {
            return "redirect:/termin/" + terminId;
        }
        model.addAttribute("user",
                principal.getAttribute("login")
        );

        gitnamen.add(principal.getAttribute("login"));


        final Optional<Uebung> uebung = uebungsService.findUebungByTerminId(terminId);
        final Optional<Termin> termin = terminService.idZuTermin(uebung.get(), terminId);
        final Set<Benutzer> benutzerSet = benutzerService.findBenutzerByName(gitnamen);
        final Set<Long> benutzerIdSet = benutzerService.changeBenutzerToLong(benutzerSet);



        if(!termin.get().isBelegt()){
           gruppenService.erstelleGruppeBenutzer(terminId,gruppenName,benutzerIdSet, uebungsService.findUebungByTerminId(terminId).get().getUebungId());
        }
        return "redirect:/termin/" + terminId ;
    }

    @Secured("ROLE_STUDENT")
    @GetMapping("/termin/{terminId}/belegen")
    public String terminBelegenGet(final @AuthenticationPrincipal OAuth2User principal, final Model model, @PathVariable("terminId") final Long terminId){
        model.addAttribute("user",
                principal != null ?
                        principal.getAttribute("login") : null
        );

        final Optional<Uebung> uebung = uebungsService.findUebungByTerminId(terminId);
        final Optional<Termin> termin = terminService.idZuTermin(uebung.get(), terminId);

        model.addAttribute("gruppenMitgliederNameSet", new HashSet<String>());


        model.addAttribute("termin", termin.orElseThrow());
        return "termin/terminBelegen";
    }
    @Secured("ROLE_STUDENT")
    @PostMapping("/termin/{terminId}/beitreten")
    public String terminBeitreten(final @AuthenticationPrincipal OAuth2User principal, final Model model, @PathVariable("terminId") final Long terminId){
        if(principal == null || principal.getAttribute("login")==null) {
            return "redirect:/termin/" + terminId;
        }
        model.addAttribute("user",
                        principal.getAttribute("login")
        );



        final Uebung uebung = uebungsService.findUebungByTerminId(terminId).get();
        final Termin termin = terminService.idZuTermin(uebung, terminId).get();

        model.addAttribute("uebungId", uebung.getUebungId());

        final String user = principal.getAttribute("login");

        final Optional<Benutzer> benutzer = benutzerService.findBenutzerByGithubId(Long.parseLong(principal.getName()));

        if(uebung.getAnmeldeTyp() == AnmeldeTyp.INDIVIDUAL){
            if(!termin.isBelegt()){
                gruppenService.erstelleGruppeBenutzer(terminId,user,Set.of(benutzer.get().getBenutzerId()),uebung.getUebungId());
            } else {
                gruppenService.mitgliedHinzufuegen(terminId,Set.of(benutzer.get().getBenutzerId()),uebung.getUebungId());
            }
        }
        if(uebung.getAnmeldeTyp() == AnmeldeTyp.GRUPPE && termin.isBelegt()){
            gruppenService.mitgliedHinzufuegen(terminId,Set.of(benutzer.get().getBenutzerId()),uebung.getUebungId());
        }

        return "redirect:/termin/" + terminId ;
    }
    @Secured("ROLE_STUDENT")
    @PostMapping("/termin/{terminId}/verlassen")
    public String terminVerlassen(final @AuthenticationPrincipal OAuth2User principal, final Model model, @PathVariable("terminId") final long terminId) {
        if(principal == null || principal.getAttribute("login")==null) {
            return "redirect:/termin/" + terminId;
        }
        model.addAttribute("user",
                principal.getAttribute("login")
        );

        final Optional<Uebung> uebung = uebungsService.findUebungByTerminId(terminId);
        final Long uebungId = uebung.get().getUebungId();
        final Long benutzerId =  benutzerService.findBenutzerByGithubId(Long.parseLong(principal.getName())).get().getBenutzerId();

        gruppenService.removeGruppenMitglied(uebung.get(), terminId, benutzerId);
        uebungsService.save(uebung.get());

        return "redirect:/student/" + uebungId;
    }
}
