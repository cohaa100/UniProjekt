package propraganda.praktikum.logic.aggregate.uebung;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;
import propraganda.praktikum.logic.aggregate.sterotypes.AggregatRoot;

import java.util.HashSet;
import java.util.Set;

@AggregatRoot
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Uebung {

    @Id
    @EqualsAndHashCode.Include
    private long uebungId;
    @NonNull
    private Integer gruppenGroesse;
    @NonNull
    @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY, prefix="anmeldezeitraum_")
    private Zeitraum anmeldezeitraum;
    @NonNull
    @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY, prefix="uebungszeitraum_")
    private Zeitraum uebungszeitraum;
    @NonNull
    private AnmeldeTyp anmeldeTyp;
    @NonNull
    private Set<Termin> termin = new HashSet<>();
    @NonNull
    private Set<Zeitslot> zeitslots = new HashSet<>();

}
