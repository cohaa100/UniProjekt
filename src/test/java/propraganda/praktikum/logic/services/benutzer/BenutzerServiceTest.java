package propraganda.praktikum.logic.services.benutzer;

import org.junit.jupiter.api.*;
import org.kohsuke.github.GHUser;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import propraganda.praktikum.logic.aggregate.benutzer.Benutzer;
import propraganda.praktikum.logic.aggregate.benutzer.BenutzerTyp;
import propraganda.praktikum.persistence.aggregate.benutzer.BenutzerRepoPostgres;
import propraganda.praktikum.logic.services.github.GithubService;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class BenutzerServiceTest {

    @Mock
    private transient BenutzerRepoPostgres benutzerRepo;

    @Mock
    private transient GithubService githubService;



    @InjectMocks
    private transient BenutzerService benutzerService;

    @BeforeEach
    void init() throws IOException {
        MockitoAnnotations.openMocks(this);

        final GHUser ghUser = mock(GHUser.class);
        given(ghUser.getId()).willReturn(1L);

        final GHUser ghUser1 = mock(GHUser.class);
        given(ghUser1.getId()).willReturn(2L);

        final Benutzer benutzer = new Benutzer(2L, "cohaa100", BenutzerTyp.STUDENT);
        given(githubService.getGitHubUserByNameInOrga("cohaa100")).willReturn(ghUser1);
        //benutzer.setBenutzerId(1L);
        given(githubService.getGitHubUserByNameInOrga("nino-salih")).willReturn(ghUser);
        given(benutzerService.findBenutzerByGithubId(2L)).willReturn(Optional.of(benutzer));
        given(benutzerRepo.findByBenutzerId(0L)).willReturn(Optional.of(benutzer));
        given(benutzerRepo.findByGitHubName("cohaa100")).willReturn(Optional.of(benutzer));
    }



    @Test
    @DisplayName("Benutzer hinzufügen")
    void benutzerHinzufuegen() {
        benutzerService.benutzerHinzufuegen(BenutzerTyp.STUDENT, "nino-salih");

        verify(benutzerRepo, times(1)).save(any());

    }

    @Test
    @DisplayName("Benutzer entfernen nicht vorhanden")
    void benutzerEntfernenNichtvorhanden() {


        assertThrows(IllegalArgumentException.class, () -> {
            benutzerService.benutzerEntfernen("nino-salih");
        });
    }

    @Test
    @DisplayName("Benutzer entfernen")
    void benutzerEntfernen() {
        benutzerService.benutzerEntfernen("cohaa100");

        verify(benutzerRepo, times(1)).deleteBenutzerByBenutzerId(anyLong());
    }

    @Test
    @DisplayName("Edit rolle Benutzer nicht vorhanden")
    void editRolleNichtVorhanden() {
        assertThrows(IllegalArgumentException.class, () -> {
            benutzerService.editRolle(2L, BenutzerTyp.STUDENT);
        });
    }

    @Test
    @DisplayName("Edit rolle Benutzer")
    void editRolle() {
        benutzerService.editRolle(0L, BenutzerTyp.STUDENT);
        verify(benutzerRepo, times(1)).save(any());
    }

    @Test
    @DisplayName("Finde Benutzer Liste")
    void findeBenutzer() {
        final Set<Long> ids = new HashSet<>();
        ids.add(0L);

        final Set<Benutzer> benutzerList = new HashSet<>();
        benutzerList.add(benutzerRepo.findByBenutzerId(0L).get());

        assertThat(benutzerService.findBenutzer(ids)).isEqualTo(benutzerList);
    }

    @Test
    @DisplayName("Finde Benutzer Liste falsch")
    void findeBenutzerNichtVorhanden() {
        final Set<Long> ids = new HashSet<>();
        ids.add(2L);

        assertThrows(NoSuchElementException.class, () -> { benutzerService.findBenutzer(ids); } );
    }

    @Test
    @DisplayName("Finde Benutzer von ID")
    void findBenutzerById() {
        final Benutzer benutzer = benutzerRepo.findByBenutzerId(0L).get();
        assertThat(benutzerService.findByBenutzerId(0L).get()).isEqualTo(benutzer);
    }

    @Test
    @DisplayName("Finde Benutzer von GithubID")
    void findBenutzerByGithubId() {
        final Benutzer benutzer = benutzerRepo.findByBenutzerId(0L).get();
        assertThat(benutzerService.findBenutzerByGithubId(2L).get()).isEqualTo(benutzer);
    }

    @Test
    @DisplayName("Bekomme ein Set aus Longs")
    void changeBenutzerToLong() {
        final Set<Benutzer> benutzerList = new HashSet<>();
        benutzerList.add(benutzerRepo.findByBenutzerId(0L).get());

        final Set<Long> benutzerIds = new HashSet<>();
        benutzerIds.add(0L);
        assertThat(benutzerService.changeBenutzerToLong(benutzerList)).isEqualTo(benutzerIds);
    }

    @Test
    @DisplayName("Bekomme ein Set aus Benutzern durch Ids")
    void findBenutzerByIds() {
        final Set<Benutzer> benutzerList = new HashSet<>();
        benutzerList.add(benutzerRepo.findByBenutzerId(0L).get());

        final Set<Long> benutzerIds = new HashSet<>();
        benutzerIds.add(0L);
        assertThat(benutzerService.findBenutzer(benutzerIds)).isEqualTo(benutzerList);
    }

    @Test
    @DisplayName("Bekomme ein Benutzer Durch Githubnamen")
    void findBenutzerByName() {
        final Benutzer benutzer = benutzerRepo.findByBenutzerId(0L).get();

        assertThat(benutzerService.findBenutzerByName("cohaa100").get()).isEqualTo(benutzer);
    }


}