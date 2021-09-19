package propraganda.praktikum.logic.services.uebung;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import propraganda.praktikum.logic.aggregate.uebung.*;
import propraganda.praktikum.persistence.aggregate.uebung.UebungsRepoPostgres;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class UebungsServiceTest {

    @Mock
    private transient UebungsRepoPostgres uebungsRepoPostgres;

    @InjectMocks
    private transient UebungsService uebungsService;

    @BeforeEach
    void initRepos(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Loesche existierende Uebung")
    void loescheUebungExistiert() {
        final Zeitraum zeitraum = new Zeitraum(LocalDateTime.MIN, LocalDateTime.MAX);
        final Uebung uebung = new Uebung(1,zeitraum,zeitraum, AnmeldeTyp.GRUPPE);

        given(uebungsRepoPostgres.findByUebungId(1L)).willReturn(Optional.of(uebung));

        uebungsService.loescheUebung(1L);

        verify(uebungsRepoPostgres, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Loesche nicht existierende Uebung")
    void loescheUebungExistiertNicht() {
        assertThrows(IllegalArgumentException.class, () ->  uebungsService.loescheUebung(1L));
    }

    @Test
    @DisplayName("Editiere Uebung mit neuem TerminSet")
    void editUebungNeuesTerminSet() {
        final Uebung uebung = mock(Uebung.class);

        Termin termin = Termin.builder().datum(LocalDateTime.now()).build();
        Set<Termin> terminSet = Set.of(termin);

        uebungsService.editUebung(terminSet,1, uebung, null, null, null);

        verify(uebung, times(1)).setTermin(terminSet);
    }

    @Test
    @DisplayName("Editiere Uebung mit neuem AnmeldeTyp")
    void editUebungNeuerAnmeldeTyp() {
        final Uebung uebung = mock(Uebung.class);

        uebungsService.editUebung(null,1, uebung, AnmeldeTyp.GRUPPE, null, null);

        verify(uebung, times(1)).setAnmeldeTyp(AnmeldeTyp.GRUPPE);
    }

    @Test
    @DisplayName("Editiere Uebung mit neuem AnmeldeZeitraum")
    void editUebungNeuerAnmeldeZeitraum() {
        final Uebung uebung = mock(Uebung.class);

        final Zeitraum neuerAnmeldeZeitraum = new Zeitraum(LocalDateTime.MIN, LocalDateTime.MAX);

        uebungsService.editUebung(null,1, uebung, null, neuerAnmeldeZeitraum, null);

        verify(uebung, times(1)).setAnmeldezeitraum(neuerAnmeldeZeitraum);
    }

    @Test
    @DisplayName("Editiere Uebung mit neuem UebungsZeitraum")
    void editUebungNeuerUebungsZeitraum() {
        final Uebung uebung = mock(Uebung.class);

        final Zeitraum neuerUebungsZeitraum = new Zeitraum(LocalDateTime.MIN, LocalDateTime.MAX);

        uebungsService.editUebung(null,1, uebung, null, null, neuerUebungsZeitraum);

        verify(uebung, times(1)).setUebungszeitraum(neuerUebungsZeitraum);
    }

    @Test
    @DisplayName("Suche nicht existierende Uebung")
    void getUebungExistiert() {
        assertThrows(IllegalStateException.class, () -> uebungsService.getUebung(7L));
    }

    @Test
    @DisplayName("Suche nicht existierende Uebung")
    void getUebungExistiertNciht() { // reference
        final Zeitraum zeitraum = new Zeitraum(LocalDateTime.MIN, LocalDateTime.MAX);
        final Uebung uebung = new Uebung(1,zeitraum,zeitraum, AnmeldeTyp.GRUPPE);
        uebung.setUebungId(7L);

        given(uebungsRepoPostgres.findByUebungId(7L)).willReturn(Optional.of(uebung));

        assertThat(uebungsService.getUebung(7L)).isEqualTo(uebung);
    }

    @Test
    @DisplayName("Suche existierenden Zeitraum")
    void findZeitslotExistiert() {
        final Zeitraum zeitraum = new Zeitraum(LocalDateTime.MIN, LocalDateTime.MAX);
        final Uebung uebung = new Uebung(1,zeitraum,zeitraum, AnmeldeTyp.GRUPPE);
        final Zeitslot zeitslot = new Zeitslot(LocalDateTime.MIN,7);
        zeitslot.setZeitslotId(1L);

        uebung.setZeitslots(Set.of(zeitslot));

        assertThat(uebungsService.findZeitslotById(uebung,1L)).isEqualTo(zeitslot);
    }

    @Test
    @DisplayName("Suche nicht existierenden Zeitraum")
    void findZeitslotExistiertNicht() {
        final Zeitraum zeitraum = new Zeitraum(LocalDateTime.MIN, LocalDateTime.MAX);
        final Uebung uebung = new Uebung(1,zeitraum,zeitraum, AnmeldeTyp.GRUPPE);
        final Zeitslot zeitslot = new Zeitslot(LocalDateTime.MIN,7);
        zeitslot.setZeitslotId(7L);

        uebung.setZeitslots(Set.of(zeitslot));

        assertThat(uebungsService.findZeitslotById(uebung,1L)).isEqualTo(null);
    }
}