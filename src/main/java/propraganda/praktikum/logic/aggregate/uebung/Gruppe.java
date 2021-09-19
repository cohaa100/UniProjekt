package propraganda.praktikum.logic.aggregate.uebung;

import lombok.Data;
import org.springframework.data.annotation.Id;
import propraganda.praktikum.logic.aggregate.sterotypes.AggregatEntity;
import java.util.HashSet;
import java.util.Set;

@AggregatEntity
@Data
public class Gruppe {

    @Id
    private long gruppenId;

    private String name;

    private Set<Long> mitglieder = new HashSet<>(10);

    void addToGruppe(final long benutzerId) {
        mitglieder.add(benutzerId);
    }

    void removeFromGruppe(final long benutzerId) {
        mitglieder.remove(benutzerId);
    }
}
