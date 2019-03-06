package com.beumuth.collections.client.set;

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
