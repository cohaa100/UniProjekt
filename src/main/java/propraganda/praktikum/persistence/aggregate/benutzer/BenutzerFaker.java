package propraganda.praktikum.persistence.aggregate.benutzer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import propraganda.praktikum.logic.aggregate.benutzer.Benutzer;
import propraganda.praktikum.logic.aggregate.benutzer.BenutzerTyp;

@Profile("dev")
@Slf4j
@Primary
@Repository
public class BenutzerFaker extends BenutzerRepoDev {

    @Bean
    void benutzerFakerBean() {
        //nino-salih
        final Benutzer tutor = new Benutzer(56120209,"nino-salih", BenutzerTyp.ORGANISATOR);//NOPMD
        tutor.setBenutzerId(42);
        //balam101
        final Benutzer orga = new Benutzer(63661071,"balam101",BenutzerTyp.ORGANISATOR);//NOPMD
        orga.setBenutzerId(1);
        final Benutzer orga2 = new Benutzer(32685843,"Cskorpion",BenutzerTyp.ORGANISATOR);//NOPMD
        orga2.setBenutzerId(2);

        final Benutzer orga3 = new Benutzer(63955715,"cohaa100", BenutzerTyp.ORGANISATOR);//NOPMD
        orga3.setBenutzerId(3);

        MAP.put(tutor.getBenutzerId(), tutor);
        MAP.put(orga.getBenutzerId(), orga);
        MAP.put(orga2.getBenutzerId(), orga2);
        MAP.put(orga3.getBenutzerId(), orga3);
    }
}
