package com.beumuth.math.core.settheory.element;

import com.beumuth.math.core.internal.database.MathBeanPropertyRowMapper;
import com.beumuth.math.core.internal.database.DatabaseService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class ElementService {

    private static final MathBeanPropertyRowMapper<Element> ROW_MAPPER =
        MathBeanPropertyRowMapper.newInstance(Element.class);

    @Autowired
    private DatabaseService databaseService;

    public boolean doesElementExist(long id) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                    "SELECT 1 FROM Element WHERE id=:id",
                    ImmutableMap.of("id", id),
                    Boolean.class
                );
        } catch(EmptyResultDataAccessException e) {
            return false;
        }
    }

    public boolean doesElementExistWithNaturalKey(long idSet, long idObject) {
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

    public Optional<Element> getElement(long id) {
        try {
            return Optional.of(
                databaseService
                    .getNamedParameterJdbcTemplate()
                    .queryForObject(
                    "SELECT " +
                            "id, " +
                            "idSet, " +
                            "idObject " +
                        "FROM " +
                            "Element " +
                        "WHERE " +
                            "id = :id",
                        ImmutableMap.of("id", id),
                        ROW_MAPPER
                    )
            );
        } catch(EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Element> getElementByNaturalKey(long idSet, long idObject) {
        try {
            return Optional.of(
                databaseService
                    .getNamedParameterJdbcTemplate()
                    .queryForObject(
                    "SELECT " +
                            "id, " +
                            "idSet, " +
                            "idObject " +
                        "FROM " +
                            "Element " +
                        "WHERE " +
                            "idSet = :idSet AND " +
                            "idObject = :idObject",
                        ImmutableMap.of(
                        "idSet", idSet,
                        "idObject", idObject
                        ),
                        ROW_MAPPER
                    )
            );
        } catch(EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public long createElement(CreateElementRequest request) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        databaseService
            .getNamedParameterJdbcTemplate()
            .update(
            "INSERT INTO Element (idSet, idObject) VALUES (:idSet, :idObject)",
                new MapSqlParameterSource(
                    ImmutableMap.of(
                    "idSet", request.getIdSet(),
                    "idObject", request.getIdObject()
                    )
                ),
                keyHolder
            );
        return keyHolder.getKey().longValue();
    }

    public Set<Long> createElements(Set<CreateElementRequest> requests) {
       Set<Long> ids = Sets.newHashSet();
       for(CreateElementRequest request : requests) {
           ids.add(createElement(request));
       }
       return ids;
    }

    public void deleteElement(long id) {
        databaseService
            .getNamedParameterJdbcTemplate()
            .update(
            "DELETE FROM Element WHERE id=:id",
                ImmutableMap.of("id", id)
            );
    }
}
