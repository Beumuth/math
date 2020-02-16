package com.beumuth.math.core.jgraph.element;

import com.beumuth.math.client.jgraph.CreateElementRequest;
import com.beumuth.math.client.jgraph.Element;
import com.beumuth.math.client.jgraph.UpdateElementRequest;
import com.beumuth.math.client.settheory.orderedset.OrderedSet;
import com.beumuth.math.client.settheory.orderedset.OrderedSets;
import com.beumuth.math.core.internal.database.DatabaseService;
import com.beumuth.math.core.internal.database.MathBeanPropertyRowMapper;
import com.beumuth.math.core.jgraph.component.ComponentService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ElementService {
    private static final MathBeanPropertyRowMapper<Element> ROW_MAPPER =
        MathBeanPropertyRowMapper.newInstance(Element.class);
    private static final ResultSetExtractor<OrderedSet<Long>> ID_ORDERED_SET_EXTRACTOR = rs -> {
        OrderedSet<Long> result = OrderedSets.empty();
        while(rs.next()) {
            result.add(rs.getLong("id"));
        }
        return result;
    };
    private static final ResultSetExtractor<OrderedSet<Element>> ELEMENT_ORDERED_SET_EXTRACTOR = rs -> {
        OrderedSet<Element> result = OrderedSets.empty();
        while(rs.next()) {
            result.add(new Element(rs.getLong("id"), rs.getLong("a"), rs.getLong("b")));
        }
        return result;
    };

    @Autowired
    private ComponentService componentService;

    @Autowired
    private DatabaseService databaseService;

    public boolean doesElementExist(long id) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                    "SELECT 1 FROM JGraphElement WHERE id=:id",
                    ImmutableMap.of("id", id),
                    Boolean.class
                );
        } catch(EmptyResultDataAccessException e) {
            return false;
        }
    }

    public boolean doAnyElementsExist(Set<Long> ids) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                    "SELECT 1 FROM JGraphElement WHERE id IN (:ids)",
                    ImmutableMap.of("ids", ids),
                    Boolean.class
                );
        } catch(EmptyResultDataAccessException e) {
            return false;
        }
    }

    public boolean doAllElementsExist(Set<Long> ids) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForObject(
                "SELECT COUNT(1)=:numIds FROM JGraphElement WHERE id IN (:ids)",
                ImmutableMap.of(
                    "ids", ids,
                    "numIds", ids.size()
                ),
                Boolean.class
            );
    }

    public boolean doesElementWithAOrBExist(long a, long b) {
        return numElementsWithAOrB(a, b) > 0;
    }

    public boolean doesElementExistWithAAndB(long a, long b) {
        return numElementsWithAAndB(a, b) > 0;
    }

    public boolean isElementNode(long idElement) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                    "SELECT a=id AND b=id FROM Element WHERE id=:idElement",
                    ImmutableMap.of("idElement", idElement),
                    Boolean.class
                );
        } catch(EmptyResultDataAccessException e) {
            throw new ElementDoesNotExistException(idElement);
        }
    }

    public OrderedSet<Boolean> areElementsNodes(OrderedSet<Long> idElements) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .query(
                "SELECT " +
                    "ids.column_0 AS id, " +
                    "j.a IS NOT NULL AND j.a=id AND j.b=id AS isNode " +
                "FROM " +
                    "(VALUES " + idElements
                        .stream()
                        .map(id -> "ROW(" + id + ")")
                        .collect(Collectors.joining(",")) +
                    ") ids LEFT JOIN JGraphElement j " +
                        "ON hardcodedNames.column_0 = j.id " +
                "WHERE id IN (:idElements)",
                ImmutableMap.of("idElements", idElements),
                new ResultSetExtractor<OrderedSet<Boolean>>() {
                    @Override
                    public OrderedSet<Boolean> extractData(ResultSet rs)
                        throws SQLException, DataAccessException {
                        
                        OrderedSet<Boolean> result = new OrderedSet();
                        while(rs.next()) {
                            result.add(rs.getBoolean("idNode"));
                        }
                        return result;
                    }
                }
            );
    }

    public boolean isElementPendantFrom(long idElement, long idFrom) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                    "SELECT a=:idFrom AND b=id FROM Element WHERE id=:idElement",
                    ImmutableMap.of(
                        "idElement", idElement,
                        "idFrom", idFrom
                    ),
                    Boolean.class
                );
        } catch(EmptyResultDataAccessException e) {
            throw new ElementDoesNotExistException(idElement);
        }
    }

    public OrderedSet<Boolean> areElementsPendantsFrom(OrderedSet<Long> idElements, long idFrom) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .query(
                "SELECT " +
                    "ids.column_0 AS id, " +
                    "j.a IS NOT NULL AND id != :idFrom AND j.a=:idFrom AND j.b=id AS isPendantrom " +
                "FROM " +
                    "(VALUES " + idElements
                        .stream()
                        .map(id -> "ROW(" + id + ")")
                        .collect(Collectors.joining(",")) +
                ") ids LEFT JOIN JGraphElement j " +
                    "ON hardcodedNames.column_0 = j.id " +
                "WHERE id IN (:idElements)",
                ImmutableMap.of("idElements", idElements, "idFrom", idFrom),
                new ResultSetExtractor<OrderedSet<Boolean>>() {
                    @Override
                    public OrderedSet<Boolean> extractData(ResultSet rs)
                        throws SQLException, DataAccessException {
                        
                        OrderedSet<Boolean> result = new OrderedSet();
                        while(rs.next()) {
                            result.add(rs.getBoolean("isPendantFrom"));
                        }
                        return result;
                    }
                }
            );
    }

    public boolean isElementPendantTo(long idElement, long idTo) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                    "SELECT a=id AND b=:idTo FROM Element WHERE id=:idElement",
                    ImmutableMap.of(
                        "idElement", idElement,
                        "idTo", idTo
                    ),
                    Boolean.class
                );
        } catch(EmptyResultDataAccessException e) {
            throw new ElementDoesNotExistException(idElement);
        }
    }

    public OrderedSet<Boolean> areElementsPendantsTo(OrderedSet<Long> idElements, long idTo) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .query(
                "SELECT " +
                    "ids.column_0 AS id, " +
                    "j.a IS NOT NULL AND id != :idTo AND j.a=id AND j.b=:idTo AS isPendantTo " +
                "FROM " +
                    "(VALUES " + idElements
                        .stream()
                        .map(id -> "ROW(" + id + ")")
                        .collect(Collectors.joining(",")) +
                    ") ids LEFT JOIN JGraphElement j " +
                    "ON hardcodedNames.column_0 = j.id " +
                "WHERE id IN (:idElements)",
                ImmutableMap.of("idElements", idElements, "idTo", idTo),
                new ResultSetExtractor<OrderedSet<Boolean>>() {
                    @Override
                    public OrderedSet<Boolean> extractData(ResultSet rs)
                        throws SQLException, DataAccessException {

                        OrderedSet<Boolean> result = new OrderedSet();
                        while(rs.next()) {
                            result.add(rs.getBoolean("isPendantTo"));
                        }
                        return result;
                    }
                }
            );
    }

    public boolean isElementLoopOn(long idElement, long idOn) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                    "SELECT id!=:idOn AND a=idOn AND b=:idOn FROM Element WHERE id=:idElement",
                    ImmutableMap.of(
                        "idElement", idElement,
                        "idOn", idOn
                    ),
                    Boolean.class
                );
        } catch(EmptyResultDataAccessException e) {
            throw new ElementDoesNotExistException(idElement);
        }
    }

    public OrderedSet<Boolean> areElementsLoopsOn(OrderedSet<Long> idElements, long idOn) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .query(
                "SELECT " +
                    "ids.column_0 AS id, " +
                    "j.a IS NOT NULL AND id!=idOn AND j.a=:idTo AND j.b=:idTo AS isLoopOn " +
                "FROM " +
                    "(VALUES " + idElements
                        .stream()
                        .map(id -> "ROW(" + id + ")")
                        .collect(Collectors.joining(",")) +
                    ") ids LEFT JOIN JGraphElement j " +
                        "ON hardcodedNames.column_0 = j.id " +
                "WHERE id IN (:idElements)",
                ImmutableMap.of("idElements", idElements, "idOn", idOn),
                new ResultSetExtractor<OrderedSet<Boolean>>() {
                    @Override
                    public OrderedSet<Boolean> extractData(ResultSet rs)
                        throws SQLException, DataAccessException {
    
                        OrderedSet<Boolean> result = new OrderedSet();
                        while(rs.next()) {
                            result.add(rs.getBoolean("isLoopOn"));
                        }
                        return result;
                    }
                }
            );
    }

    /**
     * Determine whether any <em>other</em> Elements connect to/from an Element.
     * If an Element with the given id does not exist, returns false.
     * @param id
     * @return
     */
    public boolean isElementEndpoint(long id) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                    "SELECT id  FROM JGraphElement WHERE id != :id AND (a=:id OR b=:id)",
                    ImmutableMap.of("id", id),
                    Boolean.class
                );
        } catch(EmptyResultDataAccessException e) {
            throw new ElementDoesNotExistException(id);
        }
    }

    /**
     * Given the list of ids, returns a map from ids to booleans indicating whether the element with the given id
     * is an endpoint.
     * @param ids
     * @return
     */
    public OrderedSet<Boolean> areElementsEndpoints(OrderedSet<Long> ids) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .query(
                "SELECT " +
                    "ids.id, " +
                    "COUNT(JGraphElement.id) > 0 AS isEndpoint " +
                "FROM " +
                    "(VALUES " +
                        ids
                            .stream()
                            .map(id -> "(" + id + ")")
                            .collect(Collectors.joining(",")) +
                    ") AS ids LEFT JOIN JGraphElement ON (" +
                        "ids.id != JGraphElement.id AND (" +
                        "ids.id=JGraphElement.a OR " +
                        "ids.id=JGraphElement.b" +
                    ")" +
                ") GROUP BY ids.id",
                ImmutableMap.of("ids", ids),
                new ResultSetExtractor<OrderedSet<Boolean>>() {
                    @Override
                    public OrderedSet<Boolean> extractData(ResultSet rs)
                        throws SQLException, DataAccessException {

                        OrderedSet<Boolean> result = new OrderedSet();
                        while(rs.next()) {
                            result.add(rs.getBoolean("isEndpoint"));
                        }
                        return result;
                    }
                }
            );
    }

    /**
     * Are Elements (identified by) x and y are connected?
     * In the case that either x or y do not exist, returns false.
     * @param x The id of Element x
     * @param y The id of Element y
     * @return
     */
    public boolean areElementsConnected(long x, long y) {
        return componentService.getComponentIds(x).contains(y);
    }

    public int numElementsWithAOrB(long a, long b) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                    "SELECT COUNT(1) FROM JGraphElement WHERE a=:a OR b=:b",
                    ImmutableMap.of("a", a, "b", b),
                    Integer.class
                );
        } catch(EmptyResultDataAccessException e) {
            return 0;
        }
    }

    public int numElementsWithAAndB(long a, long b) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                    "SELECT COUNT(1) FROM JGraphElement WHERE a=:a AND b=:b",
                    ImmutableMap.of("a", a, "b", b),
                    Integer.class
                );
        } catch(EmptyResultDataAccessException e) {
           return 0;
        }
    }

    public int numNodes() {
        try {
            return databaseService
                .getJdbcTemplate()
                .queryForObject(
                    "SELECT COUNT(1) " +
                    "FROM JGraphElement " +
                    "WHERE " +
                        "a = id AND " +
                        "b = id ",
                    Integer.class
                );
        } catch(EmptyResultDataAccessException e) {
            return 0;
        }
    }

    public int numPendantsFrom(long idFrom) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                    "SELECT COUNT(1) " +
                    "FROM JGraphElement " +
                    "WHERE " +
                        "id != :idFrom AND " +
                        "a = :idFrom AND " +
                        "b = id ",
                    ImmutableMap.of("idFrom", idFrom),
                    Integer.class
                );
        } catch(EmptyResultDataAccessException e) {
            return 0;
        }
    }

    public int numPendantsTo(long idTo) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                    "SELECT COUNT(1) " +
                    "FROM JGraphElement " +
                    "WHERE " +
                        "id != :idTo AND " +
                        "a = id AND " +
                        "b = :idTo ",
                    ImmutableMap.of("idTo", idTo),
                    Integer.class
                );
        } catch(EmptyResultDataAccessException e) {
            return 0;
        }
    }

    public int numLoopsOn(long idOn) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                    "SELECT COUNT(1) " +
                    "FROM JGraphElement " +
                    "WHERE " +
                        "id != :idOn AND " +
                        "a = :idOn AND " +
                        "b = :idOn ",
                    ImmutableMap.of("idOn", idOn),
                    Integer.class
                );
        } catch(EmptyResultDataAccessException e) {
            return 0;
        }
    }

    /**
     * Get the number of elements that are an endpoint of the given id. That is, the non-identical elements that have
     * an a or b with the given id.
     */
    public int numElementsEnpointOf(long id) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                    "SELECT COUNT(1) FROM JGraphElement WHERE id != :id AND (a=:id OR b=:id)",
                    ImmutableMap.of("id", id),
                    Integer.class
                );
        } catch(EmptyResultDataAccessException e) {
            throw new ElementDoesNotExistException(id);
        }
    }

    public OrderedSet<Long> getAllIds() {
        return databaseService
            .getJdbcTemplate()
            .query(
                "SELECT id FROM JGraphElement ORDER BY id",
                ID_ORDERED_SET_EXTRACTOR
            );
    }

    /**
     * Get the ids of Elements that exist with the given list of ids. If an Element with an id at index i does not
     * exist, then the value at index i in the returned list will be null.
     */
    public OrderedSet<Long> getIds(OrderedSet<Long> ids) {            
        return databaseService
            .getNamedParameterJdbcTemplate()
            .query(
                "SELECT " +
                    "j.id " +
                "FROM " +
                    "(VALUES " +
                        ids
                            .stream()
                            .map(i -> "ROW(" + i + ")")
                            .collect(Collectors.joining(",")) +
                    ") ids LEFT JOIN JGraphElement j " +
                        "ON ids.column_0 = j.id ",
                ImmutableMap.of("ids", ids),
                ID_ORDERED_SET_EXTRACTOR
            );
    }


    public OrderedSet<Long> getIdsWithAOrB(long a, long b) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .query(
                    "SELECT id FROM JGraphElement WHERE a=:a OR b=:b ORDER BY id",
                    ImmutableMap.of("a", a,"b", b),
                    ID_ORDERED_SET_EXTRACTOR
                );
        } catch(EmptyResultDataAccessException e) {
            return OrderedSets.empty();
        }
    }

    public OrderedSet<Long> getIdsWithAAndB(long a, long b) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .query(
                    "SELECT id FROM JGraphElement WHERE a=:a AND b=:b ORDER BY id",
                    ImmutableMap.of("a", a,"b", b),
                    ID_ORDERED_SET_EXTRACTOR
                );
        } catch(EmptyResultDataAccessException e) {
            return OrderedSets.empty();
        }
    }

    public OrderedSet<Long> getIdsNodes() {
        return databaseService
            .getJdbcTemplate()
            .query(
                "SELECT id " +
                "FROM JGraphElement ON " +
                "WHERE a = id AND b = id",
                ID_ORDERED_SET_EXTRACTOR
            );
    }

    public OrderedSet<Long> getIdsPendantsFrom(long idFrom) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .query(
                "SELECT id " +
                "FROM JGraphElement " +
                "WHERE " +
                    "id != :idFrom AND " +
                    "a = :idFrom AND " +
                    "b = id",
                ImmutableMap.of("idFrom", idFrom),
                ID_ORDERED_SET_EXTRACTOR
            );
}

    public OrderedSet<Long> getIdsPendantsTo(long idTo) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .query(
                "SELECT id " +
                "FROM JGraphElement " +
                "WHERE " +
                    "id != :idFrom AND " +
                    "a = id AND " +
                    "b = :idTo",
                ImmutableMap.of("idTo", idTo),
                ID_ORDERED_SET_EXTRACTOR
            );
    }

    public OrderedSet<Long> getIdsLoopsOn(long idOn) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .query(
                "SELECT id " +
                "FROM JGraphElement " +
                "WHERE " +
                    "id != :idOn AND " +
                    "a = :idOn AND " +
                    "b = :idOn",
                ImmutableMap.of("idOn", idOn),
                ID_ORDERED_SET_EXTRACTOR
            );
    }

    /**
     * Get the ids of elements that are an endpoint of the given id. That is, the non-identical elements that have
     * an a or b with the given id.
     */
    public OrderedSet<Long> getIdsEndpointsOf(long id) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .query(
                    "SELECT id FROM JGraphElement WHERE id != :id AND (a=:id OR b=:id) ORDER BY id",
                    ImmutableMap.of("id", id),
                    ID_ORDERED_SET_EXTRACTOR
                );
        } catch(EmptyResultDataAccessException e) {
            throw new ElementDoesNotExistException(id);
        }
    }

    public OrderedSet<OrderedSet<Long>> getIdsEndpointsOfForEach(OrderedSet<Long> ids) {
        return databaseService
            .getJdbcTemplate()
            .query(
                "SELECT " +
                    "ids.column_0 AS idElement" +
                    "j.id AS idEndpoint " +
                "FROM (" +
                    "VALUES " +
                        ids
                            .stream()
                            .map(id -> "ROW(" + id + ")")
                            .collect(Collectors.joining(",")) +
                    ") AS ids LEFT JOIN JGraphElement j ON " +
                        "j.id != ids.id AND ( " +
                        "j.a = ids.id OR " +
                        "j.b = ids.b " +
                    "ORDER BY " +
                        "idElement, " +
                        "idEndpoint",
                new ResultSetExtractor<OrderedSet<OrderedSet<Long>>>() {
                    @Override
                    public OrderedSet<OrderedSet<Long>> extractData(ResultSet rs)
                        throws SQLException, DataAccessException {

                        OrderedSet<OrderedSet<Long>> result = new OrderedSet<>();
                        long idPrevious = -1;
                        Set<Long> idEndpointsForElement = null;
                        while(rs.next()) {
                            long idCurrent = rs.getLong("idElement");
                            long idEndpoint = rs.getLong("idEndpoint");
                            if(idCurrent != idPrevious) {
                                idEndpointsForElement = rs.wasNull() ?
                                    OrderedSets.empty() :
                                    OrderedSets.singleton(idEndpoint);
                            } else {
                                idEndpointsForElement.add(idEndpoint);
                            }
                        }
                        return result;
                    }
                }
            );
    }

    public Element getElement(long id) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                    "SELECT id, a, b FROM JGraphElement WHERE id=:id",
                    ImmutableMap.of("id", id),
                    ROW_MAPPER
                );
        } catch(EmptyResultDataAccessException e) {
            throw new ElementDoesNotExistException(id);
        }
    }

    public OrderedSet<Element> getAllElements() {
        return databaseService
            .getJdbcTemplate()
            .query(
                "SELECT id, a, b FROM JGraphElement ORDER BY id",
                ELEMENT_ORDERED_SET_EXTRACTOR
            );
    }

    /**
     * Get the Elements that exist with the given list of ids. If an Element with an id at index i does not exist,
     * then the value at index i in the returned list will be null.
     */
    public OrderedSet<Element> getElements(OrderedSet<Long> ids) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .query(
                    "SELECT " +
                        "ids.column_0 AS id, " +
                        "j.a AS a, " +
                        "j.b AS b " +
                    "FROM " +
                        "(VALUES " +
                            ids
                                .stream()
                                .map(i -> "ROW(" + i + ")")
                                .collect(Collectors.joining(",")) +
                        ") ids LEFT JOIN JGraphElement j " +
                            "ON ids.column_0 = j.id ",
                    ImmutableMap.of("ids", ids),
                    ELEMENT_ORDERED_SET_EXTRACTOR
                );
        } catch(EmptyResultDataAccessException e) {
            return new OrderedSet();
        }
    }

    public OrderedSet<Element> getElementsWithAOrB(long a, long b) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .query(
                    "SELECT id, a, b FROM JGraphElement WHERE a=:a OR b=:b ORDER BY id",
                    ImmutableMap.of("a", a,"b", b),
                    ELEMENT_ORDERED_SET_EXTRACTOR
                );
        } catch(EmptyResultDataAccessException e) {
            return OrderedSets.empty();
        }
    }

    public OrderedSet<Element> getElementsWithAAndB(long a, long b) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .query(
                    "SELECT id, a, b FROM JGraphElement WHERE a=:a AND b=:b ORDER BY id",
                    ImmutableMap.of("a", a,"b", b),
                    ELEMENT_ORDERED_SET_EXTRACTOR
                );
        } catch(EmptyResultDataAccessException e) {
            return OrderedSets.empty();
        }
    }
    public OrderedSet<Element> getNodes() {
        try {
            return databaseService
                .getJdbcTemplate()
                .query(
                    "SELECT id, a, b " +
                    "FROM JGraphElement " +
                    "WHERE a = id AND b = id",
                    ELEMENT_ORDERED_SET_EXTRACTOR
                );
        } catch(EmptyResultDataAccessException e) {
            return OrderedSets.empty();
        }
    }

    public OrderedSet<Element> getPendantsFrom(long idFrom) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .query(
                    "SELECT id, a, b " +
                    "FROM JGraphElement " +
                    "WHERE "  +
                        "id != :idFrom AND " +
                        "a = :idFrom AND " +
                        "b = id",
                    ImmutableMap.of("idFrom", idFrom),
                    ELEMENT_ORDERED_SET_EXTRACTOR
                );
        } catch(EmptyResultDataAccessException e) {
            return OrderedSets.empty();
        }
    }

    public OrderedSet<Element> getPendantsTo(long idTo) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .query(
                    "SELECT id, a, b " +
                    "FROM JGraphElement " +
                    "WHERE " +
                        "id != :idTo AND " +
                        "a = :idTo AND " +
                        "b = id",
                    ImmutableMap.of("idTo", idTo), ELEMENT_ORDERED_SET_EXTRACTOR
                );
        } catch(EmptyResultDataAccessException e) {
            return OrderedSets.empty();
        }
    }

    public OrderedSet<Element> getLoopsOn(long idOn) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .query(
                    "SELECT id, a, b " +
                    "FROM JGraphElement " +
                    "WHERE " +
                        "id != :idOn AND " +
                        "a = :idOn AND " +
                        "b = :idOn",
                    ImmutableMap.of("idOn", idOn),
                    ELEMENT_ORDERED_SET_EXTRACTOR
                );
        } catch(EmptyResultDataAccessException e) {
            return OrderedSets.empty();
        }
    }

    /**
     * Get the ids of elements that are an endpoint of the given id. That is, the non-identical elements that have
     * an a or b with the given id.
     */
    public OrderedSet<Element> getEndpointsOf(long id) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .query(
                    "SELECT id, a, b FROM JGraphElement WHERE id != :id AND (a=:id OR b=:id) ORDER BY id",
                    ImmutableMap.of("id", id),
                    ELEMENT_ORDERED_SET_EXTRACTOR
                );
        } catch(EmptyResultDataAccessException e) {
            return OrderedSets.empty();
        }
    }

    public OrderedSet<Set<Element>> getEndpointsOfForEach(OrderedSet<Long> ids) {
        return databaseService
            .getJdbcTemplate()
            .query(
                "SELECT " +
                    "ids.column_0 AS idElement" +
                    "j.id AS idEndpoint " +
                    "j.a AS a " +
                    "j.b AS b " +
                "FROM (" +
                    "VALUES " +
                        ids
                            .stream()
                            .map(id -> "ROW(" + id + ")")
                            .collect(Collectors.joining(",")) +
                    ") AS ids LEFT JOIN JGraphElement j ON " +
                        "j.id != ids.id AND ( " +
                        "j.a = ids.id OR " +
                        "j.b = ids.b ",
                new ResultSetExtractor<OrderedSet<Set<Element>>>() {
                    @Override
                    public OrderedSet<Set<Element>> extractData(ResultSet rs)
                        throws SQLException, DataAccessException {

                        OrderedSet<Set<Element>> result = new OrderedSet<>();
                        long idPrevious = -1;
                        Set<Element> endpointsForElement = null;
                        while(rs.next()) {
                            long idCurrent = rs.getLong("idElement");
                            Element endpoint = new Element(rs.getLong("idEndpoint"), rs.getLong("a"), rs.getLong("b"));
                            if(idCurrent != idPrevious) {
                                endpointsForElement = rs.wasNull() ?
                                    Sets.newHashSet() :
                                    Sets.newHashSet(endpoint);
                            } else {
                                endpointsForElement.add(endpoint);
                            }
                        }
                        return result;
                    }
                }
            );
    }

    public long createElement(long a, long b) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        databaseService
            .getNamedParameterJdbcTemplate()
            .update(
                "INSERT INTO JGraphElement (a, b) VALUES (:a, :b)",
                    new MapSqlParameterSource(
                        ImmutableMap.of(
                            "a", createElementRequestValueToSqlInsert(a),
                            "b", createElementRequestValueToSqlInsert(b)
                        )
                    ),
                keyHolder
            );
        return keyHolder.getKey().longValue();
    }

    public OrderedSet<Long> createElements(Set<CreateElementRequest> requests) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        databaseService
            .getJdbcTemplate()
            .update(
                "INSERT INTO JGraphElement (id, a, b) VALUES " + requests
                    .stream()
                    .map( request ->
                        "(" +
                            createElementRequestValueToSqlInsert(request.getA()) + ", " +
                            createElementRequestValueToSqlInsert(request.getB()) +
                        ")"
                    ).collect(Collectors.joining(",")),
                keyHolder
            );
        return keyHolder
            .getKeyList()
            .stream()
            .mapToLong(key -> ((BigInteger) key.get("GENERATED_KEY")).longValue())
            .boxed()
            .collect(Collectors.toCollection(OrderedSet::new));
    }

    private String createElementRequestValueToSqlInsert(long value) {
        return value > 0 ? value + "" : "LAST_INSERT_ID() + " + (-1 * value + 1);
    }

    public long createNode() {
        return createElement(0, 0);
    }

    public OrderedSet<Long> createNodes(int number) {
        return createElements(
            IntStream
                .range(0, number)
                .mapToObj(i -> new CreateElementRequest(-i, -i))
                .collect(Collectors.toCollection(Sets::newHashSet))
        );
    }

    public long createPendantFrom(long from) {
        return createElement(from, 0);
    }

    public OrderedSet<Long> createPendantsFrom(long from, int howMany) {
        return createElements(
            IntStream
                .range(0, howMany)
                .mapToObj(i -> new CreateElementRequest(from, -1 * i))
                .collect(Collectors.toCollection(Sets::newHashSet))
        );
    }

    public long createPendantTo(long to) {
        return createElement(0, to);
    }

    public OrderedSet<Long> createPendantsTo(long to, int howMany) {
        return createElements(
            IntStream
                .range(0, howMany)
                .mapToObj(i -> new CreateElementRequest(-1 * i, to))
                .collect(Collectors.toCollection(Sets::newHashSet))
        );
    }

    public long createLoopOn(long on) {
        return createElement(on, on);
    }

    public OrderedSet<Long> createLoopsOn(long on, int howMany) {
        return OrderedSets.with(
            createElements(
                IntStream
                    .range(0, howMany)
                    .mapToObj(i -> new CreateElementRequest(on, on))
                    .collect(Collectors.toCollection(Sets::newHashSet))
            )
        );
    }

    public void updateElement(long id, UpdateElementRequest request) {
        databaseService
            .getNamedParameterJdbcTemplate()
            .update(
                "UPDATE JGraphElement set a=:a, b=:b WHERE id=:id",
                ImmutableMap.of(
                    "id", id,
                    "a", request.getA(),
                    "b", request.getB()
                )
            );
    }

    public void updateElements(OrderedSet<Long> ids, List<UpdateElementRequest> requests) {
        databaseService
            .getJdbcTemplate()
            .update(
                "INSERT INTO JGraphElement (id, a, b) VALUES " + IntStream
                    .range(0, ids.size())
                    .mapToObj(i ->
                        "(" + ids.get(i) + ", " +
                            requests.get(i).getA() + ", " +
                            requests.get(i).getB() + ")"
                    ).collect(Collectors.joining(",")) +
                " ON DUPLICATE KEY UPDATE " +
                    "id=VALUES(id), " +
                    "a=VALUES(a), " +
                    "b=VALUES(b) "
            );
    }

    /**
     * The Element with the given id must either not exist, or have no Elements connected to or from it.
     */
    public void deleteElement(long id) {
        databaseService
            .getNamedParameterJdbcTemplate()
            .update(
                "DELETE FROM Element WHERE id = :id",
                ImmutableMap.of("id", id)
            );
    }

    /**
     * All of the Elements with the given ids must either not exist, or have no Elements connected to or from it.
     */
    public void deleteElements(Set<Long> ids) {
        databaseService
            .getNamedParameterJdbcTemplate()
            .update(
                "DELETE FROM Element WHERE id IN (:ids)",
                ImmutableMap.of("ids", ids)
            );
    }
}
