package propraganda.praktikum.logic.services.benutzer;

import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import propraganda.praktikum.logic.aggregate.benutzer.Benutzer;
import propraganda.praktikum.logic.aggregate.benutzer.BenutzerTyp;
import propraganda.praktikum.persistence.aggregate.benutzer.BenutzerRepoPostgres;
import propraganda.praktikum.logic.services.github.GithubService;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BenutzerService {

    @Autowired
    private transient BenutzerRepoPostgres benutzerRepo;

    @Autowired
    private transient GithubService githubService;


    public Optional<Benutzer> findBenutzerByGithubId(final long gitHubId) {
        return benutzerRepo.findByGitHubUserId(gitHubId);
    }


    public Optional<Benutzer> findByBenutzerId(final long benutzerId) {
        return benutzerRepo.findByBenutzerId(benutzerId);
    }

    public void benutzerHinzufuegen(final BenutzerTyp benutzerTyp,final String githubName) {
        try {

            final Optional<Benutzer> vorhanden = benutzerRepo.findByGitHubName(githubName);
            if(vorhanden.isPresent()) {
                log.info("Benutzer {} existiert bereits in der Datenbank", githubName);
                if(vorhanden.get().getBenutzerTyp() != benutzerTyp) {
                    log.info("Benutzer {} bekommt die neue Rolle: {}", vorhanden.get().getGitHubName(), benutzerTyp);
                    vorhanden.get().setBenutzerTyp(benutzerTyp);
                    benutzerRepo.save(vorhanden.get());
                }
                return;
            }
            final GHUser ghUser = githubService.getGitHubUserByNameInOrga(githubName);
           final Benutzer benutzer = new Benutzer(ghUser.getId(), ghUser.getLogin(), benutzerTyp);
           benutzerRepo.save(benutzer);
        } catch (IOException ioException) {
            log.error(ioException.getMessage());
        }
    }

    public void benutzerEntfernen(final String githubName) {
        try {
            final GHUser ghUser = githubService.getGitHubUserByNameInOrga(githubName);
            final Optional<Benutzer> benutzer = benutzerRepo.findByGitHubUserId(ghUser.getId());
            if(benutzer.isEmpty()){
                throw new IllegalArgumentException("Benutzer gibt es nicht mit dem Namen");
            }
            githubService.removeUserFromOrga(githubName);
            benutzerRepo.deleteBenutzerByBenutzerId(benutzer.get().getBenutzerId());
        } catch (IOException exception) {
            log.error(exception.getMessage());
        }
    }


    public void editRolle(final long benutzerId,final BenutzerTyp benutzerTyp) {
        final Optional<Benutzer> benutzer = benutzerRepo.findByBenutzerId(benutzerId);
        if(benutzer.isEmpty()){
            throw new IllegalArgumentException("Benutzer mit id konnte nicht gefunden werden.");
        }
        benutzer.get().setBenutzerTyp(benutzerTyp);
        benutzerRepo.save(benutzer.get());
    }

    public Set<Benutzer> findBenutzerByName(final Set<String> benutzer) {
        final Set<Benutzer> list = new HashSet<>();
        benutzer.forEach(b ->
                list.add(benutzerRepo.findByGitHubName(b).orElseThrow()));
        return list;
    }


    public Optional<Benutzer> findBenutzerByName(final String githubName) {
        return benutzerRepo.findByGitHubName(githubName);
    }

    public Set<Benutzer> findBenutzer(final Set<Long> benutzerIdListe) {
        final Set<Benutzer> benutzerList = new HashSet<>();
        benutzerIdListe.forEach(id ->
                benutzerList.add(benutzerRepo.findByBenutzerId(id).orElseThrow()));
        return benutzerList;
    }

    public Set<Long> changeBenutzerToLong(final Set<Benutzer> benutzerSet) {
       return benutzerSet.parallelStream().map(Benutzer::getBenutzerId).collect(Collectors.toSet());
    }

}
