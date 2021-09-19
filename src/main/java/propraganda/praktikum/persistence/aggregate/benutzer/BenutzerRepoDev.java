package propraganda.praktikum.persistence.aggregate.benutzer;


import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import propraganda.praktikum.logic.aggregate.benutzer.Benutzer;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("dev")
@SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
public class BenutzerRepoDev implements BenutzerRepoPostgres{

    final static Map<Long, Benutzer> MAP = new ConcurrentHashMap<>(100);

    @Override
    public Optional<Benutzer> findByBenutzerId(final long benutzerId) {
        return Optional.ofNullable(MAP.get(benutzerId));
    }

    @Override
    public Optional<Benutzer> findByGitHubUserId(final long githubId) {
        return MAP.values()
                .parallelStream()
                .filter(benutzer -> benutzer.getGitHubUserId() == githubId)
                .findFirst();
    }

    @Override
    public Optional<Benutzer> findByGitHubName(final String gitHubName) {
        Optional<Benutzer> benutzer = null;
        for (final Benutzer b : MAP.values()) {
            if(b.getGitHubName().contains(gitHubName)) {
                benutzer = Optional.of(b);
                break;
            }
        }
        return benutzer;
    }

    @Override
    public void deleteBenutzerByBenutzerId(final long benutzerId) {
        MAP.remove(benutzerId);
    }

    @Override
    public <S extends Benutzer> S save(final S entity) {

        if (Benutzer.class.isInstance(entity)) {
            MAP.put(entity.getBenutzerId(),entity);
        }
        return null;
    }

    @Override
    public <S extends Benutzer> Iterable<S> saveAll(final Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<Benutzer> findById(final Long aLong) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(final Long aLong) {
        return false;
    }

    @Override
    public Iterable<Benutzer> findAll() {
        return null;
    }

    @Override
    public Iterable<Benutzer> findAllById(final Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(final Long aLong) {

    }

    @Override
    public void delete(final Benutzer entity) {

    }

    @Override
    public void deleteAll(final Iterable<? extends Benutzer> entities) {

    }

    @Override
    public void deleteAll() {

    }
}
