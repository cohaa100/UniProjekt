package propraganda.praktikum.logic.aggregate.uebung;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.lang.Nullable;
import propraganda.praktikum.logic.aggregate.sterotypes.AggregatEntity;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@AggregatEntity
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Termin {

    @Id
    @EqualsAndHashCode.Include
    private long terminId;

    private boolean belegt;

    @NonNull
    private LocalDateTime datum;

    @Nullable
    private Long tutor;
    @Builder.Default
    private Gruppe gruppe = new Gruppe();

    public void setMitglieder(final Set<Long> mitglieder) {
        gruppe.setMitglieder(mitglieder);
    }

    public void addGruppenMitglied(final long benutzerId) {
        gruppe.addToGruppe(benutzerId);
    }

    public void removeGruppenMitglied(final long benutzerId) {
        gruppe.removeFromGruppe(benutzerId);
    }

    public void setGruppenName(final String gruppenName) {
        gruppe.setName(gruppenName);
    }

    public Set<Long> getMitglieder() {
        return gruppe.getMitglieder();
    }

    public String getGruppenName() {
        return gruppe.getName();
    }
}
