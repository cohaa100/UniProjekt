package propraganda.praktikum.logic.aggregate.uebung;

import lombok.Data;
import propraganda.praktikum.logic.aggregate.sterotypes.AggregatEntity;

@AggregatEntity
@Data
class Repo {
    private String url;
    private String name;
}
