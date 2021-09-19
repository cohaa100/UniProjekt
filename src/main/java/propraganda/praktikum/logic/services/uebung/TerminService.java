package propraganda.praktikum.logic.services.uebung;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import propraganda.praktikum.logic.aggregate.uebung.Termin;
import propraganda.praktikum.logic.aggregate.uebung.Uebung;
import propraganda.praktikum.logic.aggregate.uebung.Zeitslot;
import propraganda.praktikum.persistence.aggregate.uebung.UebungsRepoPostgres;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class TerminService {

    @Autowired
    private transient UebungsRepoPostgres uebungRepo;

    public void createTemplate(final Uebung aktuelleUebung, final Uebung letzteUebung) {
        if(uebungRepo.count() <=0) {
            return;
        }
        final Set<Termin> alteTermine = letzteUebung.getTermin();

        long days = aktuelleUebung.getUebungszeitraum().getZeitraumStart().getDayOfYear()-letzteUebung.getUebungszeitraum().getZeitraumStart().getDayOfYear();
        if(days < 0 ){
            days += 365; // days kann negativ sein
        }

        //termineAktualisieren.stream().forEach(e -> neueTermine.add(createTerminNewId(e)));

        for(final Termin termin: alteTermine) {
            final Termin terminNeu = Termin.builder().datum(termin.getDatum().plusDays(days)).build();
            saveTermin(aktuelleUebung, terminNeu);
        }
        //log.info(""+neueTermine.stream().findFirst().get().getTerminId());

        //neueTermine.stream().forEach(termin -> termin.setDatum(termin.getDatum().plusDays(days2)));
    }

    public Optional<Termin> idZuTermin(final Uebung uebung, final long terminId) {
        return uebung.getTermin().stream().filter(t -> t.getTerminId() == terminId).findFirst();
    }

    public void setTerminTutor(final Uebung uebung,final long terminId, final long tutorId) {
        final Optional<Termin> termin1 = idZuTermin(uebung,terminId);

        if(termin1.isEmpty()) {
            return;
        }

        termin1.get().setTutor(tutorId);
    }

    public Optional<Long> getTerminTutor(final Uebung uebung,final long terminId) {
        final Optional<Termin> termin1 = idZuTermin(uebung, terminId);

        if(termin1.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(termin1.get().getTutor());
    }

    public void setGruppenMitglieder(final Uebung uebung, final long terminId, final Set<Long> mitglieder) {
        final Optional<Termin> termin1 = idZuTermin(uebung, terminId);

        if(termin1.isEmpty()) {
            return;
        }
        termin1.get().setMitglieder(mitglieder);
    }

    public boolean isTerminIdInUebung(final Uebung uebung,final long terminId) {
        final Optional<Termin> termin1 = idZuTermin(uebung,terminId);
        return termin1.isPresent();
    }

    public void removeTermin(final Uebung uebung,final Termin termin) {
        final Set<Termin> termine = uebung.getTermin();
        termine.remove(termin);
        uebung.setTermin(termine);
        removeZeitslot(uebung, termin.getDatum(), termine);
        uebungRepo.save(uebung);
    }

    public Uebung saveTermin(final Uebung uebung, final Termin termin) {
        final Set<Termin> terminSet = uebung.getTermin();
        terminSet.remove(termin);
        terminSet.add(termin);
        uebung.setTermin(terminSet);
        log.info("Uebung : {} mit Termin {}", uebung.toString(), termin.toString());
        addZeitslot(uebung, termin.getDatum(), terminSet);
        uebungRepo.save(uebung);
        return uebung;
    }

    public void addZeitslot(final Uebung uebung, final LocalDateTime localDateTime,final  Set<Termin> terminSet) {

        int groesse = 0;

        for (final Termin termin : terminSet) {
            if (termin.getDatum().isEqual(localDateTime)) {
                groesse++;
            }
        }
        Zeitslot zeitslot = null;
        for(final Zeitslot zeitslots : uebung.getZeitslots()) {
            if(zeitslots.getDatum().isEqual(localDateTime)) {
                zeitslot = zeitslots;
                zeitslot.setGroesse(groesse);
            }
        }
        if(zeitslot == null) {
            zeitslot = new Zeitslot(localDateTime, groesse);
            final Set<Zeitslot> zeitslots = uebung.getZeitslots();
            zeitslots.add(zeitslot);
            uebung.setZeitslots(zeitslots);
        }

    }

    public void removeZeitslot(final Uebung uebung, final LocalDateTime localDateTime, final Set<Termin> terminSet) {

        int groesse = 0;

        for (final Termin termin : terminSet) {
            if (termin.getDatum().isEqual(localDateTime)) {
                groesse++;
            }
        }
        Zeitslot zeitslot = null;
        for(final Zeitslot zeitslots : uebung.getZeitslots()) {
            if(zeitslots.getDatum().isEqual(localDateTime)) {
                zeitslots.setGroesse(groesse);
                zeitslot = zeitslots;
            }
        }


        final Set<Zeitslot> zeitslots = uebung.getZeitslots();
        if(zeitslot.getGroesse() == 0) {
            zeitslots.remove(zeitslot);
        }
        uebung.setZeitslots(zeitslots);
    }



}
