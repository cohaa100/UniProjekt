package propraganda.praktikum.logic.services.uebung;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import propraganda.praktikum.logic.aggregate.benutzer.Benutzer;
import propraganda.praktikum.logic.aggregate.benutzer.BenutzerTyp;
import propraganda.praktikum.logic.aggregate.uebung.Termin;
import propraganda.praktikum.persistence.aggregate.benutzer.BenutzerRepoPostgres;
import propraganda.praktikum.logic.aggregate.uebung.Uebung;
import propraganda.praktikum.persistence.aggregate.uebung.UebungsRepoPostgres;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class GruppenService {
    @Autowired
    private transient BenutzerRepoPostgres benutzerRepo;
    @Autowired
    private transient UebungsRepoPostgres uebungRepo;
    @Autowired
    private transient TerminService terminService;

    private static final String KEIN_TERMIN_MIT_DIESER_ID_GEFUNDEN = "kein Termin mit dieser ID gefunden";

    public void editGruppen(final Uebung uebung,final long terminId, final long tutor,final Set<Long> mitglieder,final String name) {

        // termin ist nicht in der Uebung
        if(!terminService.isTerminIdInUebung(uebung, terminId)){
            return;
        }

        final Optional<Benutzer> tutorBenutzer = benutzerRepo.findByBenutzerId(tutor);

        final Optional<Termin> termin = terminService.idZuTermin(uebung,terminId);


        // Tutor ist gueltiger BenutzerId && tutor auch rechte Tutor hat
        if(tutorBenutzer.isPresent() && tutorBenutzer.get().getBenutzerTyp() == BenutzerTyp.TUTOR ) {
           terminService.setTerminTutor(uebung, terminId, tutor);
        }

        // Mitglieder sind vorhaden
        if(mitglieder != null && !mitglieder.isEmpty() && isUserIdInRepo(mitglieder)) {
            mitgliederAusAltenGruppenEntfernen(mitglieder, uebung);
            termin.get().setMitglieder(mitglieder);
        }
        if(name != null) {
            termin.get().setGruppenName(name);
        }
        terminService.saveTermin(uebung,termin.get());
    }

    public void editGruppen(final Long terminId,final Set<Long> mitglieder,final String name) {

        final Optional<Uebung> uebung = uebungRepo.findUebungByTerminId(terminId);

        if(uebung.isEmpty()) {
            log.error("Der Termin mit der Id {} ist keiner Uebung zugeordnet", terminId);
            throw new IllegalArgumentException("Termin ist ungueltig");
        }
        log.debug("Der Termin ist in der Uebung {}", uebung.get());
        editGruppen(uebung.get(),terminId,-1,mitglieder,name);
    }

    /*private Set<Long> getLongSet(final Set<Benutzer> mitglieder,final  Optional<Termin> termin) {
        final Set<Long> idSet = mitglieder.parallelStream().map(Benutzer::getBenutzerId).collect(Collectors.toSet());
        if(termin.isEmpty()){
            throw new IllegalArgumentException(KEIN_TERMIN_MIT_DIESER_ID_GEFUNDEN);
        }
        return idSet;
    }*/

    public void erstelleGruppeOrga(final Long terminId,final String name,final Set<Long> mitglieder,final Long uebungsId){

        final Optional<Uebung> uebung = uebungRepo.findUebungByTerminId(terminId);

        if(uebung.isEmpty()) {
            log.error("Der Termin mit der Id {} ist keiner Uebung zugeordnet", terminId);
            throw new IllegalArgumentException("Termin ist ungueltig");
        }

        final Optional<Termin> termin = terminService.idZuTermin(uebung.get(), terminId);

        istGuelitgeGruppe(name, uebungsId, termin, mitglieder);

        termin.get().setBelegt(true);

        mitgliederAusAltenGruppenEntfernen(mitglieder, uebung.get());

        termin.get().setGruppenName(name);
        termin.get().setMitglieder(mitglieder);

        terminService.saveTermin(uebung.get(), termin.get());
    }

    public void erstelleGruppeBenutzer(final Long terminId,final String name,final Set<Long> mitglieder,final Long uebungsId){

        final Optional<Uebung> uebung = uebungRepo.findUebungByTerminId(terminId);

        final Optional<Termin> termin = terminService.idZuTermin(uebung.get(), terminId);

        istGuelitgeGruppe(name, uebungsId, termin, mitglieder);

        if(gruppenGroesseUeberpruefen(Set.of(), uebung.get(), mitglieder)) {
            throw new IllegalArgumentException("Gruppe wird zu groß");
        }

        final Set<Long> benutzerSet = getMitgliederNichtInGruppe(mitglieder, uebungsId);

        if(benutzerSet.isEmpty()){
            throw new EmptyResultDataAccessException(1);
        }
        termin.get().setMitglieder(new HashSet<>());

        for(final Long id : benutzerSet) {
            addGruppenMitglied(uebung.get(), terminId,id);
        }
        setGruppenName(uebung.get(), terminId, name);

        uebungRepo.save(uebung.get());
    }

    private void istGuelitgeGruppe(final String name, final Long uebungsId, final Optional<Termin> termin, final Set<Long> mitgliederIdSet) {
        final Optional<Uebung> uebung = uebungRepo.findByUebungId(uebungsId);

        if(uebung.isEmpty()){
            log.error("Uebubng mit Id: {} nicht gefunden", uebungsId);
            throw new IllegalArgumentException("keine Uebubng mit dieser ID gefunden");
        }

        if(termin.isEmpty()){
            log.error("Der Termin ist nicht Vorhanden");
            throw new IllegalArgumentException(KEIN_TERMIN_MIT_DIESER_ID_GEFUNDEN);
        }

        if( mitgliederIdSet == null || mitgliederIdSet.isEmpty()) {
            log.error("Die Mitglieder sind leer oder null");
            throw new IllegalArgumentException();
        }

        if(termin.get().isBelegt()){
            log.error("Termin mit Id: {}", termin.get().getTerminId());
            throw new IllegalArgumentException("Termin belegt");
        }

        if(!isUserIdInRepo(mitgliederIdSet)) {
            throw new IllegalStateException("Ungueltige userId ueberreicht");
        }

        if(name == null || name.equals("")){
            log.error("Der Gruppenname ist ungueltig");
            throw new IllegalArgumentException("name muss angegeben werden");
        }
    }

    public void loescheGruppe(final Long terminId){
        final Optional<Uebung> uebung = uebungRepo.findUebungByTerminId(terminId);

        if(uebung.isEmpty()) {
            log.error("Der Termin mit der Id {} ist keiner Uebung zugeordnet", terminId);
            throw new IllegalArgumentException("Termin ist ungueltig");
        }
        final Termin termin = terminService.idZuTermin(uebung.get(),terminId).get();

        if(!termin.isBelegt()){
            // Jokes on you sie kann
            throw new IllegalStateException("Eine leere Gruppe kann nicht geloescht werden");
        }

        termin.setGruppe(null);
        terminService.saveTermin(uebung.get(), termin);

        //uebungRepo.save(uebung.get());
    }

    public void mitgliedHinzufuegen(final Long terminId, final Set<Long> benutzerId, final Long uebungsId) {


        final Optional<Uebung> uebung = uebungRepo.findByUebungId(uebungsId);

        if(uebung.isEmpty()){
            throw new IllegalArgumentException("keine Uebung mit dieser ID gefunden");
        }

        final Optional<Termin> termin = terminService.idZuTermin(uebung.get(), terminId);

        if(termin.isEmpty()){
            throw new IllegalArgumentException(KEIN_TERMIN_MIT_DIESER_ID_GEFUNDEN);
        }


        if(benutzerId == null || benutzerId.isEmpty()) {
            throw new IllegalArgumentException("Kein Mitglieder bestimmt");
        }

        if(!isUserIdInRepo(benutzerId)) {
            throw new IllegalStateException("Ein GruppenMitglied ist Ungueltig");
        }

        //final Set<Long> mitglieder = new TreeSet<>(getGruppenMitglieder(uebung.get(),terminId));

        if(gruppenGroesseUeberpruefen(benutzerId, uebung.get(), termin.get().getMitglieder())){
            throw new IllegalStateException("Gruppe wird zu gross");
        }

        mitgliederAusAltenGruppenEntfernen(benutzerId, uebung.get());

        for(final Long id : benutzerId) {
            log.info("Hinzufuegen Termin {} Benutzer {}", terminId, benutzerId);
            addGruppenMitglied(uebung.get(), terminId,id);
        }
        uebungRepo.save(uebung.get());

    }

    private boolean isUserIdInRepo(final Set<Long> benutzerId) {

        final AtomicBoolean isUser = new AtomicBoolean(true);
        benutzerId.parallelStream().forEach(id ->  {
            if(benutzerRepo.findByBenutzerId(id).isEmpty()) {
                log.error("Der Benutzer mit der ID: {} konnte nicht gefunden werden", id);
                isUser.set(false);
            }
        });
        return isUser.get();
    }

    private void mitgliederAusAltenGruppenEntfernen(final Set<Long> benutzerId, final Uebung uebung) {
        //Benutzer entfernen wenn schon in einem Termin

        final Set<Termin> bevorEntfernen = new HashSet<>(uebung.getTermin());

        for(final Termin termin : bevorEntfernen) {
            for(final long bId : benutzerId) {
                if(termin.getGruppe().getMitglieder().contains(bId)){

                    removeGruppenMitglied(uebung,termin.getTerminId(), bId);
                }
            }
        }
    }

    private Set<Long> getMitgliederNichtInGruppe(final Set<Long> benutzerId, final Long uebungId) {
        final Optional<Uebung> uebung = uebungRepo.findByUebungId(uebungId);

        if(uebung.isEmpty()) {
            throw new IllegalArgumentException("Uebung exisitiert nicht");
        }

        final Set<Long> benutzer = new TreeSet<>(benutzerId);

        for (final Termin termin : uebung.get().getTermin()) {
            for(final long bId : benutzerId){
                if (getGruppenMitglieder(uebung.get(), termin.getTerminId()).contains(bId)) {
                    benutzer.remove(bId);
                }
            }
        }
        return benutzer;
    }

    private boolean gruppenGroesseUeberpruefen(final Set<Long> benutzerId,final Uebung uebung,final Set<Long> mitglieder) {
        return (mitglieder.size() + benutzerId.size()) > uebung.getGruppenGroesse();
    }

    public Set<Long> getGruppenMitglieder(final Uebung uebung,final long terminId) {
        final Optional<Termin> termin1 = terminService.idZuTermin(uebung, terminId);

        if(termin1.isEmpty()) {
            return Collections.emptySet();
        }

        return termin1.get().getGruppe().getMitglieder();
    }

    public String getGruppenName(final Uebung uebung, final long terminId) {
        final Optional<Termin> termin1 = terminService.idZuTermin(uebung, terminId);

        if(termin1.isEmpty()) {
            return "";
        }

        return termin1.get().getGruppe().getName();
    }

    public void setGruppenName(final Uebung uebung, final long terminId,final String name) {
        final Optional<Termin> termin1 = terminService.idZuTermin(uebung, terminId);

        if(termin1.isEmpty()) {
            return;
        }

        termin1.get().setGruppenName(name);
        terminService.saveTermin(uebung,termin1.get());
    }

    public void removeGruppenMitglied(final Uebung uebung, final long terminId ,final long benuzterId) {
        final Optional<Termin> termin1 = terminService.idZuTermin(uebung, terminId);

        if(termin1.isEmpty()) {
            return;
        }

        log.info("Benutzer {} wird entfernt aus {}", benuzterId, terminId);

        final Set<Long> gruppenMitgliederIds = termin1.get().getGruppe().getMitglieder();

        if(gruppenMitgliederIds.isEmpty()) {
            return;
        }
        termin1.get().removeGruppenMitglied(benuzterId);

        if(termin1.get().getMitglieder().isEmpty()) {
            termin1.get().setBelegt(false);
        }
        log.info(termin1.get().getMitglieder().toString());
        terminService.saveTermin(uebung,termin1.get());
    }

    public void addGruppenMitgliedKeinLimit(final Uebung uebung, final long terminId ,final long benuzterId) {
        final Optional<Termin> termin1 = terminService.idZuTermin(uebung, terminId);

        if(termin1.isEmpty()) {
            return;
        }

        final Set<Long> gruppenMitgliederIds = termin1.get().getGruppe().getMitglieder();
        termin1.get().setBelegt(true);
        termin1.get().getGruppe().setMitglieder(gruppenMitgliederIds);

        terminService.saveTermin(uebung,termin1.get());
    }

    public void addGruppenMitglied(final Uebung uebung, final long terminId ,final long benuzterId) {
        final Optional<Termin> termin1 = terminService.idZuTermin(uebung, terminId);

        if(termin1.isEmpty()) {
            return;
        }

        final Set<Long> gruppenMitgliederIds = termin1.get().getGruppe().getMitglieder();

        if(gruppenMitgliederIds.size() + 1 > uebung.getGruppenGroesse()){
            return;
        }

        termin1.get().setBelegt(true);
        termin1.get().addGruppenMitglied(benuzterId);

        terminService.saveTermin(uebung,termin1.get());
    }


}
