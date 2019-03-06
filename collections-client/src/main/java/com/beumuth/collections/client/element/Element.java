package com.beumuth.collections.client.element;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of="id")
public class Element {
    @Getter
    @Setter
    private long id;
}
