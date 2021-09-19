package propraganda.praktikum.logic.services.sync;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import propraganda.praktikum.logic.aggregate.uebung.Termin;
import propraganda.praktikum.logic.aggregate.uebung.Zeitslot;
import propraganda.praktikum.persistence.aggregate.uebung.UebungsRepoPostgres;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class ZuordnungsServiceTest {

    //Tutor Zuordnung wird nicht getestet, da es aus den anderen Methoden besteht

    @InjectMocks
    private transient ZuordnungsService zuordnungsService;

    @BeforeEach
    void init() throws IOException {
        MockitoAnnotations.openMocks(this);

    }
    @Test
    @DisplayName("Gewicht für null Liste")
    void getGewichtListNull() {
        assertThat(zuordnungsService.getGewicht(null, 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Gewicht für null Termine")
    void getGewichtTerminNull() {
        final List<Termin> terminList = new ArrayList<>();
        terminList.add(null);
        assertThat(zuordnungsService.getGewicht(terminList, 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Gewicht für Termine")
    void getGewicht() {
        final List<Termin> terminList = new ArrayList<>();
        final Termin termin = Termin.builder().datum(LocalDateTime.now().plusHours(26)).terminId(1).belegt(true).build();
        termin.setMitglieder(Set.of(1L));
        termin.setTutor(1L);
        terminList.add(termin);
        assertThat(zuordnungsService.getGewicht(terminList, 1L)).isEqualTo(1);
    }

    @Test
    @DisplayName("Boolean ob ein Tutor bereits in einem Termin für den Zeitslot ist")
    void getIsInTerminZeitslot() {
        final List<Termin> terminList = new ArrayList<>();
        final LocalDateTime localDateTime = LocalDateTime.now();
        final Termin termin = Termin.builder().datum(localDateTime).terminId(1).belegt(true).build();
        termin.setMitglieder(Set.of(1L));
        termin.setTutor(1L);
        terminList.add(termin);
        assertThat(zuordnungsService.isInTerminZeitslot(1L, terminList, localDateTime)).isEqualTo(true);
    }
    @Test
    @DisplayName("Suche passende Termine kein Termin vorhanden")
    void getPassendeTermineEmpty() {
        final List<Termin> terminList = new ArrayList<>();
        final Termin termin = Termin.builder().datum(LocalDateTime.now().plusHours(26)).terminId(1).belegt(true).build();
        termin.setMitglieder(Set.of(1L));
        termin.setTutor(1L);
        terminList.add(termin);
        assertThat(zuordnungsService.suchePassendeTermine(LocalDateTime.now(), terminList)).isEmpty();
    }

    @Test
    @DisplayName("Suche passende Termine ein Termin vorhanden")
    void getPassendeTermineNotEmpty() {
        final List<Termin> terminList = new ArrayList<>();
        final LocalDateTime localDateTime = LocalDateTime.now();
        final Termin termin = Termin.builder().datum(localDateTime).terminId(1).belegt(true).build();
        termin.setMitglieder(Set.of(1L));
        termin.setBelegt(true);
        terminList.add(termin);
        assertThat(zuordnungsService.suchePassendeTermine(localDateTime, terminList)).contains(termin);
    }

    @Test
    @DisplayName("Füge tutor Hinzu gleiche größe")
    void fuegeTutorHinzuGleicheGroesse() {
        final List<Termin> terminList = new ArrayList<>();
        final LocalDateTime localDateTime = LocalDateTime.now();
        final Zeitslot zeitslot = new Zeitslot(localDateTime, 1);
        zeitslot.setTutorId(Arrays.asList(1L));
        final Termin termin = Termin.builder().datum(localDateTime).terminId(1).belegt(true).build();
        termin.setMitglieder(Set.of(1L));
        termin.setBelegt(true);
        terminList.add(termin);
        zuordnungsService.fuegeTutorhinzu(terminList, zeitslot, terminList);
        assertThat(terminList.get(0).getTutor()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Füge tutor Hinzu")
    void fuegeTutorHinzuNichtGleicheGroesse() {
        final List<Termin> terminList = new ArrayList<>();
        final LocalDateTime localDateTime = LocalDateTime.now();
        final Zeitslot zeitslot = new Zeitslot(localDateTime, 2);
        zeitslot.setTutorId(Arrays.asList(1L));
        final Termin termin = Termin.builder().datum(localDateTime).terminId(1).belegt(true).build();
        termin.setMitglieder(Set.of(1L));
        termin.setBelegt(true);
        terminList.add(termin);
        zuordnungsService.fuegeTutorhinzu(terminList, zeitslot, terminList);
        assertThat(terminList.get(0).getTutor()).isEqualTo(1L);
    }


}