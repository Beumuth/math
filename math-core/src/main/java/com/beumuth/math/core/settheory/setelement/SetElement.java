package com.beumuth.math.core.settheory.setelement;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of="id")
public class SetElement {
    private long id;
    private long idSet;
    private long idElement;
}
