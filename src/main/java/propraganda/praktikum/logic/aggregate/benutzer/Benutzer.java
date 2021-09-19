package propraganda.praktikum.logic.aggregate.benutzer;

import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import propraganda.praktikum.logic.aggregate.sterotypes.AggregatRoot;

@AggregatRoot
@Data
public class Benutzer {

    @Id
    private long benutzerId;

    private final long gitHubUserId;

    private final String gitHubName;
    @NonNull
    private BenutzerTyp benutzerTyp;

}