package com.beumuth.collections.core.setelement;

import com.beumuth.collections.core.database.CollectionsBeanPropertyRowMapper;
import com.beumuth.collections.core.database.DatabaseService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class SetElementService {

    private static final CollectionsBeanPropertyRowMapper<SetElement> ROW_MAPPER =
        CollectionsBeanPropertyRowMapper.newInstance(SetElement.class);

    @Autowired
    private DatabaseService databaseService;

    public boolean doesSetElementExist(long id) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                    "SELECT 1 FROM SetElement WHERE id=:id",
                    ImmutableMap.of("id", id),
                    Boolean.class
                );
        } catch(EmptyResultDataAccessException e) {
            return false;
        }
    }

    public boolean doesSetElementExistWithNaturalKey(long idSet, long idElement) {
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

    public Optional<SetElement> getSetElement(long id) {
        try {
            return Optional.of(
                databaseService
                    .getNamedParameterJdbcTemplate()
                    .queryForObject(
                    "SELECT " +
                            "id, " +
                            "idSet, " +
                            "idElement " +
                        "FROM " +
                            "SetElement " +
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

    public Optional<SetElement> getSetElementByNaturalKey(long idSet, long idElement) {
        try {
            return Optional.of(
                databaseService
                    .getNamedParameterJdbcTemplate()
                    .queryForObject(
                    "SELECT " +
                            "id, " +
                            "idSet, " +
                            "idElement " +
                        "FROM " +
                            "SetElement " +
                        "WHERE " +
                            "idSet = :idSet AND " +
                            "idElement = :idElement",
                        ImmutableMap.of(
                        "idSet", idSet,
                        "idElement", idElement
                        ),
                        ROW_MAPPER
                    )
            );
        } catch(EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public long createSetElement(CreateSetElementRequest request) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        databaseService
            .getNamedParameterJdbcTemplate()
            .update(
            "INSERT INTO SetElement (idSet, idElement) VALUES (:idSet, :idElement)",
                new MapSqlParameterSource(
                    ImmutableMap.of(
                    "idSet", request.getIdSet(),
                    "idElement", request.getIdElement()
                    )
                ),
                keyHolder
            );
        return keyHolder.getKey().longValue();
    }

    public Set<Long> createSetElements(Set<CreateSetElementRequest> requests) {
       Set<Long> ids = Sets.newHashSet();
       for(CreateSetElementRequest request : requests) {
           ids.add(createSetElement(request));
       }
       return ids;
    }

    public void deleteSetElement(long id) {
        databaseService
            .getNamedParameterJdbcTemplate()
            .update(
            "DELETE FROM SetElement WHERE id=:id",
                ImmutableMap.of("id", id)
            );
    }
}
