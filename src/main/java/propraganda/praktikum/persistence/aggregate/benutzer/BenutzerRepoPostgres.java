package propraganda.praktikum.persistence.aggregate.benutzer;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import propraganda.praktikum.logic.aggregate.benutzer.Benutzer;

import java.util.Optional;

@Profile("prod")
@Primary
@Repository
public interface BenutzerRepoPostgres extends CrudRepository<Benutzer, Long> {

    Optional<Benutzer> findByBenutzerId(long benutzerId);

    Optional<Benutzer> findByGitHubUserId(long githubId);

    Optional<Benutzer> findByGitHubName(String gitHubName);

    void deleteBenutzerByBenutzerId(long benutzerId);
}
