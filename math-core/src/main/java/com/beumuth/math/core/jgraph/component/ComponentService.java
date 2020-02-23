package com.beumuth.math.core.jgraph.component;

import com.beumuth.math.client.jgraph.Element;
import com.beumuth.math.core.internal.database.DatabaseService;
import com.beumuth.math.core.internal.database.MathBeanPropertyRowMapper;
import com.beumuth.math.core.jgraph.element.ElementService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
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
    public Set<Long> getComponentIds(long id) {
        try {
            return Sets.newHashSet(
                databaseService
                    .getNamedParameterJdbcTemplate()
                    .queryForList(
                        "WITH RECURSIVE neighbors (id) AS (" +
                                "SELECT id FROM JGraphElement WHERE id=:id " +
                            "UNION ALL " +
                                "SELECT j.id " +
                                    "FROM JGraphElement j INNER JOIN neighbors " +
                                      "ON j.id != neighbors.id AND (j.a=neighbors.id OR j.b=neighbors.id)" +
                        ") SELECT DISTINCT id FROM neighbors",
                        ImmutableMap.of("id", id),
                        Long.class
                    )
            );
        } catch(EmptyResultDataAccessException e) {
            return Sets.newHashSet();
        }
    }

    /**
     * Get the component of the Element with the given id; that is, the set of Elements connected to it.
     * @param id
     */
    public Set<Element> getComponent(long id) {
        try {
            return Sets.newHashSet(
                databaseService
                    .getNamedParameterJdbcTemplate()
                    .queryForList(
                        "WITH RECURSIVE neighbors (id, a, b) AS (" +
                                "SELECT id, a, b FROM JGraphElement WHERE id=:id " +
                            "UNION ALL " +
                                "SELECT j.id, j.a, j.b " +
                                    "FROM JGraphElement j INNER JOIN neighbors " +
                                        "ON j.id != neighbors.id AND (j.a = neighbors.id OR j.b = neighbors.id)" +
                            ") SELECT id, a, b FROM neighbors",
                        ImmutableMap.of("id", id),
                        Element.class
                    )
            );
        } catch(EmptyResultDataAccessException e) {
            return Sets.newHashSet();
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
