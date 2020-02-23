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
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

@Service("JGraphElementService")
public class ElementService {

    public static final MathBeanPropertyRowMapper<Element> ROW_MAPPER =
        MathBeanPropertyRowMapper.newInstance(Element.class);
    public static final ResultSetExtractor<OrderedSet<Long>> ID_ORDERED_SET_EXTRACTOR = rs -> {
        OrderedSet<Long> result = OrderedSets.empty();
        while(rs.next()) {
            result.add(rs.getLong("id") == 0 ? null : rs.getLong("id"));
        }
        return result;
    };
    public static final ResultSetExtractor<List<Long>> ID_LIST_EXTRACTOR = rs -> {
        List<Long> result = Lists.newArrayList();
        while(rs.next()) {
            result.add(rs.getLong("id") == 0 ? null : rs.getLong("id"));
        }
        return result;
    };
    public static final ResultSetExtractor<OrderedSet<Element>> ELEMENT_ORDERED_SET_EXTRACTOR = rs -> {
        OrderedSet<Element> result = OrderedSets.empty();
        while(rs.next()) {
            long id = rs.getLong("id");
            long a = rs.getLong("a");
            long b = rs.getLong("b");
            if(id == 0 || a == 0 || b == 0) {
                result.add(null);
            } else {
                result.add(new Element(id, a, b));
            }
        }
        return result;
    };
    private static final ResultSetExtractor<List<Element>> ELEMENT_LIST_EXTRACTOR = rs -> {
        List<Element> result = Lists.newArrayList();
        while(rs.next()) {
            long id = rs.getLong("id");
            long a = rs.getLong("a");
            long b = rs.getLong("b");
            if(id == 0 || a == 0 || b == 0) {
                result.add(null);
            } else {
                result.add(new Element(id, a, b));
            }
        }
        return result;
    };

    @Autowired
    private ComponentService componentService;

    @Autowired
    private DatabaseService databaseService;

    private AtomicLong nextId;

    @PostConstruct
    public void initialize() {
        Long maxId = databaseService
            .getJdbcTemplate()
            .queryForObject(
                "SELECT MAX(id) FROM JGraphElement",
                Long.class
            );
        if(maxId != null) {
            nextId = new AtomicLong(maxId + 1);
        } else {
            seed();
        }
    }

