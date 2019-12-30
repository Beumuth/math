package com.beumuth.math.client.settheory.element;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of="id")
public class Element {
    @Getter
    @Setter
    private long id;
}
