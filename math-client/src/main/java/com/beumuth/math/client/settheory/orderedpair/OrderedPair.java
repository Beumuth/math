package com.beumuth.math.client.settheory.orderedpair;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of="id")
public class OrderedPair {
    private long id;

    /**
     * The id of the Object for this OrderedPair itself
     */
    private long idObject;

    /**
     * The id of the Object in the first slot of this OrderedPair
     */
    private long idLeft;

    /**
     * The id of the Object in the second slot of this OrderedPair
     */
    private long idRight;
}