package propraganda.praktikum.logic.services.uebung;

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

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class TerminServiceTest {
    @Mock
    private transient UebungsRepoPostgres uebungsRepoPostgres;


    @InjectMocks
    private transient TerminService terminService;

    @BeforeEach
    void initRepos(){
        MockitoAnnotations.openMocks(this);
    }


    @Test
    @DisplayName("Create Template erste uebung wird erstellt")
    void createTemplateKeinTemplateVorhanden() {

        given(uebungsRepoPostgres.count()).willReturn(0L);

        verify(uebungsRepoPostgres, times(0)).save(any());
    }


    @Test
    @DisplayName("Create Template erste uebung wird richtiger Tag")
    void createTemplate() {

        final Uebung alteUebung = mock(Uebung.class);

        final Uebung neueUebung = new Uebung(5,new Zeitraum(LocalDateTime.MIN, LocalDateTime.MIN), new Zeitraum(LocalDateTime.MIN.plusDays(7), LocalDateTime.MIN.plusDays(14)), AnmeldeTyp.INDIVIDUAL);

        final Termin termin1 = Termin.builder().datum(LocalDateTime.MIN.plusDays(1)).terminId(1).build();
        final Termin termin2 = Termin.builder().datum(LocalDateTime.MIN.plusDays(3)).terminId(2).build();
        final Termin termin3 = Termin.builder().datum(LocalDateTime.MIN.plusDays(5)).terminId(3).build();



        final Termin zuPruefen1 = Termin.builder().datum(LocalDateTime.MIN.plusDays(8)).terminId(1).build();
        final Termin zuPruefen2 = Termin.builder().datum(LocalDateTime.MIN.plusDays(10)).terminId(2).build();
        final Termin zuPruefen3 = Termin.builder().datum(LocalDateTime.MIN.plusDays(12)).terminId(3).build();

        final Set<Termin> zuPruefenSet = new HashSet<>(Set.of(zuPruefen1, zuPruefen2, zuPruefen3));

        when(alteUebung.getTermin()).thenReturn(Set.of(termin1,termin2,termin3));
        when(alteUebung.getUebungszeitraum()).thenReturn(new Zeitraum(LocalDateTime.MIN, LocalDateTime.MIN.plusDays(7)));


        neueUebung.setTermin(zuPruefenSet);
        given(uebungsRepoPostgres.count()).willReturn(1L);



        terminService.createTemplate(neueUebung, alteUebung);


        verify(uebungsRepoPostgres, atLeast(1)).save(neueUebung);
    }

    @Test
    @DisplayName("Id zu terminen")
    void idZuTerminTest() {
        final Uebung neueUebung = new Uebung(5,new Zeitraum(LocalDateTime.MIN, LocalDateTime.MIN), new Zeitraum(LocalDateTime.MIN.plusDays(7), LocalDateTime.MIN.plusDays(14)), AnmeldeTyp.INDIVIDUAL);

        final Termin termin1 = Termin.builder().datum(LocalDateTime.MIN.plusDays(1)).terminId(1).build();
        neueUebung.setTermin(Set.of(termin1));
        termin1.setTerminId(1);

        assertThat(terminService.idZuTermin(neueUebung, 1)).get().isEqualTo(termin1);

    }

    @Test
    @DisplayName("setTerminTutor test")
    void setTerminTutorTest(){
        final Uebung neueUebung = new Uebung(5,new Zeitraum(LocalDateTime.MIN, LocalDateTime.MIN), new Zeitraum(LocalDateTime.MIN.plusDays(7), LocalDateTime.MIN.plusDays(14)), AnmeldeTyp.INDIVIDUAL);
        final Termin termin1 = Termin.builder().datum(LocalDateTime.MIN.plusDays(1)).terminId(1).build();
        neueUebung.setTermin(Set.of(termin1));
        termin1.setTerminId(1);
        final Benutzer tutor = new Benutzer(111, "karlderTutor", BenutzerTyp.TUTOR);
        tutor.setBenutzerId(1);

        terminService.setTerminTutor(neueUebung, termin1.getTerminId(), tutor.getBenutzerId());

        assertThat(termin1.getTutor()).isEqualTo(tutor.getBenutzerId());
    }

    @Test
    @DisplayName("getTerminTutor test")
    void getTerminTutorTest(){
        final Uebung neueUebung = new Uebung(5,new Zeitraum(LocalDateTime.MIN, LocalDateTime.MIN), new Zeitraum(LocalDateTime.MIN.plusDays(7), LocalDateTime.MIN.plusDays(14)), AnmeldeTyp.INDIVIDUAL);
        final Termin termin1 = Termin.builder().datum(LocalDateTime.MIN.plusDays(1)).terminId(1).build();
        neueUebung.setTermin(Set.of(termin1));
        termin1.setTerminId(1);
        final Benutzer tutor = new Benutzer(111, "karlderTutor", BenutzerTyp.TUTOR);
        tutor.setBenutzerId(1);

        termin1.setTutor(1L);

        assertThat(terminService.getTerminTutor(neueUebung, termin1.getTerminId())).get().isEqualTo(tutor.getBenutzerId());
    }

}