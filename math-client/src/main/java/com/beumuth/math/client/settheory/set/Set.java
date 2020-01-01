package com.beumuth.math.client.settheory.set;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of="id")
public class Set {
    private long id;
    private long idObject;
}
