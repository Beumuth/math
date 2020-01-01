package com.beumuth.math.core.settheory.orderedpair;

import com.beumuth.math.client.settheory.orderedpair.CrupdateOrderedPairRequest;
import com.beumuth.math.client.settheory.orderedpair.OrderedPair;
import com.beumuth.math.core.settheory.object.ObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MockOrderedPairService {
    @Autowired
    private OrderedPairService orderedPairService;

    @Autowired
    private ObjectService objectService;

    public OrderedPair valid() {
        return orderedPairService.getOrderedPair(
            orderedPairService.createOrderedPair(
                new CrupdateOrderedPairRequest(
                    objectService.createObject(),
                    objectService.createObject()
                )
            )
        ).get();
    }

    public OrderedPair withObjects(int idLeft, int idRight) {
        return orderedPairService.getOrderedPair(
            orderedPairService.createOrderedPair(
                new CrupdateOrderedPairRequest(idLeft, idRight)
            )
        ).get();
    }

    /**
     * Note that idLeft and idRight also do not exist.
     * @return
     */
    public OrderedPair nonexistent() {
        OrderedPair orderedPair = orderedPairService.getOrderedPair(
            orderedPairService.createOrderedPair(
                new CrupdateOrderedPairRequest(
                    objectService.createObject(),
                    objectService.createObject()
                )
            )
        ).get();
        deleteMock(orderedPair);
        return orderedPair;
    }

    public void deleteMock(OrderedPair orderedPair) {
        objectService.deleteObject(orderedPair.getIdLeft());
        objectService.deleteObject(orderedPair.getIdRight());
        orderedPairService.deleteOrderedPair(orderedPair.getId());
    }
}
