package com.beumuth.math.core.settheory.element;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of={"idSet", "idObject"})
public class CreateElementRequest {
    @Getter
    @Setter
    private long idSet;

    @Getter
    @Setter
    private long idObject;
}
