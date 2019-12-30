package com.beumuth.math.core.settheory.set;

import com.beumuth.math.client.settheory.set.Set;
import com.beumuth.math.core.settheory.element.ElementService;
import com.github.instantpudd.validator.ClientErrorException;
import com.github.instantpudd.validator.ClientErrorStatusCode;
import com.github.instantpudd.validator.Validator;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping(path="api/sets")
public class SetController {
    @Autowired
    private SetService setService;
    @Autowired
    private ElementService elementService;

    @GetMapping(path="/set/{id}/exists")
    @ResponseBody
    public boolean doesSetExist(@PathVariable(name="id") long id) {
        return setService.doesSetExist(id);
    }

    @GetMapping(path="/set/{id}")
    @ResponseBody
    public Set getSet(@PathVariable(name="id") long id) throws ClientErrorException {
        Optional<Set> set = setService.getSet(id);
        validateIfSetExists(id, "Set with given id [" + id + "] does not exist");
        return set.get();
    }

    @GetMapping(path="/set/{id}/elements")
    @ResponseBody
    public java.util.Set<Long> getSetElements(@PathVariable(name="id") long id) throws ClientErrorException {
        validateIfSetExists(id, "Set with given id [" + id + "] does not exist");
        return setService.getSetElements(id);
    }

    @PostMapping(path="/set")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public long createSet() {
        return setService.createSet();
    }

    @PostMapping(path="/set/withElements")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public long createSetWithElements(@RequestBody java.util.Set<Long> idElements) throws ClientErrorException {
        validateIfElementsExist(idElements, ClientErrorStatusCode.STATUS_400);
        return setService.createSetWithElements(idElements);
    }

    @PostMapping(path="/set/{id}/copy")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public long copySet(@PathVariable("id") long id) throws ClientErrorException {
        validateIfSetExists(id, "Set with given id [" + id + "] does not exist");
        return setService.copySet(id);
    }

    @DeleteMapping(path="/set/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSet(@PathVariable(name="id") long id) {
        setService.deleteSet(id);
    }


    @GetMapping(path="/set/{idSet}/containsElement/{idElement}")
    @ResponseBody
    public boolean doesSetContainElement(
        @PathVariable(name="idSet") long idSet,
        @PathVariable(name="idElement") long idElement
    ) throws ClientErrorException {
        validateIfSetExists(idSet, "Set with given id [" + idSet + "] does not exist");
        validateIfElementExists(idElement, "Element with given id [" + idElement + "] does not exist");
        return setService.containsElement(idSet, idElement);
    }


    @GetMapping(path="/set/{idSet}/containsElements")
    @ResponseBody
    public boolean doesSetContainElements(
        @PathVariable(name="idSet") long idSet,
        @RequestParam(name="idElements") java.util.Set<Long> idElements
    ) throws ClientErrorException {
        validateIfSetExists(idSet, "Set with given id [" + idSet + "] does not exist");
        validateIfElementsExist(idElements, ClientErrorStatusCode.NOT_FOUND);
        return setService.containsAllElements(idSet, idElements);
    }

    @PutMapping(path="/set/{idSet}/element/{idElement}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addElementToSet(
        @PathVariable("idSet") long idSet,
        @PathVariable("idElement") long idElement
    ) throws ClientErrorException {
        validateIfSetExists(idSet, "Set with given id [" + idSet + "] does not exist");
        validateIfElementExists(idElement, "Element with given id [" + idElement + "] does not exist");
        setService.addElementToSet(idSet, idElement);
    }

    @PostMapping(path="/set/{idSet}/element")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public long createAndAddElementToSet(@PathVariable("idSet") long idSet) throws ClientErrorException {
        validateIfSetExists(idSet, "Set with given id [" + idSet + "] does not exist");
        return setService.createAndAddElement(idSet);
    }

    @DeleteMapping(path="/set/{idSet}/element/{idElement}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeElementFromSet(
        @PathVariable("idSet") long idSet,
        @PathVariable("idElement") long idElement
    ) throws ClientErrorException {
        validateIfSetExists(idSet, "Set with given id [" + idSet + "] does not exist");
        validateIfElementExists(idElement, "Element with given id [" + idElement + "] does not exist");
        setService.removeElementFromSet(idSet, idElement);
    }

