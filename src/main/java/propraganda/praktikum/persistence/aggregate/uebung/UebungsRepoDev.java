package propraganda.praktikum.persistence.aggregate.uebung;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import propraganda.praktikum.logic.aggregate.uebung.Uebung;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("dev")
@SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
public class UebungsRepoDev implements UebungsRepoPostgres{


    final transient Map<Long, Uebung> map = new ConcurrentHashMap<>(100);

    @Override
    public Optional<Uebung> findByUebungId(final long uebungsId) {
        return Optional.ofNullable(map.get(uebungsId));
    }

    @Override
    //Wird nicht funktionieren
    public Optional<Uebung> findUebungByLatest() {
        Uebung uebung = new ArrayList<>(map.values()).get(0);
        for (final Uebung value : map.values()) {
            if(value.getUebungszeitraum().getZeitraumStart().isAfter(uebung.getUebungszeitraum().getZeitraumStart())) {
                uebung = value;
            }


        }
        return Optional.ofNullable(uebung);
    }

    @Override
    public Optional<Uebung> findUebungByTerminId(final long terminId) {
        final List<Optional<Uebung>> result = new ArrayList<>();
        findAll().forEach( uebung -> {
            uebung.getTermin()
                    .parallelStream()
                    .filter(termin -> termin.getTerminId() == terminId)
                    .forEach( termin -> { result.add(Optional.of(uebung)); }
                    );
                }
        );

        if(!result.isEmpty()) {
            return result.get(0);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Uebung> findUebungByZeitslotId(final long zeitslotId) {
        return Optional.empty();
    }

    @Override
    public void deleteByUebungId(final long uebungsId) {
          map.remove(uebungsId);
    }

    @Override
    public <S extends Uebung> S save(final S entity) {
        if(Uebung.class.isInstance(entity)) {
            if(entity.getUebungId() == 0) {
                entity.setUebungId(count() + 1);
            }
            map.put(entity.getUebungId(),entity);
        }
        return entity;
    }

    @Override
    public <S extends Uebung> Iterable<S> saveAll(final Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<Uebung> findById(final Long aLong) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(final Long aLong) {
        return false;
    }

    @Override
    public Iterable<Uebung> findAll() {
        return map.values();
    }

    @Override
    public Iterable<Uebung> findAllById(final Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return map.size();
    }

    @Override
    public void deleteById(final Long aLong) {
        map.remove(aLong);
    }

    @Override
    public void delete(final Uebung entity) {

    }

    @Override
    public void deleteAll(final Iterable<? extends Uebung> entities) {

    }

    @Override
    public void deleteAll() {

    }
}
