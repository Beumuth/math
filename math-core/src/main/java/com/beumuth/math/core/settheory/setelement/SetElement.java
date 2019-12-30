package com.beumuth.math.core.settheory.setelement;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of="id")
public class SetElement {
    @Getter
    @Setter
    private long id;

    @Getter
    @Setter
    private long idSet;

    @Getter
    @Setter
    private long idElement;
}