    @GetMapping(path="/set/{idSetA}/equals/{idSetB}")
    @ResponseBody
    public boolean areEqual(
        @PathVariable("idSetA") long idSetA,
        @PathVariable("idSetB") long idSetB
    ) throws ClientErrorException {
        validateIfSetExists(idSetA, "Set with given id [" + idSetA + "] does not exist");
        validateIfSetExists(idSetB, "Set with given id [" + idSetB + "] does not exist");
        return setService.areEqual(idSetA, idSetB);
    }

    @GetMapping(path="/set/{idSetA}/isSubset/{idSetB}")
    @ResponseBody
    public boolean isSubset(
        @PathVariable("idSetA") long idSetA,
        @PathVariable("idSetB") long idSetB
    ) throws ClientErrorException {
        validateIfSetExists(idSetA, "Set with given id [" + idSetA + "] does not exist");
        validateIfSetExists(idSetB, "Set with given id [" + idSetB + "] does not exist");
        return setService.isSubset(idSetA, idSetB);
    }

    @GetMapping(path="/set/{idSetA}/isDisjoint/{idSetB}")
    @ResponseBody
    public boolean areDisjoint(
        @PathVariable("idSetA") long idSetA,
        @PathVariable("idSetB") long idSetB
    ) throws ClientErrorException {
        validateIfSetExists(idSetA, "Set with given id [" + idSetA + "] does not exist");
        validateIfSetExists(idSetB, "Set with given id [" + idSetB + "] does not exist");
        return setService.areDisjoint(idSetA, idSetB);
    }

    @GetMapping(path="/areDisjoint")
    @ResponseBody
    public boolean areDisjointMultiple(
        @RequestParam("idSets") java.util.Set<Long> idSets
    ) throws ClientErrorException {
        Validator
            .returnStatus(ClientErrorStatusCode.BAD_REQUEST)
            .ifTrue(idSets.size() < 2)
            .withErrorMessage("idSets must contain at least two ids")
            .execute();
        validateIfSetsExist(idSets);
        return setService.areDisjointMultiple(idSets);
    }

    @GetMapping(path="/set/{idSet}/isPartition")
    @ResponseBody
    public boolean isPartition(
        @PathVariable("idSet") long idSet,
        @RequestParam("candidatePartition") java.util.Set<Long> candidatePartition
    ) throws ClientErrorException {
        validateIfSetExists(idSet, "Set with given id [" + idSet + "] does not exist");
        Validator
            .returnStatus(ClientErrorStatusCode.BAD_REQUEST)
            .ifTrue(candidatePartition.isEmpty())
            .withErrorMessage("candidatePartition cannot be empty")
            .execute();
        validateIfSetsExist(candidatePartition);

        return setService.isPartition(candidatePartition, idSet);
    }

    @GetMapping(path="/set/{id}/size")
    @ResponseBody
    public int setCardinality(@PathVariable("id") long id) throws ClientErrorException {
        validateIfSetExists(id, "Set with given id [" + id + "] does not exist");
        return setService.cardinality(id);
    }

    @GetMapping(path="/set/{id}/isEmpty")
    @ResponseBody
    public boolean isSetEmpty(@PathVariable("id") long id) throws ClientErrorException {
        validateIfSetExists(id, "Set with given id [" + id + "] does not exist");
        return setService.isEmpty(id);
    }

    @PostMapping(path="/set/{idSetA}/intersect/{idSetB}")
    @ResponseBody
    public long intersection(
        @PathVariable("idSetA") long idSetA,
        @PathVariable("idSetB") long idSetB
    ) throws ClientErrorException {
        validateIfSetExists(idSetA, "Set with given id [" + idSetA + "] does not exist");
        validateIfSetExists(idSetB, "Set with given id [" + idSetB + "] does not exist");
        return setService.intersection(idSetA, idSetB);
    }

    @PostMapping(path="/intersect")
    @ResponseBody
    public long intersectMultipleSets(
        @RequestBody java.util.Set<Long> idSets
    ) throws ClientErrorException {
        validateIfSetsExist(idSets);
        return setService.intersectionMultiple(idSets);
    }

