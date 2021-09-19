package propraganda.praktikum.persistence.aggregate.uebung;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import propraganda.praktikum.logic.aggregate.uebung.Termin;
import propraganda.praktikum.logic.aggregate.uebung.AnmeldeTyp;
import propraganda.praktikum.logic.aggregate.uebung.Uebung;
import propraganda.praktikum.logic.aggregate.uebung.Zeitraum;

import java.time.LocalDateTime;
import java.util.Set;

@Profile("dev")
@Slf4j
@Primary
@Repository
public class UebungsFaker extends UebungsRepoDev{


    @Bean
    public void uebungsfakerBean() {
        final LocalDateTime vergangenheit = LocalDateTime.of(2021,3,16,1,1);

        final Termin termin1 = Termin.builder().datum(LocalDateTime.of(2021,1,1,1,1)).build();
        //termin1.setGruppenName("Rofler");
        final Termin termin2 = Termin.builder().datum(LocalDateTime.of(2021,1,1,1,1)).build();

        termin1.setTutor(1L);

        termin1.setGruppenName("gfnfdgjn");
        termin2.setGruppenName("");
        //termin1.setMitglieder(Set.of(42L,2L));

        final Uebung ersteUebung = new Uebung(4, new Zeitraum(vergangenheit, vergangenheit.plusDays(7)), new Zeitraum(vergangenheit.plusDays(7), vergangenheit.plusDays(14)), AnmeldeTyp.GRUPPE);

        termin1.setMitglieder(Set.of(42L,2L));
        ersteUebung.setTermin(Set.of(termin1));

        final Uebung uebung = new Uebung(2, new Zeitraum(vergangenheit, vergangenheit.plusDays(7)), new Zeitraum(vergangenheit.plusDays(8), vergangenheit.plusDays(14)), AnmeldeTyp.INDIVIDUAL);

        map.put(ersteUebung.getUebungId(), ersteUebung);
        map.put(uebung.getUebungId(), uebung);

        log.info("faker wird asugeführt");
    }
}