    public boolean doesElementExist(long id) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                    "SELECT COUNT(1) FROM JGraphElement WHERE id=:id",
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
                    "SELECT COUNT(1) FROM JGraphElement WHERE id IN (:ids)",
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
                    "SELECT a=id AND b=id FROM JGraphElement WHERE id=:idElement",
                    ImmutableMap.of("idElement", idElement),
                    Boolean.class
                );
        } catch(EmptyResultDataAccessException e) {
            throw new ElementDoesNotExistException(idElement);
        }
    }

    public List<Boolean> areElementsNodes(OrderedSet<Long> idElements) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForList(
                "SELECT " +
                    "j.a IS NOT NULL AND j.a=id AND j.b=id " +
                "FROM " +
                    "(VALUES " + idElements
                        .stream()
                        .map(id -> "ROW(" + id + ")")
                        .collect(Collectors.joining(",")) +
                    ") ids LEFT JOIN JGraphElement j " +
                        "ON ids.column_0 = j.id",
                ImmutableMap.of("idElements", idElements),
                Boolean.class
            );
    }

    public boolean isElementPendantFrom(long idElement, long idFrom) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                    "SELECT a=:idFrom AND b=id FROM JGraphElement WHERE id=:idElement",
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

    public List<Boolean> areElementsPendantsFrom(OrderedSet<Long> idElements, long idFrom) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForList(
                "SELECT " +
                    "j.a IS NOT NULL AND id != :idFrom AND j.a=:idFrom AND j.b=id " +
                "FROM " +
                    "(VALUES " + idElements
                        .stream()
                        .map(id -> "ROW(" + id + ")")
                        .collect(Collectors.joining(",")) +
                ") ids LEFT JOIN JGraphElement j " +
                    "ON ids.column_0 = j.id",
                ImmutableMap.of("idElements", idElements, "idFrom", idFrom),
                Boolean.class
            );
    }

    public boolean isElementPendantTo(long idElement, long idTo) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                    "SELECT a=id AND b=:idTo FROM JGraphElement WHERE id=:idElement",
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

    public List<Boolean> areElementsPendantsTo(OrderedSet<Long> idElements, long idTo) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForList(
                "SELECT " +
                    "j.a IS NOT NULL AND id != :idTo AND j.a=id AND j.b=:idTo " +
                "FROM " +
                    "(VALUES " + idElements
                        .stream()
                        .map(id -> "ROW(" + id + ")")
                        .collect(Collectors.joining(",")) +
                    ") ids LEFT JOIN JGraphElement j " +
                    "ON ids.column_0 = j.id",
                ImmutableMap.of("idElements", idElements, "idTo", idTo),
                Boolean.class
            );
    }

    public boolean isElementLoopOn(long idElement, long idOn) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                    "SELECT id!=:idOn AND a=:idOn AND b=:idOn FROM JGraphElement WHERE id=:idElement",
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

    public List<Boolean> areElementsLoopsOn(OrderedSet<Long> idElements, long idOn) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForList(
                "SELECT " +
                    "j.a IS NOT NULL AND ids.column_0 != :idOn AND j.a = :idOn AND j.b = :idOn " +
                "FROM " +
                    "(VALUES " + idElements
                        .stream()
                        .map(id -> "ROW(" + id + ")")
                        .collect(Collectors.joining(",")) +
                    ") ids LEFT JOIN JGraphElement j " +
                        "ON ids.column_0 = j.id",
                ImmutableMap.of("idElements", idElements, "idOn", idOn),
                Boolean.class
            );
    }

    /**
     * Determine whether any <em>other</em> Elements connect to/from an Element.
     * If an Element with the given id does not exist, returns false.
     * @param id
     * @return
     */
    public boolean isElementEndpoint(long id) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForObject(
                "SELECT COUNT(1)  FROM JGraphElement WHERE id != :id AND (a=:id OR b=:id)",
                ImmutableMap.of("id", id),
                Boolean.class
            );
    }

    /**
     * Given the list of ids, returns a map from ids to booleans indicating whether the element with the given id
     * is an endpoint.
     * @param ids
     * @return
     */
    public List<Boolean> areElementsEndpoints(OrderedSet<Long> ids) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForList(
                "SELECT " +
                    "j.id IS NOT NULL AS isEndpoint " +
                "FROM " +
                    "(VALUES " +
                        ids
                            .stream()
                            .map(id -> "ROW (" + id + ")")
                            .collect(Collectors.joining(",")) +
                    ") AS ids LEFT JOIN JGraphElement j ON " +
                        "ids.column_0 != j.id AND (" +
                            "ids.column_0 = j.a OR " +
                            "ids.column_0 = j.b" +
                        ") " +
                "GROUP BY ids.column_0",
                ImmutableMap.of("ids", ids),
                Boolean.class
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
    public List<Long> getIds(OrderedSet<Long> ids) {
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
                ID_LIST_EXTRACTOR
            );
    }

    public OrderedSet<Long> getIdsThatDoNotExist(OrderedSet<Long> ids) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .query(
                "SELECT " +
                    "ids.column_0 AS id " +
                "FROM " +
                    "(VALUES " +
                        ids
                            .stream()
                            .map(i -> "ROW(" + i + ")")
                            .collect(Collectors.joining(",")) +
                    ") ids LEFT JOIN JGraphElement j " +
                        "ON ids.column_0 = j.id " +
                "WHERE j.id IS NULL",
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
                "FROM JGraphElement " +
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
                    "id != :idTo AND " +
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

    public List<OrderedSet<Long>> getIdsEndpointsOfForEach(OrderedSet<Long> ids) {
        return databaseService
            .getJdbcTemplate()
            .query(
                "SELECT " +
                    "ids.column_0 AS idElement, " +
                    "j.id AS idEndpoint " +
                "FROM (" +
                    "VALUES " +
                        ids
                            .stream()
                            .map(id -> "ROW(" + id + ")")
                            .collect(Collectors.joining(",")) +
                    ") AS ids LEFT JOIN JGraphElement j ON " +
                        "j.id != ids.column_0 AND ( " +
                            "j.a = ids.column_0 OR " +
                            "j.b = ids.column_0 " +
                        ") " +
                    "ORDER BY " +
                        "idElement, " +
                        "idEndpoint",
                new ResultSetExtractor<List<OrderedSet<Long>>>() {
                    @Override
                    public List<OrderedSet<Long>> extractData(ResultSet rs)
                        throws SQLException, DataAccessException {

                        List<OrderedSet<Long>> result = Lists.newArrayList();
                        long idPrevious = -1;
                        OrderedSet<Long> idEndpointsForElement = null;
                        while(rs.next()) {
                            long idCurrent = rs.getLong("idElement");
                            long idEndpoint = rs.getLong("idEndpoint");
                            if(idCurrent != idPrevious) {
                                idPrevious = idCurrent;
                                idEndpointsForElement = rs.wasNull() ?
                                    OrderedSets.empty() :
                                    OrderedSets.singleton(idEndpoint);
                                result.add(idEndpointsForElement);
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
    public List<Element> getElements(OrderedSet<Long> ids) {
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
                ELEMENT_LIST_EXTRACTOR
            );
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
                        "a = id AND " +
                        "b = :idTo",
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

    public List<OrderedSet<Element>> getEndpointsOfForEach(OrderedSet<Long> ids) {
        return databaseService
            .getJdbcTemplate()
            .query(
                "SELECT " +
                    "ids.column_0 AS idElement, " +
                    "j.id AS idEndpoint, " +
                    "j.a AS a, " +
                    "j.b AS b " +
                "FROM (" +
                    "VALUES " +
                        ids
                            .stream()
                            .map(id -> "ROW(" + id + ")")
                            .collect(Collectors.joining(",")) +
                    ") AS ids LEFT JOIN JGraphElement j ON " +
                        "j.id != ids.column_0 AND ( " +
                        "j.a = ids.column_0 OR " +
                        "j.b = ids.column_0 " +
                    ")",
                new ResultSetExtractor<List<OrderedSet<Element>>>() {
                    @Override
                    public List<OrderedSet<Element>> extractData(ResultSet rs)
                        throws SQLException, DataAccessException {

                        List<OrderedSet<Element>> result = Lists.newArrayList();
                        long idPrevious = -1;
                        OrderedSet<Element> endpointsForElement = null;
                        while(rs.next()) {
                            long idCurrent = rs.getLong("idElement");
                            Element endpoint = new Element(rs.getLong("idEndpoint"), rs.getLong("a"), rs.getLong("b"));
                            if(idCurrent != idPrevious) {
                                idPrevious = idCurrent;
                                endpointsForElement = rs.wasNull() ?
                                    OrderedSets.empty() :
                                    OrderedSets.singleton(endpoint);
                                result.add(endpointsForElement);
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
        databaseService
            .getJdbcTemplate()
            .update(
                "INSERT INTO JGraphElement (id, a, b) VALUES (" +
                    nextId.get() + ", " +
                    createElementRequestValueToSqlInsert(a, nextId.get()) + ", " +
                    createElementRequestValueToSqlInsert(b, nextId.get()) +
                ")"
            );
        return nextId.getAndIncrement();
    }

    public OrderedSet<Long> createElements(List<CreateElementRequest> requests) {
        AtomicInteger counter = new AtomicInteger();
        OrderedSet<Long> ids = LongStream
            .range(nextId.get(), nextId.get() + requests.size())
            .boxed()
            .collect(Collectors.toCollection(OrderedSet::new));
        databaseService
            .getJdbcTemplate()
            .update(
                "INSERT INTO JGraphElement (id, a, b) VALUES " + requests
                    .stream()
                    .map(request ->
                        "(" +
                            ids.get(counter.getAndIncrement()) + ", " +
                            createElementRequestValueToSqlInsert(request.getA(), nextId.get()) + ", " +
                            createElementRequestValueToSqlInsert(request.getB(), nextId.get()) +
                        ")"
                    ).collect(Collectors.joining(","))
            );
        nextId.set(nextId.get() + requests.size());
        return ids;
    }

    private String createElementRequestValueToSqlInsert(long value, long nextId) {
        return value > 0 ? value + "" : "" + (nextId + (-1 * value));
    }

    public long createNode() {
        return createElement(0, 0);
    }

    public OrderedSet<Long> createNodes(int number) {
        return createElements(
            IntStream
                .range(0, number)
                .mapToObj(i -> new CreateElementRequest(-i, -i))
                .collect(Collectors.toList())
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
                .collect(Collectors.toList())
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
                .collect(Collectors.toList())
        );
    }

    public long createLoopOn(long idOn) {
        return createElement(idOn, idOn);
    }

    public OrderedSet<Long> createLoopsOn(long idOn, int howMany) {
        return createElements(
            IntStream
                .range(0, howMany)
                .mapToObj(i -> new CreateElementRequest(idOn, idOn))
                .collect(Collectors.toList())
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
                "DELETE FROM JGraphElement WHERE id = :id",
                ImmutableMap.of("id", id)
            );
    }

    /**
     * All of the Elements with the given ids must either not exist, or have no Elements connected to or from it.
     */
    public void deleteElements(Set<Long> ids) {
        if(ids.isEmpty()) {
            return;
        }

        databaseService
            .getNamedParameterJdbcTemplate()
            .update(
                "DELETE FROM JGraphElement WHERE id IN (:ids)",
                ImmutableMap.of("ids", ids)
            );
    }

    /**
     * This deletes all elements, resets the auto_increment, and inserts the seed node.
     */
    public void seed() {
        deleteElements(getAllIds());
        databaseService
            .getNamedParameterJdbcTemplate()
            .update(
                "INSERT INTO JGraphElement (id, a, b) VALUES (:idSeed, :idSeed, :idSeed)",
                ImmutableMap.of("idSeed", Elements.ID_SEED)
        );
        nextId = new AtomicLong(Elements.ID_SEED + 1);
    }
}