    @PostMapping(path="/set/{idSetA}/union/{idSetB}")
    @ResponseBody
    public long union(
        @PathVariable("idSetA") long idSetA,
        @PathVariable("idSetB") long idSetB
    ) throws ClientErrorException {
        validateIfSetExists(idSetA, "Set with given id [" + idSetA + "] does not exist");
        validateIfSetExists(idSetB, "Set with given id [" + idSetB + "] does not exist");
        return setService.union(idSetA, idSetB);
    }

    @PostMapping(path="/union")
    @ResponseBody
    public long unionMultipleSets(@RequestBody java.util.Set<Long> idSets) throws ClientErrorException {
        validateIfSetsExist(idSets);
        return setService.unionMultiple(idSets);
    }

    @PostMapping(path="/set/{idSetA}/subtract/{idSetB}")
    @ResponseBody
    public long difference(
        @PathVariable("idSetA") long idSetA,
        @PathVariable("idSetB") long idSetB
    ) throws ClientErrorException {
        validateIfSetExists(idSetA, "Set with given id [" + idSetA + "] does not exist");
        validateIfSetExists(idSetB, "Set with given id [" + idSetB + "] does not exist");
        return setService.difference(idSetA, idSetB);
    }

    @PostMapping(path="/set/{idSet}/complement/{idUniversalSet}")
    @ResponseBody
    public long complement(
        @PathVariable("idSet") long idSet,
        @PathVariable("idUniversalSet") long idUniversalSet
    ) throws ClientErrorException {
        validateIfSetExists(idSet, "Set with given id [" + idSet + "] does not exist");
        validateIfSetExists(idUniversalSet, "Set with given id [" + idUniversalSet + "] does not exist");
        Validator
            .returnStatus(ClientErrorStatusCode.BAD_REQUEST)
            .ifFalse(setService.isSubset(idSet, idUniversalSet))
            .withErrorMessage(
                "The given set [" + idSet + "] is not a subset of the given universal set [ " + idUniversalSet + "]"
            ).execute();
        return setService.complement(idSet, idUniversalSet);
    }

    @PostMapping(path="/set/{idSetA}/symmetricDifference/{idSetB}")
    @ResponseBody
    public long symmetricDifference(
        @PathVariable("idSetA") long idSetA,
        @PathVariable("idSetB") long idSetB
    ) throws ClientErrorException {
        validateIfSetExists(idSetA, "Set with given id [" + idSetA + "] does not exist");
        validateIfSetExists(idSetB, "Set with given id [" + idSetB + "] does not exist");
        return setService.symmetricDifference(idSetA, idSetB);
    }

    private void validateIfSetExists(long idSet, String errorMessage) throws ClientErrorException {
        Validator
            .returnStatus(ClientErrorStatusCode.NOT_FOUND)
            .ifFalse(setService.doesSetExist(idSet))
            .withErrorMessage(errorMessage)
            .execute();
    }

    private void validateIfSetsExist(java.util.Set<Long> idSets) throws ClientErrorException {
        java.util.Set<Long> idSetsThatDoNotExist = setService.getSetsThatDoNotExist(idSets);
        Validator
            .returnStatus(ClientErrorStatusCode.STATUS_400)
            .ifFalse(idSetsThatDoNotExist.isEmpty())
            .withErrorMessage(
                "The Sets with the following ids do not exist: " + StringUtils.join(idSetsThatDoNotExist)
            ).execute();
    }

    private void validateIfElementExists(long idElement, String errorMessage) throws ClientErrorException {
        Validator
            .returnStatus(ClientErrorStatusCode.NOT_FOUND)
            .ifFalse(elementService.doesElementExist(idElement))
            .withErrorMessage(errorMessage)
            .execute();
    }

    private void validateIfElementsExist(java.util.Set<Long> idElements, ClientErrorStatusCode statusCode)
        throws ClientErrorException {
        java.util.Set<Long> idElementsThatDoNotExist = elementService.getElementsThatDoNotExist(idElements);
        Validator
            .returnStatus(statusCode)
            .ifFalse(idElementsThatDoNotExist.isEmpty())
            .withErrorMessage("The following elements do not exist: " + StringUtils.join(idElementsThatDoNotExist))
            .execute();
    }
}