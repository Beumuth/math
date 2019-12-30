package com.beumuth.math.client.settheory.set;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of="id")
public class Set {
    @Getter
    @Setter
    private long id;

    @Getter
    @Setter
    private long idElement;
}
