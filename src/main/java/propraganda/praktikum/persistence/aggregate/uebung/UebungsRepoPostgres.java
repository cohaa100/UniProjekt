package propraganda.praktikum.persistence.aggregate.uebung;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import propraganda.praktikum.logic.aggregate.uebung.Uebung;
import java.util.Optional;

@Profile("prod")
@Primary
@Repository
public interface UebungsRepoPostgres extends CrudRepository<Uebung, Long> {

    Optional<Uebung> findByUebungId(long uebungsId);

    @Deprecated
    @Query("SELECT * FROM uebung ORDER BY uebungszeitraum_zeitraum_ende DESC LIMIT 1;")
    Optional<Uebung> findUebungByLatest();

    @Query("SELECT * FROM uebung WHERE uebung_id = (" +
            "                              SELECT uebung FROM termin WHERE termin_id = :terminId" +
            "                              );")
    Optional<Uebung> findUebungByTerminId(long terminId);

    @Query("SELECT * FROM uebung WHERE uebung_id = (" +
            "                              SELECT uebung FROM zeitslot WHERE zeitslot_id = :zeitslotId" +
            "                              );")
    Optional<Uebung> findUebungByZeitslotId(long zeitslotId);

    void deleteByUebungId(long uebungsId);
}
