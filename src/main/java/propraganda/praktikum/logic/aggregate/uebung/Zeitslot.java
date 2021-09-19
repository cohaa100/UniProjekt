package propraganda.praktikum.logic.aggregate.uebung;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Zeitslot {

    @Id
    @EqualsAndHashCode.Include
    private long zeitslotId;
    @NonNull
    private List<Long> tutorId = new ArrayList<>();
    private final LocalDateTime datum;
    @NonNull
    private int groesse;


}
