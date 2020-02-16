package com.beumuth.math.client.jgraph;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of="id")
public class Element {
    private long id;
    private long a;
    private long b;

    @Override
    public String toString() {
        return "[" + id + "," + a + "," + b + "]";
    }
}
