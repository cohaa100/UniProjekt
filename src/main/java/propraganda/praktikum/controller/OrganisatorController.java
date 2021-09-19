package propraganda.praktikum.controller;

import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import propraganda.praktikum.logic.aggregate.benutzer.Benutzer;
import propraganda.praktikum.logic.aggregate.benutzer.BenutzerTyp;
import propraganda.praktikum.logic.aggregate.uebung.Termin;
import propraganda.praktikum.logic.aggregate.uebung.AnmeldeTyp;
import propraganda.praktikum.logic.aggregate.uebung.Uebung;
import propraganda.praktikum.logic.aggregate.uebung.Zeitraum;
import propraganda.praktikum.logic.services.benutzer.BenutzerService;
import propraganda.praktikum.logic.services.github.GithubService;
import propraganda.praktikum.logic.services.uebung.GruppenService;
import propraganda.praktikum.logic.services.uebung.TerminService;
import propraganda.praktikum.logic.services.uebung.UebungsService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;


@Controller
@Slf4j
@Secured("ROLE_ORGANISATOR")
public class OrganisatorController {
    @Autowired
    private transient TerminService terminService;
    @Autowired
    private transient GruppenService gruppenService;
    @Autowired
    private transient UebungsService uebungsService;
    @Autowired
    private transient BenutzerService benutzerService;
    @Autowired
    private transient GithubService githubService;

    @GetMapping("/organisator/add")
    public String test(final @AuthenticationPrincipal OAuth2User principal, final Model model) {
        model.addAttribute("user",
                principal != null ?
                        principal.getAttribute("login") : null
        );

        final List<Termin> terminList = new ArrayList<>(100);

        model.addAttribute("uebung", UebungsService.createDummy());
        model.addAttribute("terminList", terminList);

        return "benutzer/orgaCreateUebung";
    }


