package com.beumuth.math.core.settheory.orderedpair;

import com.github.instantpudd.validator.ClientErrorException;
import com.github.instantpudd.validator.ClientErrorStatusCode;
import com.github.instantpudd.validator.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderedPairValidationService {
    @Autowired
    private OrderedPairService orderedPairService;

    public void validateOrderedPairExists(long id, ClientErrorStatusCode statusCode, String message)
        throws ClientErrorException {
        Validator
            .returnStatus(statusCode)
            .ifFalse(orderedPairService.doesOrderedPairExist(id))
            .withErrorMessage(message)
            .execute();
    }
}
