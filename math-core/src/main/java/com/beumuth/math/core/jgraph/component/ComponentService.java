package com.beumuth.math.core.jgraph.component;

import com.beumuth.math.client.jgraph.Element;
import com.beumuth.math.client.settheory.orderedset.OrderedSet;
import com.beumuth.math.client.settheory.orderedset.OrderedSets;
import com.beumuth.math.core.internal.database.DatabaseService;
import com.beumuth.math.core.jgraph.element.ElementService;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ComponentService {
    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private ElementService elementService;

    /**
     * Get the component of the Element with the given id; that is, the set ids of Elements connected to it.
     * @param id
     */
    public OrderedSet<Long> getComponentIds(long id) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .query(
                    "WITH RECURSIVE neighbors (id, a, b) AS (" +
                            "SELECT id, a, b FROM JGraphElement WHERE id=:id " +
                        "UNION " +
                            "SELECT j.id, j.a, j.b " +
                                "FROM JGraphElement j INNER JOIN neighbors n ON " +
                                    "j.id=n.id OR " +
                                    "j.a=n.id OR " +
                                    "j.b=n.id OR " +
                                    "j.id=n.a OR " +
                                    "j.id=n.b " +
                    ") SELECT id FROM neighbors ORDER BY id",
                    ImmutableMap.of("id", id),
                    ElementService.ID_ORDERED_SET_EXTRACTOR
                );
        } catch(EmptyResultDataAccessException e) {
            return OrderedSets.empty();
        }
    }

    /**
     * Get the component of the Element with the given id; that is, the set of Elements connected to it.
     * @param id
     */
    public OrderedSet<Element> getComponent(long id) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .query(
                    "WITH RECURSIVE neighbors (id, a, b) AS (" +
                            "SELECT id, a, b FROM JGraphElement WHERE id=:id " +
                        "UNION " +
                            "SELECT j.id, j.a, j.b " +
                            "FROM JGraphElement j INNER JOIN neighbors n ON " +
                                "j.id=n.id OR " +
                                "j.a=n.id OR " +
                                "j.b=n.id OR " +
                                "j.id=n.a OR " +
                                "j.id=n.b " +
                        ") SELECT id, a, b FROM neighbors ORDER BY id",
                    ImmutableMap.of("id", id),
                    ElementService.ELEMENT_ORDERED_SET_EXTRACTOR
                );
        } catch(EmptyResultDataAccessException e) {
            return OrderedSets.empty();
        }
    }

    /**
     * Get the Set of components (ids only) for the Elements with given ids (no duplicates returned).
     */
    public Set<Set<Long>> getComponentsIds(Set<Long> ids) {
        return ids
            .stream()
            .map(this::getComponentIds)
            .collect(Collectors.toSet());
    }

    /**
     * Get the Set of components for the Elements with given ids (no duplicates returned).
     */
    public Set<Set<Element>> getComponents(Set<Long> ids) {
        return ids
            .stream()
            .map(this::getComponent)
            .collect(Collectors.toSet());
    }

    /**
     * Delete the component of the Element with the given id.
     * @param id
     */
    public void deleteComponent(long id) {
        elementService.deleteElements(getComponentIds(id));
    }

    /**
     * Delete the j-graph connected to the Element with the given id.
     * @param ids
     */
    public void deleteComponents(Set<Long> ids) {
        elementService.deleteElements(
            getComponentsIds(ids)
                .stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet())
        );
    }
}
