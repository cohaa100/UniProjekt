package propraganda.praktikum.logic.services.sync;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import propraganda.praktikum.logic.aggregate.benutzer.Benutzer;
import propraganda.praktikum.logic.aggregate.uebung.Termin;
import propraganda.praktikum.logic.aggregate.uebung.Uebung;
import propraganda.praktikum.persistence.aggregate.uebung.UebungsRepoPostgres;
import propraganda.praktikum.logic.services.benutzer.BenutzerService;
import propraganda.praktikum.logic.services.github.GithubService;

import java.time.LocalDate;
import java.util.Set;

@Service
@Slf4j
@Profile("prod")
@EnableScheduling
public class GithubRepoService {

    @Autowired
    transient UebungsRepoPostgres uebungsRepo;

    @Autowired
    transient GithubService githubService;

    @Autowired
    transient BenutzerService benutzerService;

    @Scheduled(cron =  "* * * 1 * *") // Jeden Tag einmal
    public void createGroupRepos() {
        final Iterable<Uebung> uebungen = uebungsRepo.findAll();
        uebungen.forEach(uebung -> {
            if(uebung.getAnmeldezeitraum().getZeitraumEnde().toLocalDate().isBefore(LocalDate.now()) || uebung.getAnmeldezeitraum().getZeitraumEnde().toLocalDate().equals(LocalDate.now())) {
                createRepoUebung(uebung);
            }
        });
    }

    private void createRepoUebung(final Uebung uebung) {
        final Set<Termin>  termine = uebung.getTermin();
        termine.stream().filter(Termin::isBelegt).forEach(termin -> {
            createGruppenRepo(uebung, termin);
        });
    }

    private void createGruppenRepo(final Uebung uebung, final Termin termin) {
        final String repoName = "Uebung-" + uebung.getUebungId() + "-" + termin.getGruppenName().replaceAll("[^\\w\\s]","") + "-" + termin.getDatum().toLocalDate();
        log.debug("Erstelle Repo mit {}", repoName);
        githubService.createRepo(repoName);
        termin.getMitglieder().stream().forEach(user -> {
            addUserToRepo(repoName, user);
        });
    }

    private void addUserToRepo(final String repoName,final  Long user) {
        final Benutzer benutzer = benutzerService.findByBenutzerId(user).get();
        githubService.addUserToRepo(repoName, benutzer.getGitHubName());
    }
}