    @PostMapping("/organisator/add")
    public String uebungerstellenPost(final @AuthenticationPrincipal OAuth2User principal, final Model model,
                                      final @RequestParam int gruppenGroesse,
                                      final @RequestParam(name = "anmeldezeitraum.start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime anmeldeZeitraumStart,
                                      final @RequestParam(name = "anmeldezeitraum.ende") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime anmeldeZeitraumEnde,
                                      final @RequestParam(name = "uebungszeitraum.start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime uebungsZeitraumStart,
                                      final @RequestParam(name = "uebungszeitraum.ende") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime uebungsZeitraumEnde,
                                      final @RequestParam AnmeldeTyp anmeldeTyp
                       ){
        model.addAttribute("user",
                principal != null ?
                        principal.getAttribute("login") : null
        );

        final Uebung uebung = new Uebung(gruppenGroesse,new Zeitraum(anmeldeZeitraumStart, anmeldeZeitraumEnde),
                new Zeitraum(uebungsZeitraumStart, uebungsZeitraumEnde), anmeldeTyp);

        try{
            final Optional<Uebung> uebungAlt = uebungsService.getLatestUebung();
            uebungsService.erstelleUebung(uebung);
           if(uebungAlt.isPresent()) {
               terminService.createTemplate(uebung, uebungAlt.get());
           }

        }catch(IllegalStateException stateException){
            log.error(stateException.getMessage());
        }

        return "redirect:/uebungen";

    }


    @GetMapping("/organisator/{uebungId}")
    public String terminUebersicht(final @AuthenticationPrincipal OAuth2User principal, final Model model, final @PathVariable("uebungId") long uebungsId) {
        model.addAttribute("user",
                principal != null ?
                        principal.getAttribute("login") : null
        );

        try {
            final Uebung uebung = uebungsService.getUebung(uebungsId);
            final Set<Termin> terminList = uebung.getTermin();
            model.addAttribute("terminList", terminList);
            model.addAttribute("uebungId", uebungsId);
            model.addAttribute("uebung", uebung);
        } catch (IllegalStateException illegalStateException) {
            log.error(illegalStateException.getMessage());
        }
        return "benutzer/organisator";
    }
    @GetMapping("/organisator/{uebungId}/edit")
    public String editUebung(final @AuthenticationPrincipal OAuth2User principal, final Model model,final @PathVariable("uebungId") Long uebungsId) {
        model.addAttribute("user",
                principal != null ?
                        principal.getAttribute("login") : null
        );

        try {
            final Uebung uebung = uebungsService.getUebung(uebungsId);
            model.addAttribute("uebung", uebung);
            //Changed for PMD to Exception (avoid NPE)
        } catch (Exception exception) {
            log.error("Nullpointer");
        }

        return  "benutzer/orgaEdit";
    }

    @PostMapping("/organisator/{uebungId}/edit")
    //Verschiebe bestehende daten
    public String editTerminUebersicht(final @AuthenticationPrincipal OAuth2User principal, final Model model,
                                       final @RequestParam int gruppenGroesse,
                                       final @RequestParam(name = "anmeldezeitraum.zeitraumStart") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime anmeldeZeitraumStart,
                                       final @RequestParam(name = "anmeldezeitraum.zeitraumEnde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime anmeldeZeitraumEnde,
                                       final @RequestParam(name = "uebungszeitraum.zeitraumStart") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime uebungsZeitraumStart,
                                       final @RequestParam(name = "uebungszeitraum.zeitraumEnde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime uebungsZeitraumEnde,
                                       final @RequestParam AnmeldeTyp anmeldeTyp,
                                       final @PathVariable("uebungId") Long uebungsId) {

        model.addAttribute("user",
                principal != null ?
                        principal.getAttribute("login") : null
        );

        final Uebung uebung = uebungsService.getUebung(uebungsId);
        final Set<Termin> termine = uebung.getTermin();
        log.info(termine.toString());

        final Set<Termin> neueTermine = uebungsService.termineAnpassen(new Zeitraum(uebungsZeitraumStart,uebungsZeitraumEnde),uebung);

        uebungsService.editUebung(neueTermine, gruppenGroesse, uebung, anmeldeTyp,new Zeitraum(anmeldeZeitraumStart, anmeldeZeitraumEnde),
                new Zeitraum(uebungsZeitraumStart, uebungsZeitraumEnde));


        return  "redirect:/uebungen";
    }

    @GetMapping("/organisator/{uebungId}/remove")
    public String loescheUebung(final @AuthenticationPrincipal OAuth2User principal, final Model model,final @PathVariable("uebungId") long uebungId) {
        model.addAttribute("user",
                principal != null ?
                        principal.getAttribute("login") : null
        );

        try {
            uebungsService.loescheUebung(uebungId);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("Die Uebung mit der ID {} konnt nicht geloescht werden", uebungId);
        }

        return "redirect:/uebungen";
    }


    @GetMapping("/organisator/{uebungId}/{terminId}")
    public String gruppenUebersicht(final @AuthenticationPrincipal OAuth2User principal, final Model model,final @PathVariable("uebungId") Long uebungsId,final @PathVariable("terminId") Long terminId) {
        model.addAttribute("user",
                principal != null ?
                        principal.getAttribute("login") : null
        );

        try {
            final Optional<Uebung> uebung = uebungsService.findUebungByTerminId(terminId);

            final Termin termin = terminService.idZuTermin(uebung.get(),terminId).get();
            model.addAttribute("termin", termin);
        } catch (IllegalStateException illegalStateException) {
            log.error(illegalStateException.getMessage());
        }
        return "organisator";
    }

    @GetMapping("/organisator/{id}/terminerstellen")
    public String terminerstellenGet(final @AuthenticationPrincipal OAuth2User principal, final Model model,final @PathVariable("id") long uebungId) {
        model.addAttribute("user",
                principal != null ?
                        principal.getAttribute("login") : null
        );



        final Termin termin = Termin.builder().datum(LocalDateTime.now()).build();


        model.addAttribute("uebungId", uebungId);
        model.addAttribute("termin", termin);

        return "termin/terminErstellen";
    }

    @PostMapping("/organisator/{id}/terminerstellen")
    public String terminerstellenPost(final @AuthenticationPrincipal OAuth2User principal,
                                      final @RequestParam(name = "datum") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime datum,
                                      final Model model,final @PathVariable("id") long uebungId) {
        model.addAttribute("user",
                principal != null ?
                        principal.getAttribute("login") : null
        );

        final Uebung uebung = uebungsService.getUebung(uebungId);
        if(datum.isBefore(uebung.getUebungszeitraum().getZeitraumStart()) || datum.isAfter(uebung.getUebungszeitraum().getZeitraumEnde())) {
            throw new IllegalArgumentException("Termin nicht im Uebungszeitraum");
        }
        final Termin termin = Termin.builder().datum(datum).build();
        //log.info(""+termin.getTerminId());


        terminService.saveTermin(uebung, termin);

        return "redirect:/organisator/" + uebungId;
    }

    @PostMapping("/organisator/{terminId}/entfernen")
    public String loescheTermin(final @AuthenticationPrincipal OAuth2User principal, final Model model,final @PathVariable("terminId") long terminId) {
        model.addAttribute("user",
                principal != null ?
                        principal.getAttribute("login") : null
        );

        try {
            final Optional<Uebung> uebung = uebungsService.findUebungByTerminId(terminId);
            final Optional<Termin> termin = terminService.idZuTermin(uebung.get(), terminId);
            terminService.removeTermin(uebung.get(), termin.get());
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("Der Termin {} ist nicht vorhanden", terminId);
        }

        return "redirect:/uebungen";
    }


    @GetMapping("/organisator/{terminId}/gruppenedit")
    public String gruppenBearbeitenGet(final @AuthenticationPrincipal OAuth2User principal, final Model model,final @PathVariable("terminId") long terminId){
        model.addAttribute("user",
                principal != null ?
                        principal.getAttribute("login") : null
        );

        final Optional<Uebung> uebung = uebungsService.findUebungByTerminId(terminId);
        final Optional<Termin> termin = terminService.idZuTermin(uebung.get(), terminId);
        model.addAttribute("termin", termin.get());

        return "benutzer/orgaGruppeEdit";
    }

    @PostMapping("/organisator/{terminId}/gruppenedit")
    public String gruppenBearbeitenPost(final @AuthenticationPrincipal OAuth2User principal, final Model model,
                                        final @RequestParam String gruppenName,
                                        final @RequestParam Set<String> gruppenMitgliederNameSet,
                                        final @PathVariable("terminId") long terminId){
        model.addAttribute("user",
                principal != null ?
                        principal.getAttribute("login") : null
        );

        final Set<Benutzer> benutzerSet = benutzerService.findBenutzerByName(gruppenMitgliederNameSet);
        final Set<Long> benutzerIdSet = benutzerService.changeBenutzerToLong(benutzerSet);
        final Optional<Uebung> uebung = uebungsService.findUebungByTerminId(terminId);
        final Optional<Termin> termin = terminService.idZuTermin(uebung.get(), terminId);

        if(termin.get().isBelegt()) {
            gruppenService.editGruppen(terminId,benutzerIdSet,gruppenName);
        } else {
            gruppenService.erstelleGruppeOrga(terminId,gruppenName,benutzerIdSet,uebung.get().getUebungId());
        }


        return "redirect:/termin/" + terminId;
    }


    @GetMapping("/organisator/{terminId}/tutorzuweisen")
    public String tutorZuweisenGet(final @AuthenticationPrincipal OAuth2User principal, final Model model,final @PathVariable("terminId") long terminId){
        model.addAttribute("user",
                principal != null ?
                        principal.getAttribute("login") : null
        );

        final Optional<Uebung> uebung = uebungsService.findUebungByTerminId(terminId);
        final Optional<Termin> termin = terminService.idZuTermin(uebung.get(), terminId);

        model.addAttribute("termin", termin.get());
        model.addAttribute("tutorName", "");

        return "benutzer/orgaTutorZuweisen";
    }

    @PostMapping("/organisator/{terminId}/tutorzuweisen")
    public String tutorZuweisenPost(final @RequestParam(name = "tutorName") String tutorName, final Model model,final @PathVariable("terminId") long terminId) throws IOException {

        final GHUser tutor = githubService.getGitHubUserByNameInOrga(tutorName);
        final Optional<Benutzer> benutzer = benutzerService.findBenutzerByGithubId(tutor.getId());

        if(benutzer.isEmpty()){
            throw new UsernameNotFoundException("Der Benutzer ist nicht vorhanden");
        }

        if(benutzer.get().getBenutzerTyp() != BenutzerTyp.TUTOR){
            throw new IllegalArgumentException("Kein Tutor");
        }

        final Optional<Uebung> uebung = uebungsService.findUebungByTerminId(terminId);
        terminService.setTerminTutor(uebung.get(), terminId,benutzer.get().getBenutzerId());
        //termin.setTutor(benutzer.get().getBenutzerId());

        uebungsService.save(uebung.get());

        return "redirect:/termin/" + terminId;
    }

}
