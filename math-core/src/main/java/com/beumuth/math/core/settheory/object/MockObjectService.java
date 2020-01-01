package com.beumuth.math.core.settheory.object;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MockObjectService {
    @Autowired
    private ObjectService objectService;

    public long valid() {
        return objectService.createObject();
    }

    public List<Long> validMultiple(int number) {
        return objectService.createMultipleObjects(number);
    }

    public long nonexistent() {
        long id = objectService.createObject();
        objectService.deleteObject(id);
        return id;
    }

    public void deleteMock(long id) {
        objectService.deleteObject(id);
    }
}
