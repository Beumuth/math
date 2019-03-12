package com.beumuth.collections.core.setelement;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of={"idSet", "idElement"})
public class CreateSetElementRequest {
    @Getter
    @Setter
    private long idSet;

    @Getter
    @Setter
    private long idElement;
}
