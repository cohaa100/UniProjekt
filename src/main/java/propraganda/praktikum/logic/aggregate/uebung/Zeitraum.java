package propraganda.praktikum.logic.aggregate.uebung;

import lombok.NonNull;
import lombok.Value;
import propraganda.praktikum.logic.aggregate.sterotypes.AggregatValue;

import java.time.LocalDateTime;

@AggregatValue
@Value
public class Zeitraum {

    @NonNull
    private LocalDateTime zeitraumStart;
    @NonNull
    private LocalDateTime zeitraumEnde;
}
