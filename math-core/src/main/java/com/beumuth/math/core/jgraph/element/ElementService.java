package com.beumuth.math.core.jgraph.element;

import com.beumuth.math.client.category.Categories;
import com.beumuth.math.client.jgraph.CreateElementRequest;
import com.beumuth.math.client.jgraph.Element;
import com.beumuth.math.client.jgraph.UpdateElementRequest;
import com.beumuth.math.client.settheory.orderedset.OrderedSet;
import com.beumuth.math.client.settheory.orderedset.OrderedSets;
import com.beumuth.math.core.internal.database.DatabaseService;
import com.beumuth.math.core.internal.database.MathBeanPropertyRowMapper;
import com.beumuth.math.core.jgraph.component.ComponentService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
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
                "SELECT MAX(id) FROM Element",
                Long.class
            );
        nextId = maxId == null ? new AtomicLong(1) : new AtomicLong(maxId + 1);
    }

    public boolean doesElementExist(long id) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForObject(
                "SELECT COUNT(1)>0 FROM Element WHERE id=:id",
                ImmutableMap.of("id", id),
                Boolean.class
            );
    }

    public boolean doAnyElementsExist(Set<Long> ids) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForObject(
                "SELECT COUNT(1)>0 FROM Element WHERE id IN (:ids)",
                ImmutableMap.of("ids", ids),
                Boolean.class
            );
    }

    public boolean doAllElementsExist(Set<Long> ids) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForObject(
                "SELECT COUNT(1)=:numIds FROM Element WHERE id IN (:ids)",
                ImmutableMap.of(
                    "ids", ids,
                    "numIds", ids.size()
                ),
                Boolean.class
            );
    }

    public boolean doesElementExistWithA(long a) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForObject(
                "SELECT COUNT(1) > 0 FROM Element WHERE a=:a",
                ImmutableMap.of("a", a),
                Boolean.class
            );
    }

    public boolean doesElementExistWithB(long b) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForObject(
                "SELECT COUNT(1) > 0 FROM Element WHERE b=:b",
                ImmutableMap.of("b", b),
                Boolean.class
            );
    }

    public boolean doesElementWithAOrBExist(long a, long b) {
        return numElementsWithAOrB(a, b) > 0;
    }

    public boolean doesElementExistWithAAndB(long a, long b) {
        return numElementsWithAAndB(a, b) > 0;
    }
    
    public boolean doesElementHaveA(long id, long a) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForObject(
                "SELECT a=:a FROM Element WHERE id=:id",
                ImmutableMap.of("id", id, "a", a),
                Boolean.class
            );
    }
    
    public boolean doesElementHaveB(long id, long b) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForObject(
                "SELECT b=:b FROM Element WHERE id=:id",
                ImmutableMap.of("id", id, "b", b),
                Boolean.class
            );
    }
    
    public List<Boolean> doElementsHaveA(OrderedSet<Long> ids, long a) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForList(
                "SELECT " +
                    "e.id IS NOT NULL AND e.a=:a " +
                "FROM " +
                    "(VALUES " + ids
                        .stream()
                        .map(id -> "ROW(" + id + ")")
                        .collect(Collectors.joining(",")) +
                    ") ids LEFT JOIN Element e " +
                        "ON ids.column_0 = e.id",
                ImmutableMap.of("a", a),
                Boolean.class
            );
    }

    public List<Boolean> doElementsHaveB(OrderedSet<Long> ids, long b) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForList(
                "SELECT " +
                    "e.id IS NOT NULL AND e.b=:b " +
                "FROM " +
                    "(VALUES " + ids
                        .stream()
                        .map(id -> "ROW(" + id + ")")
                        .collect(Collectors.joining(",")) +
                    ") ids LEFT JOIN Element e " +
                        "ON ids.column_0 = e.id",
                ImmutableMap.of("b", b),
                Boolean.class
            );
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
            return false;
        }
    }

    public List<Boolean> areElementsNodes(OrderedSet<Long> idElements) {
        return databaseService
            .getJdbcTemplate()
            .queryForList(
                "SELECT " +
                    "e.a IS NOT NULL AND e.a=id AND e.b=id " +
                "FROM " +
                    "(VALUES " + idElements
                        .stream()
                        .map(id -> "ROW(" + id + ")")
                        .collect(Collectors.joining(",")) +
                    ") ids LEFT JOIN Element e " +
                        "ON ids.column_0 = e.id",
                Boolean.class
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
            return false;
        }
    }

    public List<Boolean> areElementsPendantsFrom(OrderedSet<Long> idElements, long idFrom) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForList(
                "SELECT " +
                    "e.a IS NOT NULL AND id != :idFrom AND e.a=:idFrom AND e.b=id " +
                "FROM " +
                    "(VALUES " + idElements
                        .stream()
                        .map(id -> "ROW(" + id + ")")
                        .collect(Collectors.joining(",")) +
                ") ids LEFT JOIN Element e " +
                    "ON ids.column_0 = e.id",
                ImmutableMap.of("idFrom", idFrom),
                Boolean.class
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
            return false;
        }
    }

    public List<Boolean> areElementsPendantsTo(OrderedSet<Long> idElements, long idTo) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForList(
                "SELECT " +
                    "e.a IS NOT NULL AND id != :idTo AND e.a=id AND e.b=:idTo " +
                "FROM " +
                    "(VALUES " + idElements
                        .stream()
                        .map(id -> "ROW(" + id + ")")
                        .collect(Collectors.joining(",")) +
                    ") ids LEFT JOIN Element e " +
                    "ON ids.column_0 = e.id",
                ImmutableMap.of("idTo", idTo),
                Boolean.class
            );
    }

    public boolean isElementLoopOn(long idElement, long idOn) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                    "SELECT id!=:idOn AND a=:idOn AND b=:idOn FROM Element WHERE id=:idElement",
                    ImmutableMap.of(
                        "idElement", idElement,
                        "idOn", idOn
                    ),
                    Boolean.class
                );
        } catch(EmptyResultDataAccessException e) {
            return false;
        }
    }

    public List<Boolean> areElementsLoopsOn(OrderedSet<Long> idElements, long idOn) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForList(
                "SELECT " +
                    "e.a IS NOT NULL AND ids.column_0 != :idOn AND e.a = :idOn AND e.b = :idOn " +
                "FROM " +
                    "(VALUES " + idElements
                        .stream()
                        .map(id -> "ROW(" + id + ")")
                        .collect(Collectors.joining(",")) +
                    ") ids LEFT JOIN Element e " +
                        "ON ids.column_0 = e.id",
                ImmutableMap.of("idOn", idOn),
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
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                    "SELECT COUNT(1)  FROM Element WHERE id != :id AND (a=:id OR b=:id)",
                    ImmutableMap.of("id", id),
                    Boolean.class
                );
        } catch(EmptyResultDataAccessException e) {
            return false;
        }
    }

    /**
     * Given the list of ids, returns a map from ids to booleans indicating whether the element with the given id
     * is an endpoint.
     * @param ids
     * @return
     */
    public List<Boolean> areElementsEndpoints(OrderedSet<Long> ids) {
        return databaseService
            .getJdbcTemplate()
            .queryForList(
                "SELECT " +
                    "e.id IS NOT NULL AS isEndpoint " +
                "FROM " +
                    "(VALUES " +
                        ids
                            .stream()
                            .map(id -> "ROW (" + id + ")")
                            .collect(Collectors.joining(",")) +
                    ") AS ids LEFT JOIN Element e ON " +
                        "ids.column_0 != e.id AND (" +
                            "ids.column_0 = e.a OR " +
                            "ids.column_0 = e.b" +
                        ") " +
                "GROUP BY ids.column_0",
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

    public int numElementsWithA(long a) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForObject(
                "SELECT COUNT(1) FROM Element WHERE a=:a",
                ImmutableMap.of("a", a),
                Integer.class
            );
    }

    public int numElementsWithB(long b) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForObject(
                "SELECT COUNT(1) FROM Element WHERE b=:b",
                ImmutableMap.of("b", b),
                Integer.class
            );
    }

    public int numElementsWithAOrB(long a, long b) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForObject(
                "SELECT COUNT(1) FROM Element WHERE a=:a OR b=:b",
                ImmutableMap.of("a", a, "b", b),
                Integer.class
            );
    }

    public int numElementsWithAAndB(long a, long b) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForObject(
                "SELECT COUNT(1) FROM Element WHERE a=:a AND b=:b",
                ImmutableMap.of("a", a, "b", b),
                Integer.class
            );
    }

    public int numNodes() {
        return databaseService
            .getJdbcTemplate()
            .queryForObject(
                "SELECT COUNT(1) " +
                    "FROM Element " +
                    "WHERE " +
                    "a = id AND " +
                    "b = id ",
                Integer.class
            );
    }

    public int numPendantsFrom(long idFrom) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForObject(
                "SELECT COUNT(1) " +
                    "FROM Element " +
                    "WHERE " +
                    "id != :idFrom AND " +
                    "a = :idFrom AND " +
                    "b = id ",
                ImmutableMap.of("idFrom", idFrom),
                Integer.class
            );
    }

    public int numPendantsTo(long idTo) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForObject(
                "SELECT COUNT(1) " +
                    "FROM Element " +
                    "WHERE " +
                    "id != :idTo AND " +
                    "a = id AND " +
                    "b = :idTo ",
                ImmutableMap.of("idTo", idTo),
                Integer.class
            );
    }

    public int numLoopsOn(long idOn) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForObject(
                "SELECT COUNT(1) " +
                    "FROM Element " +
                    "WHERE " +
                    "id != :idOn AND " +
                    "a = :idOn AND " +
                    "b = :idOn ",
                ImmutableMap.of("idOn", idOn),
                Integer.class
            );
    }

    public OrderedSet<Long> getAllIds() {
        return databaseService
            .getJdbcTemplate()
            .query(
                "SELECT id FROM Element ORDER BY id",
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
                    "e.id " +
                "FROM " +
                    "(VALUES " +
                        ids
                            .stream()
                            .map(i -> "ROW(" + i + ")")
                            .collect(Collectors.joining(",")) +
                    ") ids LEFT JOIN Element e " +
                        "ON ids.column_0 = e.id ",
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
                    ") ids LEFT JOIN Element e " +
                        "ON ids.column_0 = e.id " +
                "WHERE e.id IS NULL",
                ImmutableMap.of("ids", ids),
                ID_ORDERED_SET_EXTRACTOR
            );
    }

    public OrderedSet<Long> getIdsWithA(long a) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .query(
                    "SELECT id FROM Element WHERE a=:a ORDER BY id",
                    ImmutableMap.of("a", a),
                    ID_ORDERED_SET_EXTRACTOR
                );
        } catch(EmptyResultDataAccessException e) {
            return OrderedSets.empty();
        }
    }

    public OrderedSet<Long> getIdsWithB(long b) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .query(
                    "SELECT id FROM Element WHERE b=:b ORDER BY id",
                    ImmutableMap.of("b", b),
                    ID_ORDERED_SET_EXTRACTOR
                );
        } catch(EmptyResultDataAccessException e) {
            return OrderedSets.empty();
        }
    }

    public OrderedSet<Long> getIdsWithAOrB(long a, long b) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .query(
                    "SELECT id FROM Element WHERE a=:a OR b=:b ORDER BY id",
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
                    "SELECT id FROM Element WHERE a=:a AND b=:b ORDER BY id",
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
                "FROM Element " +
                "WHERE a = id AND b = id",
                ID_ORDERED_SET_EXTRACTOR
            );
    }

    public OrderedSet<Long> getIdsPendantsFrom(long idFrom) {
        return databaseService
            .getNamedParameterJdbcTemplate()
            .query(
                "SELECT id " +
                "FROM Element " +
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
                "FROM Element " +
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
                "FROM Element " +
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
        return databaseService
            .getNamedParameterJdbcTemplate()
            .query(
                "SELECT id FROM Element WHERE id != :id AND (a=:id OR b=:id) ORDER BY id",
                ImmutableMap.of("id", id),
                ID_ORDERED_SET_EXTRACTOR
            );
    }

    public List<OrderedSet<Long>> getIdsEndpointsOfForEach(OrderedSet<Long> ids) {
        return databaseService
            .getJdbcTemplate()
            .query(
                "SELECT " +
                    "ids.column_0 AS idElement, " +
                    "e.id AS idEndpoint " +
                "FROM (" +
                    "VALUES " +
                        ids
                            .stream()
                            .map(id -> "ROW(" + id + ")")
                            .collect(Collectors.joining(",")) +
                    ") AS ids LEFT JOIN Element e ON " +
                        "e.id != ids.column_0 AND ( " +
                            "e.a = ids.column_0 OR " +
                            "e.b = ids.column_0 " +
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
        return databaseService
            .getNamedParameterJdbcTemplate()
            .queryForObject(
                "SELECT id, a, b FROM Element WHERE id=:id",
                ImmutableMap.of("id", id),
                ROW_MAPPER
            );
    }

    public OrderedSet<Element> getAllElements() {
        return databaseService
            .getJdbcTemplate()
            .query(
                "SELECT id, a, b FROM Element ORDER BY id",
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
                    "e.a AS a, " +
                    "e.b AS b " +
                "FROM " +
                    "(VALUES " +
                        ids
                            .stream()
                            .map(i -> "ROW(" + i + ")")
                            .collect(Collectors.joining(",")) +
                    ") ids LEFT JOIN Element e " +
                        "ON ids.column_0 = e.id ",
                ImmutableMap.of("ids", ids),
                ELEMENT_LIST_EXTRACTOR
            );
    }

    public OrderedSet<Element> getElementsWithA(long a) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .query(
                    "SELECT id, a, b FROM Element WHERE a=:a ORDER BY id",
                    ImmutableMap.of("a", a),
                    ELEMENT_ORDERED_SET_EXTRACTOR
                );
        } catch(EmptyResultDataAccessException e) {
            return OrderedSets.empty();
        }
    }

    public OrderedSet<Element> getElementsWithB(long b) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .query(
                    "SELECT id, a, b FROM Element WHERE b=:b ORDER BY id",
                    ImmutableMap.of("b", b),
                    ELEMENT_ORDERED_SET_EXTRACTOR
                );
        } catch(EmptyResultDataAccessException e) {
            return OrderedSets.empty();
        }
    }

    public OrderedSet<Element> getElementsWithAOrB(long a, long b) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .query(
                    "SELECT id, a, b FROM Element WHERE a=:a OR b=:b ORDER BY id",
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
                    "SELECT id, a, b FROM Element WHERE a=:a AND b=:b ORDER BY id",
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
                    "FROM Element " +
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
                    "FROM Element " +
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
                    "FROM Element " +
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
                    "FROM Element " +
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
                    "SELECT id, a, b FROM Element WHERE id != :id AND (a=:id OR b=:id) ORDER BY id",
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
                    "e.id AS idEndpoint, " +
                    "e.a AS a, " +
                    "e.b AS b " +
                "FROM (" +
                    "VALUES " +
                        ids
                            .stream()
                            .map(id -> "ROW(" + id + ")")
                            .collect(Collectors.joining(",")) +
                    ") AS ids LEFT JOIN Element e ON " +
                        "e.id != ids.column_0 AND ( " +
                        "e.a = ids.column_0 OR " +
                        "e.b = ids.column_0 " +
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
                "INSERT INTO Element (id, a, b) VALUES (" +
                    nextId.get() + ", " +
                    createElementRequestValueToSqlInsert(a, nextId.get()) + ", " +
                    createElementRequestValueToSqlInsert(b, nextId.get()) +
                ")"
            );
        return nextId.getAndIncrement();
    }

    public OrderedSet<Long> createElements(List<CreateElementRequest> requests) {
        long startId = nextId.getAndAdd(requests.size());
        OrderedSet<Long> ids = LongStream
            .range(startId, startId + requests.size())
            .boxed()
            .collect(Collectors.toCollection(OrderedSet::new));
        databaseService
            .getJdbcTemplate()
            .update(
                "INSERT INTO Element (id, a, b) VALUES " +
                IntStream
                    .range(0, requests.size())
                    .mapToObj(i->
                        "(" +
                            ids.get(i) + ", " +
                            createElementRequestValueToSqlInsert(requests.get(i).getA(), startId) + ", " +
                            createElementRequestValueToSqlInsert(requests.get(i).getB(), startId) +
                        ")"
                    ).collect(Collectors.joining(","))
            );
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
                "UPDATE Element set a=:a, b=:b WHERE id=:id",
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
                "INSERT INTO Element (id, a, b) VALUES " + IntStream
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
        if(ids.isEmpty()) {
            return;
        }

        databaseService
            .getNamedParameterJdbcTemplate()
            .update(
                "DELETE FROM Element WHERE id IN (:ids)",
                ImmutableMap.of("ids", ids)
            );
    }

    /**
     * This deletes all elements and resets the auto_increment to 1.
     */
    public void reset() {
        deleteElements(
            Sets.difference(
                Categories.ALL_STANDARD,
                getAllIds()
            )
        );
        nextId = new AtomicLong(1);
    }
}
