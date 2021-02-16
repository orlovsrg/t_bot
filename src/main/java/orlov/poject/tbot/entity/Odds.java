package orlov.poject.tbot.entity;

import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class Odds {
    private Double point;
    private Double coefficient;
}
