package com.beumuth.math.core.settheory.set;

import com.beumuth.math.client.settheory.set.Set;
import com.beumuth.math.core.internal.database.MathBeanPropertyRowMapper;
import com.beumuth.math.core.internal.database.DatabaseService;
import com.beumuth.math.core.settheory.object.ObjectService;
import com.beumuth.math.core.settheory.element.CreateElementRequest;
import com.beumuth.math.core.settheory.element.ElementService;
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
    private static final MathBeanPropertyRowMapper<Set> ROW_MAPPER =
        MathBeanPropertyRowMapper.newInstance(Set.class);

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private ElementService elementService;

    @Autowired
    private ObjectService objectService;

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
                    "SELECT id, idObject FROM Sset WHERE id=:id",
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
                        ImmutableMap.of("idSets", idSets),
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
                        "SELECT idObject FROM Element WHERE idSet=:idSet",
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
                "INSERT INTO Sset (idObject) VALUES (:idObject)",
                    new MapSqlParameterSource(
                        ImmutableMap.of(
                            "idObject", objectService.createObject()
                        )
                    ),
                    keyHolder
            );
        return keyHolder.getKey().longValue();
    }

    public void deleteSet(long id) {
        //Delete both the Set and the Set's Object at the same time - cascading delete.
        databaseService
            .getNamedParameterJdbcTemplate()
            .update(
                "DELETE FROM Object WHERE id = ( " +
                    "SELECT idObject FROM Sset WHERE id=:id" +
                ")",
                ImmutableMap.of("id", id)
            );
    }

    /**
     * Synonym for create
     * @return The id of the created Set
     */
    public long createEmptySet() {
        return createSet();
    }

    /**
     * Create a Set and add the given Objects.
     * @param idObjects The ids of the Objects that are to become Elements in the Set.
     * @return The id of the created Set.
     */
    public long createSetWithElements(java.util.Set<Long> idObjects) {
        if(idObjects.isEmpty()) {
            return createEmptySet();
        }

        long idSet = createSet();
        elementService.createElements(
            idObjects
                .stream()
                .map(idElement -> new CreateElementRequest(idSet, idElement))
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
     * Determines if a Set contains an Object
     * @param idSet
     * @param idObject
     * @return
     */
    public boolean containsObject(long idSet, long idObject) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                "SELECT 1 FROM Element WHERE idSet=:idSet AND idObject=:idObject",
                    ImmutableMap.of(
                        "idSet", idSet,
                        "idObject", idObject
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
     * @param idObjects
     * @return
     */
    public boolean containsAllObjects(long idSet, java.util.Set<Long> idObjects) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForObject(
                "SELECT COUNT(idObject)=:numObjects " +
                    "FROM Element " +
                    "WHERE " +
                        "idSet=:idSet AND " +
                        "idObject IN (:idObjects)",
                    ImmutableMap.of(
                        "idSet", idSet,
                        "idObjects", idObjects,
                        "numObjects", idObjects.size()
                    ),
                Boolean.class
            );
    }

    /**
     * Add an Object to a Set
     * @param idSet
     * @param idObject
     */
    public void addObjectToSet(long idSet, long idObject) {
        //Do nothing if the set already contains the object
        if(containsObject(idSet, idObject)) {
            return;
        }

        elementService.createElement(
            new CreateElementRequest(idSet, idObject)
        );
    }

    /**
     * Create an Object and add it to a Set
     * @param idSet
     * @return The id of the created Object
     */
    public long createAndAddObject(long idSet) {
        long idObject = objectService.createObject();
        addObjectToSet(idSet, idObject);
        return idObject;
    }

    /**
     * Remove an Object from a Set
     * @param idSet
     * @param idObject
     */
    public void removeElementFromSet(long idSet, long idObject) {
        databaseService
            .getNamedParameterJdbcTemplate()
            .update(
            "DELETE FROM Element WHERE idSet=:idSet AND idObject=:idObject",
                ImmutableMap.of(
                    "idSet", idSet,
                    "idObject", idObject
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
                            "(SELECT COUNT(1) FROM Element WHERE idSet=:idSetA) AS countSetAElements, " +
                            "(SELECT COUNT(1) FROM Element WHERE idSet=:idSetB) AS countSetBElements, " +
                            "(" +
                                "SELECT COUNT(1) FROM (" +
                                    "SELECT DISTINCT idObject " +
                                    "FROM Element " +
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
                    "SELECT idObject " +
                    "FROM Element " +
                    "WHERE " +
                        "idSet=:idPossibleSubset AND " +
                        "idObject NOT IN (" +
                            "SELECT idObject FROM Element WHERE idSet=:idPossibleSuperset" +
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
     * Determine if A and B are disjoint; that is, if the intersection of A and B is an empty Set.
     * @param idSetA
     * @param idSetB
     * @return True if disjoint, otherwise false.
     */
    public boolean areDisjoint(long idSetA, long idSetB) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForObject(
                "SELECT " +
                    "COUNT(1)=0 " +
                "FROM (" +
                    "SELECT DISTINCT idObject FROM ( " +
                        "SELECT " +
                            "idObject " +
                        "FROM " +
                            "Element " +
                        "WHERE " +
                            "idSet=:idSetA" +
                        ") AS setAElements " +
                    "INNER JOIN (" +
                        "SELECT " +
                            "idObject " +
                        "FROM " +
                            "Element " +
                        "WHERE " +
                            "idSet=:idSetB" +
                    ") AS setBElements " +
                        "USING(idObject) " +
                ") AS elementsInIntersection",
                ImmutableMap.of(
                    "idSetA", idSetA,
                    "idSetB", idSetB
                ),
                Boolean.class
            );
    }

    /**
     * Determine if the collection of Sets are disjoint; that is, if their intersection is an empty Set.
     * @param idSets
     * @return True if disjoint, otherwise false.
     */
    public boolean areDisjointMultiple(java.util.Set<Long> idSets) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForObject(
                "SELECT  " +
                    "COUNT(1) = 0 " +
                "FROM ( " +
                    "SELECT " +
                        "idObject " +
                    "FROM " +
                        "Element " +
                    "WHERE " +
                        "idSet IN (:idSets) " +
                    "GROUP BY  " +
                        "idObject " +
                    "HAVING " +
                        "COUNT(1) > 1 " +
                ") AS idSharedElements",
                ImmutableMap.of("idSets", idSets),
                Boolean.class
            );
    }

    /**
     * Determine if the given set of Sets is a partition of a Set; that is, if they are disjoint and their union equals
     * the set.
     * @param idSet The id of the Set that they may be a partition of.
     * @param candidatePartition The ids of the Sets that may form a partition
     * @return True if a partition, otherwise false.
     */
    public boolean isPartition(java.util.Set<Long> candidatePartition, long idSet) {
        //Does the candidate partition only have one object?
        if(candidatePartition.size() == 1) {
            //Yes. It is a partition iff it is equal to the Set.
            return areEqual(idSet, candidatePartition.stream().findFirst().get());
        }

        //The candidate partition has multiple Sets.
        //Are the multiple Sets disjoint?
        if(! areDisjointMultiple(candidatePartition)) {
            //No. It cannot be a partition.
            return false;
        }

        //It is a partition if the union equals the set
        boolean isPartition = false;
        long idUnion = unionMultiple(candidatePartition);
        if(areEqual(idSet, idUnion)) {
            isPartition = true;
        }
        deleteSet(idUnion);
        return isPartition;
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
            "SELECT COUNT(1) FROM Element WHERE idSet=:idSet",
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
                    "SELECT DISTINCT idObject FROM ( " +
                            "SELECT " +
                                "idObject " +
                            "FROM " +
                                "Element " +
                            "WHERE " +
                                "idSet=:idSetA" +
                        ") AS setAElements INNER JOIN (" +
                            "SELECT " +
                                "idObject " +
                            "FROM " +
                                "Element " +
                            "WHERE " +
                                "idSet=:idSetB" +
                        ") AS setBElements " +
                            "USING(idObject)",
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
                    "SELECT idObject " +
                        "FROM Element " +
                        "WHERE idSet IN (:idSets) " +
                        "GROUP BY idObject " +
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
                            "DISTINCT idObject " +
                        "FROM " +
                            "Element " +
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
                            "DISTINCT idObject " +
                        "FROM " +
                            "Element " +
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
                        "SELECT " +
                            "DISTINCT idObject " +
                        "FROM ( " +
                            "SELECT " +
                                "idObject " +
                            "FROM " +
                                "Element " +
                            "WHERE " +
                                "idSet=:idSetA " +
                        ") AS setAElements " +
                        "WHERE " +
                            "setAElements.idObject NOT IN ( " +
                                "SELECT " +
                                    "idObject " +
                                "FROM " +
                                    "Element " +
                                "WHERE " +
                                    "idSet=:idSetB " +
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
     * Create a Set that is the complement of the given set with respective to the given universal set.
     * That is, the Set that contains all of the Elements in the universal set that are not in the given set.
     * Note that this is a synonym for difference(idUniverse, idSet).
     * @param idSet
     * @param idUniversalSet
     * @return The id of the new Set.
     */
    public long complement(long idSet, long idUniversalSet) {
        return difference(idUniversalSet, idSet);
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
                        "SELECT idObject " +
                        "FROM Element " +
                        "WHERE idSet IN(:idSetA, :idSetB) " +
                        "GROUP BY idObject " +
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
    public long cartesianProduct(long idSetA, long idSetB) {
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
