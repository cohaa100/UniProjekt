package propraganda.praktikum.logic.services.sync;


import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import propraganda.praktikum.logic.aggregate.uebung.Termin;
import propraganda.praktikum.logic.aggregate.uebung.Uebung;
import propraganda.praktikum.logic.aggregate.uebung.Zeitslot;
import propraganda.praktikum.persistence.aggregate.uebung.UebungsRepoPostgres;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
@EnableScheduling
@SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
public class ZuordnungsService {

    @Autowired
    private transient UebungsRepoPostgres uebungRepo;

    private final List<Uebung> uebungArrayList = new ArrayList<>();

    @Scheduled(cron = "0 0 * * * *")
    public void tutorZuordnung() {
        if(uebungRepo.findUebungByLatest().isEmpty()) {
            return;
        }
        final Optional<Uebung> uebung = uebungRepo.findUebungByLatest();
        if(!uebung.get().getAnmeldezeitraum().getZeitraumEnde().isBefore(LocalDateTime.now()) || uebungArrayList.contains(uebung.get())) {

            return;
        }
        uebungArrayList.clear();
        uebungArrayList.add(uebung.get());

        final Set<Zeitslot> zeitslots = uebung.get().getZeitslots();

        final ArrayList list = new ArrayList<>(uebung.get().getTermin());
        for(final Zeitslot zeitslot : zeitslots) {

            fuegeTutorhinzu(suchePassendeTermine(zeitslot.getDatum(), list), zeitslot, list);
        }
        uebungRepo.save(uebung.get());
    }

    public List<Termin> suchePassendeTermine(final LocalDateTime localDateTime,final  List<Termin> terminList) {
        final List<Termin> termins = new ArrayList<>();
        terminList.forEach(termin -> {

            if(termin.getDatum().isEqual(localDateTime) && termin.isBelegt() && termin.getTutor() == null) {
                termins.add(termin);
            }
        });
        return termins;
    }

    public void fuegeTutorhinzu(final List<Termin> terminList, final  Zeitslot zeitslot, final List<Termin> alleTermine) {
        if(terminList.size() == zeitslot.getGroesse()) {
            final AtomicInteger atomicInteger = new AtomicInteger();
            terminList.forEach(termin -> {
                termin.setTutor(zeitslot.getTutorId().get(atomicInteger.get()));
                atomicInteger.getAndIncrement();
            });
            return;
        }
        final HashMap<Long, Integer> gewichtBenutzer = new HashMap<>();
        //Shuffle damit die tutor ids random vorkommen
        Collections.shuffle(zeitslot.getTutorId());
        terminList.forEach(termin -> {
            //Return if zeitslot is empty so 0L is not set to db (error)
            if(zeitslot.getTutorId().isEmpty()) {
                return;
            }
            final AtomicReference<Long> tutor = new AtomicReference<>(0L);
            zeitslot.getTutorId().forEach(id -> {
                //Abfrage ob ein Tutor schon in einem anderen Termin drin ist
                final boolean inTermin = isInTerminZeitslot(id, alleTermine, termin.getDatum());
                if(!inTermin) {
                    final int gewicht = gewichtBenutzer.getOrDefault(id, getGewicht(alleTermine, id));
                    if (!gewichtBenutzer.containsKey(id)) {
                        gewichtBenutzer.put(id, gewicht);
                    }

                    if (gewicht < gewichtBenutzer.getOrDefault(tutor.get(), 100)) {
                        tutor.set(id);
                    }
                }
            });

            gewichtBenutzer.put(tutor.get(), 1000);
            if(tutor.get() != 0L) {
                termin.setTutor(tutor.get());
            }
            tutor.set(0L);
        });


    }
    //Muss noch gefixxt werden Spotbugs aber ka was der genau will
    public int getGewicht(final List<Termin> terminList, final long benutzerId) {
        if(terminList == null) {
            return 0;
        }
        int gewicht = 0;
        for (final Termin termin : terminList) {
            //Nullpointer wird hier verhindert Spotbugs heult ist jedoch egal
            if(termin != null && termin.getTutor() != null && benutzerId == termin.getTutor()) {
                    gewicht++;

            }
        }
        return gewicht;
    }

    public boolean isInTerminZeitslot(final long tutorid, final List<Termin> terminList, final LocalDateTime localDateTime) {
        boolean inTermin = false;
        for (final Termin termin : terminList) {
            //Nullpointer wird hier verhindert Spotbugs heult ist jedoch egal
            if (termin != null && termin.getTutor() != null && termin.getTutor() == tutorid && termin.getDatum().isEqual(localDateTime)) {
                    inTermin = true;
                }
        }

        return inTermin;
    }





}
