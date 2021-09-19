package propraganda.praktikum.logic.services.sync;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import propraganda.praktikum.logic.aggregate.benutzer.Benutzer;
import propraganda.praktikum.logic.aggregate.benutzer.BenutzerTyp;
import propraganda.praktikum.logic.aggregate.uebung.AnmeldeTyp;
import propraganda.praktikum.logic.aggregate.uebung.Termin;
import propraganda.praktikum.logic.aggregate.uebung.Uebung;
import propraganda.praktikum.logic.aggregate.uebung.Zeitraum;
import propraganda.praktikum.persistence.aggregate.uebung.UebungsRepoPostgres;
import propraganda.praktikum.logic.services.benutzer.BenutzerService;
import propraganda.praktikum.logic.services.github.GithubService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class GithubRepoServiceTest {

    @Mock
    transient UebungsRepoPostgres uebungsRepo;

    @Mock
    transient GithubService githubService;

    @Mock
    transient BenutzerService benutzerService;

    @InjectMocks
    transient GithubRepoService githubRepoService;

    @BeforeEach
    void init () {
        MockitoAnnotations.openMocks(this);

    }

    @Test
    @DisplayName("Github GruppenRepos erstellen")
    void createGruppenRepos() {
        final Zeitraum anmeldeZeitraum = new Zeitraum(LocalDateTime.now().minusHours(36),LocalDateTime.now().minusHours(25));
        final Zeitraum uebungszeitraum = new Zeitraum(LocalDateTime.now().plusHours(25),LocalDateTime.now().plusHours(45));
        final Uebung uebung = new Uebung(1,anmeldeZeitraum,uebungszeitraum, AnmeldeTyp.GRUPPE);
        final Termin termin = Termin.builder().datum(LocalDateTime.now().plusHours(26)).terminId(1).belegt(true).build();

        final String benutzername = "Daniel";
        final Benutzer benutzer = new Benutzer(56464,benutzername, BenutzerTyp.STUDENT);//NOPMD
        termin.setMitglieder(Set.of(1L));
        termin.setGruppenName("lerhnjgng");
        uebung.setTermin(Set.of(termin));

        final String reponame = "Uebung-" + uebung.getUebungId() + "-" +termin.getGruppenName() + "-" + termin.getDatum().toLocalDate().toString();

        given(benutzerService.findByBenutzerId(1L)).willReturn(java.util.Optional.of(benutzer));
        given(uebungsRepo.findAll()).willReturn(List.of(uebung));


        githubRepoService.createGroupRepos();

        verify(githubService, times(1)).createRepo(anyString());
        verify(githubService, times(1)).addUserToRepo(reponame, benutzername);
    }
}
