package com.beumuth.math.core.settheory.object;

import com.beumuth.math.core.settheory.object.ObjectService;
import com.github.instantpudd.validator.ClientErrorException;
import com.github.instantpudd.validator.ClientErrorStatusCode;
import com.github.instantpudd.validator.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ObjectValidationService {

    @Autowired
    private ObjectService objectService;

    public void validateObjectExists(long id, ClientErrorStatusCode clientErrorStatusCode, String message)
        throws ClientErrorException {
        Validator
            .returnStatus(clientErrorStatusCode)
            .ifFalse(objectService.doesObjectExist(id))
            .withErrorMessage(message)
            .execute();
    }
}
