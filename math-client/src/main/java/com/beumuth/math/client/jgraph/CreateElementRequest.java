package com.beumuth.math.client.jgraph;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of={"a", "b"})
public class CreateElementRequest {
    /**
     * >0   ==> a will be set as-is
     * <=0  ==> a will be set to the id of the -ath Element in the List.
     */
    private long a;
    /**
     * >0   ==> b will be set as-is
     * <=0  ==> b will be set to the id of the -bth Element in the List.
     */
    private long b;
}
