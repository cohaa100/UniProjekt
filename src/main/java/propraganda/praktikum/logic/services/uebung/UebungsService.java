package propraganda.praktikum.logic.services.uebung;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import propraganda.praktikum.logic.aggregate.uebung.*;
import propraganda.praktikum.persistence.aggregate.uebung.UebungsRepoPostgres;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class UebungsService {

    @Autowired
    private transient UebungsRepoPostgres uebungRepo;

    public void erstelleUebung(final int gruppenGroesse,final Zeitraum anmeldeZeitraum,final Zeitraum uebungsZeitraum,final AnmeldeTyp anmeldeTyp) {
        //LAde Template

        final Uebung uebung = new Uebung(gruppenGroesse, anmeldeZeitraum,uebungsZeitraum, anmeldeTyp);
        uebungRepo.save(uebung);
    }

    public void erstelleUebung(final int gruppenGroesse,final Zeitraum anmeldeZeitraum,final Zeitraum uebungsZeitraum,final AnmeldeTyp anmeldeTyp,final Set<Termin> terminSet) {
        final Uebung uebung = new Uebung(gruppenGroesse, anmeldeZeitraum,uebungsZeitraum, anmeldeTyp);
        uebung.setTermin(terminSet);
        uebungRepo.save(uebung);
    }

    public Uebung erstelleUebung(final Uebung uebung) {
        return uebungRepo.save(uebung);
    }

    public void loescheUebung(final long uebungId) {

        final Optional<Uebung> uebung = uebungRepo.findByUebungId(uebungId);

        if(uebung.isEmpty()) {
            throw new IllegalArgumentException("Termin gibt es nicht");
        }
        uebungRepo.deleteById(uebungId);
    }

    public void editUebung(final Set<Termin> termine,final int gruppenGroesse, final  Uebung uebung,final  AnmeldeTyp anmeldeTyp,final  Zeitraum anmeldeZeitraum,final Zeitraum uebungsZeitraum) {
        if(termine != null) {
            uebung.setTermin(termine);
        }
        uebung.setGruppenGroesse(gruppenGroesse);
        if(anmeldeTyp != null) {
            uebung.setAnmeldeTyp(anmeldeTyp);
        }
        if(anmeldeZeitraum != null) {
            uebung.setAnmeldezeitraum(anmeldeZeitraum);
        }
        if(uebungsZeitraum != null) {
            uebung.setUebungszeitraum(uebungsZeitraum);
        }
        uebungRepo.save(uebung);
    }

    public Set<Long> getAlleUebung() {
        final Set<Long> result = new TreeSet<>();
        uebungRepo.findAll().forEach(id -> result.add(id.getUebungId()));
        return result;
    }

    public Uebung getUebung(final long uebungsId) {
        final Optional<Uebung> uebung = uebungRepo.findByUebungId(uebungsId);
        if (uebung.isEmpty()) {
            log.error("Die Uebung mit der Id {} wurde nicht gefuden", uebungsId);
            throw new IllegalStateException();
        }
        return uebung.get();
    }

    public Set<Uebung> getAll() {
        final Set<Uebung> result = new HashSet<>();
        uebungRepo.findAll().forEach(result::add);
        return result;
    }

    public static Uebung createDummy() {
        final Zeitraum anmeldung = new Zeitraum(LocalDateTime.now(), LocalDateTime.now().plusDays(5));
        final Zeitraum uebung = new Zeitraum(LocalDateTime.now().plusDays(7), LocalDateTime.now().plusDays(14));
        return new Uebung(0,anmeldung, uebung , AnmeldeTyp.GRUPPE);
    }

    public Zeitslot findZeitslotById(final Uebung uebung,final  Long zeitslotId) {


       Zeitslot zeitslot = null;
        for (final Zeitslot uebungZeitslot : uebung.getZeitslots()) {


            if (uebungZeitslot.getZeitslotId() == zeitslotId) {
                zeitslot = uebungZeitslot;
            }
        }
        return zeitslot;
    }

    public Uebung findUebungByZeitslotId(final Long zeitslotId) {


        return uebungRepo.findUebungByZeitslotId(zeitslotId).get();
    }

    public Optional<Uebung> findUebungByTerminId(final Long terminId) {
        return uebungRepo.findUebungByTerminId(terminId);
    }


    public Optional<Uebung> getLatestUebung() {
        return uebungRepo.findUebungByLatest();
    }

    public void save(final Uebung uebung) {
        uebungRepo.save(uebung);
    }

    public Set<Termin> termineAnpassen(final Zeitraum neuerUebungsZeitraum,final  Uebung uebung) {
        final int offset = neuerUebungsZeitraum.getZeitraumStart().getDayOfYear() - uebung.getUebungszeitraum().getZeitraumStart().getDayOfYear();

        final Set<Termin> termine = uebung.getTermin();

        for(final Termin termin : termine) {
            final LocalDateTime time = termin.getDatum().plusDays(offset);
            termin.setDatum(time);
            termine.add(termin);
        }

        return termine;
    }

}
