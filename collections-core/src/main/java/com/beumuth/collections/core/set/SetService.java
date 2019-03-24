package com.beumuth.collections.core.set;

import com.beumuth.collections.client.set.Set;
import com.beumuth.collections.core.database.CollectionsBeanPropertyRowMapper;
import com.beumuth.collections.core.database.DatabaseService;
import com.beumuth.collections.core.element.ElementService;
import com.beumuth.collections.core.setelement.CreateSetElementRequest;
import com.beumuth.collections.core.setelement.SetElementService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SetService {
    private static final CollectionsBeanPropertyRowMapper<Set> ROW_MAPPER =
        CollectionsBeanPropertyRowMapper.newInstance(Set.class);

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private SetElementService setElementService;

    @Autowired
    private ElementService elementService;

    public boolean doesSetExist(long id) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                "SELECT 1 FROM Sset WHERE id=:id",
                    ImmutableMap.of("id", id),
                    Boolean.class
                );
        } catch(EmptyResultDataAccessException e) {
            return false;
        }
    }

    public Optional<Set> getSet(long id) {
        try {
            return Optional.of (
                databaseService
                    .getNamedParameterJdbcTemplate()
                    .queryForObject(
                    "SELECT id, idElement FROM Sset WHERE id=:id",
                        ImmutableMap.of("id", id),
                        ROW_MAPPER
                    )
            );
        } catch(EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public java.util.Set<Long> getSetsThatDoNotExist(java.util.Set<Long> idSets) {
        return Sets.difference(
            idSets,
            Sets.newHashSet(
                databaseService
                    .getNamedParameterJdbcTemplate()
                    .queryForList(
                        "SELECT id FROM Sset WHERE id IN (:idSets)",
                        ImmutableMap.of(
                        "idSets", idSets
                        ),
                        Long.class
                    )
            )
        );
    }

    public java.util.Set<Long> getSetElements(long idSet) {
        try {
            return Sets.newHashSet(
                databaseService
                    .getNamedParameterJdbcTemplate()
                    .queryForList(
                        "SELECT idElement FROM SetElement WHERE idSet=:idSet",
                        ImmutableMap.of("idSet", idSet),
                        Long.class
                    )
            );
        } catch(EmptyResultDataAccessException e) {
            throw new RuntimeException("Set with given idSet [" + idSet + "] does not exist");
        }
    }

    public long createSet() {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        databaseService
            .getNamedParameterJdbcTemplate()
            .update(
                "INSERT INTO Sset (idElement) VALUES (:idElement)",
                    new MapSqlParameterSource(
                        ImmutableMap.of(
                            "idElement", elementService.createElement()
                        )
                    ),
                    keyHolder
            );
        return keyHolder.getKey().longValue();
    }

    public void deleteSet(long id) {
        //Delete both the Set and the Set's Element at the same time - cascading delete.
        databaseService
            .getNamedParameterJdbcTemplate()
            .update(
            "DELETE FROM Element WHERE id=( " +
                    "SELECT idElement FROM Sset WHERE id=:id" +
                ")",
                ImmutableMap.of("id", id)
            );
    }

    /**
     * Synonym for createSet
     * @return The id of the created Set
     */
    public long createEmptySet() {
        return createSet();
    }

    /**
     * Create a Set with the given Elements.
     * @param idElements The ids of the Elements that are in the Set.
     * @return The id of the created Set.
     */
    public long createSetWithElements(java.util.Set<Long> idElements) {
        if(idElements.isEmpty()) {
            return createEmptySet();
        }

        long idSet = createSet();
        setElementService.createSetElements(
            idElements
                .stream()
                .map(idElement -> new CreateSetElementRequest(idSet, idElement))
                .collect(Collectors.toSet())
        );
        return idSet;
    }

    /**
     * Create a new Set with the same Elements as another Set.
     * @param idSet The Set to copy.
     * @return The id of the created Set.
     */
    public long copySet(long idSet) {
        return createSetWithElements(
            getSetElements(idSet)
        );
    }

    /**
     * Determines if a Set contains an Element
     * @param idSet
     * @param idElement
     * @return
     */
    public boolean containsElement(long idSet, long idElement) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                "SELECT 1 FROM SetElement WHERE idSet=:idSet AND idElement=:idElement",
                    ImmutableMap.of(
                    "idSet", idSet,
                    "idElement", idElement
                    ),
                    Boolean.class
                );
        } catch(EmptyResultDataAccessException e) {
            return false;
        }
    }

    /**
     * Determine if a Set contains all elements.
     * @param idSet
     * @param idElements
     * @return
     */
    public boolean containsAllElements(long idSet, java.util.Set<Long> idElements) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForObject(
                "SELECT COUNT(idElement)=:numElements " +
                    "FROM SetElement " +
                    "WHERE " +
                        "idSet=:idSet AND " +
                        "idElement IN (:idElements)",
                    ImmutableMap.of(
                        "idSet", idSet,
                        "idElements", idElements,
                        "numElements", idElements.size()
                    ),
                Boolean.class
            );
    }

    /**
     * Add an Element to a Set
     * @param idSet
     * @param idElement
     */
    public void addElementToSet(long idSet, long idElement) {
        //Do nothing if the set already contains the element
        if(containsElement(idSet, idElement)) {
            return;
        }

        setElementService.createSetElement(
            new CreateSetElementRequest(idSet, idElement)
        );
    }

    /**
     * Create an Element and add it to a Set
     * @param idSet
     * @return The id of the created Element
     */
    public long createAndAddElement(long idSet) {
        long idElement = elementService.createElement();
        addElementToSet(idSet, idElement);
        return idElement;
    }

    /**
     * Remove an Element from a Set
     * @param idSet
     * @param idElement
     */
    public void removeElementFromSet(long idSet, long idElement) {
        databaseService
            .getNamedParameterJdbcTemplate()
            .update(
            "DELETE FROM SetElement WHERE idSet=:idSet AND idElement=:idElement",
                ImmutableMap.of(
                "idSet", idSet,
                "idElement", idElement
                )
            );
    }

    /**
     * Determine if two sets are equal. That is, if they have exactly the same elements.
     * @param idSetA
     * @param idSetB
     * @return
     */
    public boolean areEqual(long idSetA, long idSetB) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                    "SELECT " +
                            "CASE " +
                                "WHEN " +
                                    "countSetAElements = countSetBElements AND " +
                                    "countSetAElements = countUnionElements " +
                                "THEN 1 " +
                                "ELSE 0 " +
                                "END " +
                        "FROM (" +
                            "SELECT " +
                                "(SELECT COUNT(1) FROM SetElement WHERE idSet=:idSetA) AS countSetAElements, " +
                                "(SELECT COUNT(1) FROM SetElement WHERE idSet=:idSetB) AS countSetBElements, " +
                                "(" +
                                    "SELECT COUNT(1) FROM (" +
                                        "SELECT DISTINCT idElement " +
                                        "FROM SetElement " +
                                        "WHERE idSet=:idSetA OR idSet=:idSetB" +
                                    ") AS unioned" +
                                ") AS countUnionElements" +
                        ") AS isEqual",
                    ImmutableMap.of(
                    "idSetA", idSetA,
                    "idSetB", idSetB
                    ),
                    Boolean.class
                );
        } catch(EmptyResultDataAccessException e) {
            return true;
        }
    }

    /**
     * Determine if a Set A is a subset of Set B
     * @param idPossibleSuperset The potential superset
     * @param idPossibleSubset The potential subset
     * @return
     */
    public boolean isSubset(long idPossibleSubset, long idPossibleSuperset) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForObject(
            "SELECT COUNT(1)=0 FROM ( " +
                    "SELECT idElement " +
                    "FROM SetElement " +
                    "WHERE " +
                        "idSet=:idPossibleSubset AND " +
                        "idElement NOT IN (" +
                            "SELECT idElement FROM SetElement WHERE idSet=:idPossibleSuperset" +
                        ")" +
                ") AS elementsInSubsetNotInSuperset",
                ImmutableMap.of(
                "idPossibleSuperset", idPossibleSuperset,
                "idPossibleSubset", idPossibleSubset
                ),
                Boolean.class
            );
    }

    /**
     * Determine the number of elements in a Set.
     * @param idSet
     * @return The cardinality
     */
    public int cardinality(long idSet) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForObject(
            "SELECT COUNT(1) FROM SetElement WHERE idSet=:idSet",
                ImmutableMap.of("idSet", idSet),
                Integer.class
            );
    }

    /**
     * Determine the number of elements in a Set. Synonym for cardinality.
     * @param idSet The set
     * @return
     */
    public int size(long idSet) {
        return cardinality(idSet);
    }

    /**
     * Determine if a Set is empty.
     * @param idSet
     * @return
     */
    public boolean isEmpty(long idSet) {
        return cardinality(idSet) == 0;
    }

    /**
     * Create a new Set that is the intersection of two other Sets.
     * @param idSetA
     * @param idSetB
     * @return The id of the new Set.
     */
    public long intersection(long idSetA, long idSetB) {
        return createSetWithElements(
            Sets.newHashSet(
                databaseService
                    .getNamedParameterJdbcTemplate()
                    .queryForList(
                    "SELECT DISTINCT idElement FROM ( " +
                            "SELECT " +
                                "idElement " +
                            "FROM " +
                                "SetElement " +
                            "WHERE " +
                                "idSet=:idSetA" +
                        ") AS setAElements INNER JOIN (" +
                            "SELECT " +
                                "idElement " +
                            "FROM " +
                                "SetElement " +
                            "WHERE " +
                                "idSet=:idSetB" +
                        ") AS setBElements " +
                            "USING(idElement)",
                        ImmutableMap.of(
                            "idSetA", idSetA,
                            "idSetB", idSetB
                        ),
                        Long.class
                    )
            )
        );
    }

    /**
     * Create a new Set that is the intersection of multiple other Sets.
     * @param idSets
     * @return The id of the new Set.
     */
    public long intersectionMultiple(java.util.Set<Long> idSets) {
        return createSetWithElements(
            Sets.newHashSet(
                databaseService
                    .getNamedParameterJdbcTemplate()
                    .queryForList(
                    "SELECT idElement " +
                        "FROM SetElement " +
                        "WHERE idSet IN (:idSets) " +
                        "GROUP BY idElement " +
                        "HAVING COUNT(1)=" + idSets.size(),
                        ImmutableMap.of("idSets", idSets),
                        Long.class
                    )
            )
        );
    }

    /**
     * Create a new Set that is the union of two other Sets.
     * @param idSetA
     * @param idSetB
     * @return The id of the new Set.
     */
    public long union(long idSetA, long idSetB) {
        return createSetWithElements(
            Sets.newHashSet(
                databaseService
                    .getNamedParameterJdbcTemplate()
                    .queryForList(
                    "SELECT " +
                            "DISTINCT idElement " +
                        "FROM " +
                            "SetElement " +
                        "WHERE " +
                            "idSet=:idSetA OR " +
                            "idSet=:idSetB",
                        ImmutableMap.of(
                        "idSetA", idSetA,
                        "idSetB", idSetB
                        ),
                        Long.class
                    )
            )
        );
    }

    /**
     * Create a Set that is the union of multiple other Sets.
     * @param idSets
     * @return The id of the new Set.
     */
    public long unionMultiple(java.util.Set<Long> idSets) {
        return createSetWithElements(
            Sets.newHashSet(
                databaseService
                    .getNamedParameterJdbcTemplate()
                    .queryForList(
                    "SELECT " +
                            "DISTINCT idElement " +
                        "FROM " +
                            "SetElement " +
                        "WHERE " +
                            "idSet IN (:idSets) ",
                        ImmutableMap.of("idSets", idSets),
                        Long.class
                    )
            )
        );
    }

    /**
     * Create a new Set that is A-B
     * @param idSetA
     * @param idSetB
     * @return The id of the new Set.
     */
    public long difference(long idSetA, long idSetB) {
        return createSetWithElements(
            Sets.newHashSet(
                databaseService
                    .getNamedParameterJdbcTemplate()
                    .queryForList(
                    "SELECT DISTINCT idElement FROM ( " +
                            "SELECT " +
                                "idElement " +
                            "FROM " +
                                "SetElement " +
                            "WHERE " +
                                "idSet=:idSetA " +
                        ") AS setAElements " +
                        "WHERE setAElements.idElement NOT IN ( " +
                            "SELECT " +
                                "idElement " +
                            "FROM " +
                                "SetElement " +
                            "WHERE " +
                                "idSet=:idSetB" +
                        ")",
                        ImmutableMap.of(
                            "idSetA", idSetA,
                            "idSetB", idSetB
                        ),
                        Long.class
                    )
            )
        );
    }

    /**
     * Create a Set that is the symmetric difference of A and B.
     * @param idSetA
     * @param idSetB
     * @return The id of the new Set.
     */
    public long symmetricDifference(long idSetA, long idSetB) {
        return createSetWithElements(
            Sets.newHashSet(
                databaseService
                    .getNamedParameterJdbcTemplate()
                    .queryForList(
                    "SELECT idElement " +
                        "FROM SetElement " +
                        "WHERE idSet IN(:idSetA, :idSetB) " +
                        "GROUP BY idElement " +
                        "HAVING COUNT(1)=1",
                        ImmutableMap.of(
                        "idSetA", idSetA,
                        "idSetB", idSetB
                        ),
                        Long.class
                    )
            )
        );
    }

    /**
     * Create a Set that is the Cartesian product of A and B
     * @param idSetA
     * @param idSetB
     * @return The id of the new Set.
     */
    public long cartesianProuct(long idSetA, long idSetB) {
        //TODO once Lists exist
        throw new UnsupportedOperationException("cartesianProduct is not yet implemented");
    }

    /**
     * Create a Set that is the n-ary Cartesian product of the given Sets.
     * @param idSets
     * @return The id of the new Set.
     */
    public long nAryCartesianProduct(java.util.Set<Long> idSets) {
        //TODO once Lists exist
        throw new UnsupportedOperationException("nAryCartesianProduct is not yet implemented");
    }

    /**
     * Create a Set that is A^n - that is, the n-ary Cartesian product of the given Set with itself.
     * @param idSet
     * @param n
     * @return
     */
    public long nAryCartesianPower(int idSet, int n) {
        //TODO once Lists exist
        throw new UnsupportedOperationException("nAryCartesianPower is not yet implemented");
    }
}
