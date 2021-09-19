package propraganda.praktikum.logic.services.uebung;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import propraganda.praktikum.logic.aggregate.benutzer.Benutzer;
import propraganda.praktikum.logic.aggregate.benutzer.BenutzerTyp;
import propraganda.praktikum.logic.aggregate.uebung.*;
import propraganda.praktikum.persistence.aggregate.benutzer.BenutzerRepoPostgres;
import propraganda.praktikum.persistence.aggregate.uebung.UebungsRepoPostgres;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class GruppenServiceTest {

    @Mock
    private transient BenutzerRepoPostgres benutzerRepo;
    @Mock
    private transient UebungsRepoPostgres uebungRepo;
    @Mock
    private transient TerminService terminService;
    @InjectMocks
    private transient GruppenService gruppenService;


    @BeforeEach
    void initRepos(){
        MockitoAnnotations.openMocks(this);

        final LocalDateTime vergangenheit = LocalDateTime.of(2020,1,1,1,1);
        final LocalDateTime zukunft = LocalDateTime.of(2020,2,1,1,1);
        final Termin termin = Termin.builder().datum(vergangenheit).build();
        termin.setTerminId(1);
        final Termin termin2 = Termin.builder().datum(zukunft).build();
        termin2.setTerminId(2);


        final Uebung ersteUebung = new Uebung(4, new Zeitraum(vergangenheit, vergangenheit.plusDays(7)), new Zeitraum(vergangenheit.plusDays(7), vergangenheit.plusDays(14)), AnmeldeTyp.GRUPPE);
        ersteUebung.setTermin(Collections.singleton(termin));
        ersteUebung.setUebungId(1L);

        final Uebung zweiteUebungGroesse0 = new Uebung(0, new Zeitraum(vergangenheit, vergangenheit.plusDays(7)), new Zeitraum(vergangenheit.plusDays(7), vergangenheit.plusDays(14)), AnmeldeTyp.GRUPPE);
        zweiteUebungGroesse0.setTermin(Collections.singleton(termin2));
        zweiteUebungGroesse0.setUebungId(2L);

        final Benutzer benutzer = new Benutzer(1L, "test", BenutzerTyp.STUDENT);

        given(uebungRepo.findByUebungId(1L)).willReturn(Optional.of(ersteUebung));
        given(uebungRepo.findByUebungId(2L)).willReturn(Optional.of(zweiteUebungGroesse0));

        given(benutzerRepo.findByBenutzerId(1L)).willReturn(Optional.of(benutzer));


        //given(terminRepo.find(2L)).willReturn(termin2);
    }


    @Test
    @DisplayName("Termin ist nicht vorhanden")
    void editGruppenTerminIstUngueltig() {

        assertThrows(IllegalArgumentException.class, () -> {
            gruppenService.editGruppen(-1L,null,null);
        });
    }

    @Test
    @DisplayName("Tutor ist nicht Vorhanden")
    void editGruppenTutorExestiertNicht() {
        final Termin termin = Termin.builder()
                .datum(LocalDateTime.of(2020,1,1,1,1))
                .terminId(1L)
                .tutor(-1L).build();

        final Uebung uebung = uebungRepo.findByUebungId(1L).get();


        given(terminService.idZuTermin(uebung, 1L)).willReturn(Optional.of(termin));
        given(terminService.isTerminIdInUebung(any(), anyLong())).willReturn(true);



        gruppenService.editGruppen(uebung, 1L,-1L,null,null);


        verify(terminService, times(0)).setTerminTutor(any(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("Tutor ist kein Tutor")
    void editGruppenTutoristKeinTutor() {
        final Termin termin = Termin.builder()
                .datum(LocalDateTime.of(2020,1,1,1,1))
                .terminId(1L)
                .build();

        final Uebung uebung = uebungRepo.findByUebungId(1L).get();
        final Benutzer student = new Benutzer(1L, "test", BenutzerTyp.STUDENT);


        given(terminService.idZuTermin(uebung, 1L)).willReturn(Optional.of(termin));
        given(terminService.isTerminIdInUebung(any(), anyLong())).willReturn(true);
        given(benutzerRepo.findByBenutzerId(1L)).willReturn(Optional.of(student));


        gruppenService.editGruppen(uebung, 1L,1L,null,null);

        verify(terminService, times(0)).setTerminTutor(any(),anyLong(),anyLong());
    }

    @Test
    @DisplayName("Studenten sind Ungueltig")
    void editGruppenStudentistNull() {
        final Termin termin = Termin.builder()
                .datum(LocalDateTime.of(2020,1,1,1,1))
                .terminId(1L)
                .tutor(0L).build();
        termin.setMitglieder(Set.of(0L,1L));

        final Benutzer student = new Benutzer(1L, "test", BenutzerTyp.STUDENT);

        final Uebung uebung = uebungRepo.findByUebungId(1L).get();

        given(terminService.idZuTermin(uebung, 1L)).willReturn(Optional.of(termin));
        given(terminService.isTerminIdInUebung(any(), anyLong())).willReturn(true);
        given(benutzerRepo.findByBenutzerId(1L)).willReturn(Optional.of(student));

        gruppenService.editGruppen(uebung, 1L,0L,Set.of(1L, -1L),null);

        verify(terminService, times(1)).saveTermin(uebung, termin);
    }

    @Test
    @DisplayName("Gruppe wird editiert")
    void editGruppen() {
        final Termin termin = Termin.builder()
                .datum(LocalDateTime.of(2020,1,1,1,1))
                .terminId(1L)
                .tutor(0L).build();

        final Termin zuPrufen = Termin.builder()
                .datum(LocalDateTime.of(2020,1,1,1,1))
                .terminId(1L)
                .tutor(0L).build();


        zuPrufen.setMitglieder(Set.of(2L, 3L));
        zuPrufen.setGruppenName("test");
        final Uebung uebung = uebungRepo.findByUebungId(1L).get();

        final Benutzer student = new Benutzer(20L, "test", BenutzerTyp.TUTOR);


        given(terminService.idZuTermin(uebung, 1L)).willReturn(Optional.of(termin));
        given(terminService.isTerminIdInUebung(any(), anyLong())).willReturn(true);
        given(benutzerRepo.findByBenutzerId(1L)).willReturn(Optional.of(student));
        given(benutzerRepo.findByBenutzerId(2L)).willReturn(Optional.ofNullable(mock(Benutzer.class)));
        given(benutzerRepo.findByBenutzerId(3L)).willReturn(Optional.ofNullable(mock(Benutzer.class)));

        gruppenService.editGruppen(uebung, 1L,1L,Set.of(2L, 3L),"test");


        verify(terminService, times(1)).saveTermin(uebung, zuPrufen);
    }

    @Test
    @DisplayName("Name ist Null")
    void editGruppenNameistNull() {
        final Termin termin = Termin.builder()
                .datum(LocalDateTime.of(2020,1,1,1,1))
                .terminId(1L)
                .tutor(0L)
                .build();
        termin.setGruppenName("test");

        final Termin zuPrufen = Termin.builder()
                .datum(LocalDateTime.of(2020,1,1,1,1))
                .terminId(1L)
                .tutor(0L).build();

        termin.setGruppenName("test");
        final Uebung uebung = uebungRepo.findByUebungId(1L).get();


        given(terminService.idZuTermin(uebung, 1L)).willReturn(Optional.of(termin));
        given(terminService.isTerminIdInUebung(any(), anyLong())).willReturn(true);

        gruppenService.editGruppen(uebung, 1L,0L,Set.of(1L, -1L),null);

        verify(terminService, times(1)).saveTermin(uebung, zuPrufen);
    }

    @Test
    @DisplayName("Ein Student ist nicht vorhanden (Id)")
    void editGruppenStudentIdUngultig() {
        final Termin termin = Termin.builder()
                .datum(LocalDateTime.of(2020,1,1,1,1))
                .terminId(1L)
                .tutor(0L)
                .build();
        termin.setGruppenName("test");
        termin.setMitglieder(Set.of(1L));


        final Termin zuPruefen = Termin.builder()
                .datum(LocalDateTime.of(2020,1,1,1,1))
                .terminId(1L)
                .tutor(0L)
                .build();
        zuPruefen.setGruppenName("test");
        zuPruefen.setMitglieder(Set.of(1L));

        final Uebung uebung = uebungRepo.findByUebungId(1L).get();

        given(terminService.idZuTermin(uebung, 1L)).willReturn(Optional.of(termin));
        given(terminService.isTerminIdInUebung(any(), anyLong())).willReturn(true);
        given(benutzerRepo.findByBenutzerId(2L)).willReturn(Optional.ofNullable(mock(Benutzer.class)));

        gruppenService.editGruppen(uebung, 1L,1L,Set.of(-1L, 2L),"test");

        verify(terminService, times(1)).saveTermin(uebung, zuPruefen);
    }

    @Test
    @DisplayName("Uebung ist Unguelitg")
    void erstelleGruppeOrgaUebungNichtVorhandenOrga() {
        final String name = "Egal";
        final long uebungsId = 1L;

        assertThrows(IllegalArgumentException.class, () -> gruppenService.erstelleGruppeOrga(1L, name, null, uebungsId));
    }

    @Test
    @DisplayName("Mitglieder sind null")
    void erstelleGruppeMitgliederNullOrga() {
        final String name = "Egal";
        final long uebungsId = 1L;

        given(uebungRepo.findUebungByTerminId(anyLong())).willReturn(Optional.ofNullable(mock(Uebung.class)));
        given(terminService.idZuTermin(any(),anyLong())).willReturn(Optional.ofNullable(mock(Termin.class)));
        assertThrows(IllegalArgumentException.class, () -> gruppenService.erstelleGruppeOrga(1L, name, null, uebungsId));
    }

    @Test
    @DisplayName("Termin ist ungueltig")
    void erstelleGruppeTerminNullOrga() {
        final String name = "Egal";
        final long uebungsId = 1L;

        given(uebungRepo.findUebungByTerminId(anyLong())).willReturn(Optional.ofNullable(mock(Uebung.class)));
        assertThrows(IllegalArgumentException.class, () -> gruppenService.erstelleGruppeOrga(-1L, name, Set.of(), uebungsId));
    }

    @Test
    @DisplayName("Uebung sind ungueltig")
    void erstelleGruppeUebungNullOrga() {
        final String name = "Egal";
        final long uebungsId = -1L;

        assertThrows(IllegalArgumentException.class, () -> gruppenService.erstelleGruppeOrga(1L, name, Set.of(), uebungsId));
    }

    @Test
    @DisplayName("UserId in der Gruppe ist nicht vorhanden")
    void erstelleGruppeMitgliederNichtVorhandenOrga() {
        final String name = "Egal";
        final long uebungsId = 1L;
        final Set<Long> mitgliederId = Set.of(42L);
        given(uebungRepo.findUebungByTerminId(anyLong())).willReturn(Optional.ofNullable(mock(Uebung.class)));
        given(terminService.idZuTermin(any(),anyLong())).willReturn(Optional.ofNullable(mock(Termin.class)));
        assertThrows(IllegalStateException.class, () -> gruppenService.erstelleGruppeOrga(1L, name, mitgliederId, uebungsId));
    }

    @Test
    @DisplayName("UserId in der Gruppe vorhanden")
    void erstelleGruppeMitgliederVorhandenOrga() {
        final String name = "Egal";
        final long uebungsId = 1L;
        final Set<Long> mitgliederId = Set.of(1L);

        given(uebungRepo.findUebungByTerminId(anyLong())).willReturn(Optional.ofNullable(mock(Uebung.class)));
        given(terminService.idZuTermin(any(),anyLong())).willReturn(Optional.ofNullable(mock(Termin.class)));

        gruppenService.erstelleGruppeOrga(1L,name,mitgliederId,uebungsId);

        verify(terminService,times(1)).saveTermin(any(), any());
    }

    @Test
    @DisplayName("Gruppengröße wird überschritten")
    void erstelleGruppeMitgliederZuVielOrga() {
        final String name = "Egal";
        final long uebungsId = 2L;
        final Set<Long> mitgliederId = Set.of(1L);
        final Uebung uebung = mock(Uebung.class);

        given(uebungRepo.findUebungByTerminId(anyLong())).willReturn(Optional.ofNullable(uebung));
        when(uebung.getGruppenGroesse()).thenReturn(0);
        given(terminService.idZuTermin(any(),anyLong())).willReturn(Optional.ofNullable(mock(Termin.class)));

        gruppenService.erstelleGruppeOrga(1L,name,mitgliederId,uebungsId);

        verify(terminService,times(1)).saveTermin(any(), any());
    }

    @Test
    @DisplayName("Gruppe löschen die es nicht gibt")
    void loescheGruppeNichtVorhanden() {
        assertThrows(IllegalArgumentException.class, () ->  gruppenService.loescheGruppe(3L));
    }

    @Test
    @DisplayName("Gruppe löschen nicht belegt")
    void loescheGruppeNichtBelegt() {
        final Termin termin = Termin.builder()
                .datum(LocalDateTime.of(2020,1,1,1,1))
                .terminId(3L)
                .tutor(0L)
                .belegt(false)
                .build();



        final Uebung uebung = uebungRepo.findByUebungId(1L).get();
        given(uebungRepo.findUebungByTerminId(3L)).willReturn(Optional.of(uebung));
        given(terminService.idZuTermin(any(),anyLong())).willReturn(Optional.of(termin));
        assertThrows(IllegalStateException.class, () ->  gruppenService.loescheGruppe(3L));
    }

    @Test
    @DisplayName("Belegte Gruppe löschen")
    void loescheGruppeBelegt() {
        final Termin termin = Termin.builder()
                .datum(LocalDateTime.of(2020,1,1,1,1))
                .terminId(3L)
                .tutor(0L)
                .belegt(true)
                .build();

        final Zeitraum zeitraum = new Zeitraum(LocalDateTime.MIN,LocalDateTime.MIN);

        final Uebung uebung = new Uebung(5,zeitraum, zeitraum, AnmeldeTyp.INDIVIDUAL);
        uebung.setTermin(Collections.singleton(termin));

        given(uebungRepo.findUebungByTerminId(3L)).willReturn(Optional.of(uebung));
        given(terminService.idZuTermin(uebung, 3L)).willReturn(Optional.of(termin));

        gruppenService.loescheGruppe(3L);

        verify(terminService, times(1)).saveTermin(any(), any());
    }


    @Test
    @DisplayName("Termin nicht vorhanden")
    void mitgliedEntfernenNichtVorhanden() {
        assertThrows(IllegalArgumentException.class, () ->  gruppenService.loescheGruppe(3L));
    }

    @Test
    @DisplayName("Mitglied entfernen")
    void mitgliedEntfernen() {

        final Set<Long> mitglieder = new HashSet<>();
        mitglieder.add(1L);
        mitglieder.add(2L);

        final Termin termin = Termin.builder()
                .datum(LocalDateTime.of(2020,1,1,1,1))
                .terminId(3L)
                .tutor(0L)
                .belegt(true)
                .build();
        termin.setMitglieder(mitglieder);

        final Termin zuPruefen = Termin.builder()
                .datum(LocalDateTime.of(2020,1,1,1,1))
                .terminId(3L)
                .tutor(0L)
                .belegt(true)
                .build();
        zuPruefen.setMitglieder(Set.of(1L));

        final Zeitraum zeitraum = new Zeitraum(LocalDateTime.MIN,LocalDateTime.MIN);

        final Uebung uebung = new Uebung(5,zeitraum, zeitraum, AnmeldeTyp.INDIVIDUAL);
        uebung.setTermin(Collections.singleton(termin));

        given(uebungRepo.findUebungByTerminId(3L)).willReturn(Optional.of(uebung));
        given(terminService.idZuTermin(uebung, 3L)).willReturn(Optional.of(termin));


        gruppenService.removeGruppenMitglied(uebung, 3L, 2L);

        verify(terminService, times(1)).saveTermin(uebung, zuPruefen);
    }

    @Test
    @DisplayName("Mitglied Entfernen kein gueltige TerminId")
    void mitgliedEntfernenTerminNichtVorhanden() {

        final Uebung uebung = mock(Uebung.class);


        gruppenService.removeGruppenMitglied(uebung,3L, 1L);

        verify(terminService, times(0)).saveTermin(any(),any());

    }
//
    @Test
    @DisplayName("Benutzer aus alter Gruppe entfernen falls schon in einer Anderen")
    void mitgliederAusAltenGruppenEntfernen() {
        final Uebung uebung = mock(Uebung.class);

        final Set<Long> mitglieder = new HashSet<>();
        mitglieder.add(1L);
        mitglieder.add(2L);


        final Benutzer benutzer = new Benutzer(42L, "test", BenutzerTyp.STUDENT);

        final Termin termin = Termin.builder()
                .datum(LocalDateTime.of(2020,1,1,1,1))
                .terminId(1L)
                .belegt(true)
                .build();
        termin.setMitglieder(mitglieder);


        final Termin termin2 = Termin.builder()
                .datum(LocalDateTime.of(2020,1,1,1,1))
                .terminId(2L)
                .belegt(false)
                .build();

        final Termin zuPruefen = Termin.builder()
                .datum(LocalDateTime.of(2020,1,1,1,1))
                .terminId(1L)
                .belegt(true)
                .build();

        zuPruefen.setMitglieder(Set.of());
        zuPruefen.setGruppenName("");


        final Termin zuPruefen2 = Termin.builder()
                .datum(LocalDateTime.of(2020,1,1,1,1))
                .terminId(2L)
                .build();
        zuPruefen2.setGruppenName("test");
        zuPruefen2.setBelegt(true);
        zuPruefen2.setMitglieder(Set.of(1L,2L));

        when(uebung.getTermin()).thenReturn(Set.of(termin, termin2));
        given(benutzerRepo.findByBenutzerId(2L)).willReturn(Optional.of(benutzer));
        given(uebungRepo.findUebungByTerminId(1L)).willReturn(Optional.ofNullable(uebung));
        given(uebungRepo.findUebungByTerminId(2L)).willReturn(Optional.ofNullable(uebung));

        given(terminService.idZuTermin(uebung, 1L)).willReturn(Optional.of(termin));
        given(terminService.idZuTermin(uebung, 2L)).willReturn(Optional.of(termin2));

        gruppenService.erstelleGruppeOrga(2L,"test", Set.of(1L,2L), 1L);

        //Entfernen von Benutzern
        verify(terminService, times(2)).saveTermin(uebung, zuPruefen);
        // Speichern von neuer Gruppe
        verify(terminService, times(1)).saveTermin(uebung, zuPruefen2);
    }
//
    @Test
    @DisplayName("Mitglieder Hinzufuegen termin ist aber nicht Vorhanden")
    void mitgliedHinzufuegenTerminIstUngueltig() {
        final Uebung uebung = mock(Uebung.class);

        given(uebungRepo.findUebungByTerminId(1L)).willReturn(Optional.ofNullable(uebung));

        assertThrows(IllegalArgumentException.class, () -> {
            gruppenService.mitgliedHinzufuegen(-1L, Set.of(1L), 1L);
        });
    }
//
    @Test
    @DisplayName("Mitglieder Hinzufuegen Uebung ist aber nicht Vorhanden")
    void mitgliedHinzufuegenUebungIstUngueltig() {
        assertThrows(IllegalArgumentException.class, () -> {
            gruppenService.mitgliedHinzufuegen(1L, Set.of(), -1L);
        });
    }
//
    @Test
    @DisplayName("Mitglieder Hinzufuegen Gruppehat keine Mitglieder")
    void mitgliedHinzufuegenGruppeIstLeer() {
        final Uebung uebung = mock(Uebung.class);


        final Termin termin = Termin.builder()
                .terminId(1L)
                .datum(LocalDateTime.MIN)
                .build();

        given(uebungRepo.findUebungByTerminId(1L)).willReturn(Optional.ofNullable(uebung));
        given(terminService.idZuTermin(any(), anyLong())).willReturn(Optional.of(termin));
        assertThrows(IllegalArgumentException.class, () -> {
            gruppenService.mitgliedHinzufuegen(1L, Set.of(), 1L);
        });
    }
//
    @Test
    @DisplayName("Mitglieder Hinzufuegen Gruppenmitglied ist ungueltig")
    void mitgliedHinzufuegenUngueltigeUserId() {
        final Uebung uebung = mock(Uebung.class);


        final Termin termin = Termin.builder()
                .terminId(1L)
                .datum(LocalDateTime.MIN)
                .build();

        given(uebungRepo.findUebungByTerminId(1L)).willReturn(Optional.ofNullable(uebung));
        given(terminService.idZuTermin(any(), anyLong())).willReturn(Optional.of(termin));

        assertThrows(IllegalStateException.class, () -> {
            gruppenService.mitgliedHinzufuegen(1L, Set.of(1L, -1L), 1L);
        });
    }
//
    @Test
    @DisplayName("Mitglieder Hinzufuegen Termin ist aber nicht in der Uebung")
    void mitgliedHinzufuegenTerminIstNichtInUebung() {
        final Uebung uebung = mock(Uebung.class);

        given(uebungRepo.findUebungByTerminId(1L)).willReturn(Optional.ofNullable(uebung));
        assertThrows(IllegalArgumentException.class, () -> {
            gruppenService.mitgliedHinzufuegen(-1L, Set.of(1L), 2L);
        });
    }
//
    @Test
    @DisplayName("Mitglieder Hinzufuegen Gruppe wird zu Gross")
    void mitgliedHinzufuegenGruppeWirdZuGross() {
        final Uebung uebung = mock(Uebung.class);

        when(uebung.getGruppenGroesse()).thenReturn(0);

        final Termin termin = Termin.builder()
                .terminId(1L)
                .datum(LocalDateTime.MIN)
                .build();

        given(uebungRepo.findByUebungId(anyLong())).willReturn(Optional.of(uebung));
        given(uebungRepo.findUebungByTerminId(1L)).willReturn(Optional.ofNullable(uebung));
        given(terminService.idZuTermin(any(), anyLong())).willReturn(Optional.of(termin));


        assertThrows(IllegalStateException.class, () -> {
            gruppenService.mitgliedHinzufuegen(1L, Set.of(1L), 2L);
        });
    }

    @Test
    @DisplayName("Mitglieder Hinzufuegen Gruppe hat genau einen Platz frei")
    void mitgliedHinzufuegenGruppeEinPlatzFrei() {
        final Uebung uebung = mock(Uebung.class);

        when(uebung.getGruppenGroesse()).thenReturn(1);

        final Termin termin = Termin.builder()
                .terminId(1L)
                .datum(LocalDateTime.MIN)
                .build();

        given(uebungRepo.findByUebungId(anyLong())).willReturn(Optional.of(uebung));
        given(uebungRepo.findUebungByTerminId(1L)).willReturn(Optional.ofNullable(uebung));
        given(terminService.idZuTermin(any(), anyLong())).willReturn(Optional.of(termin));

        gruppenService.mitgliedHinzufuegen(1L, Set.of(1L), 2L);

        verify(uebungRepo,times(1)).save(any());
    }
}