package com.beumuth.math.core.settheory.element;

import lombok.*;

/**
 * An Element is the instance of an Object in a Set.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of="id")
public class Element {
    private long id;
    private long idObject;
    private long idSet;
}