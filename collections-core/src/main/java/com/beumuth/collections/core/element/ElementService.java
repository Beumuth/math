package com.beumuth.collections.core.element;

import com.beumuth.collections.client.element.Element;
import com.beumuth.collections.core.database.CollectionsBeanPropertyRowMapper;
import com.beumuth.collections.core.database.DatabaseService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class ElementService {
    private static final CollectionsBeanPropertyRowMapper<Element> ROW_MAPPER =
        CollectionsBeanPropertyRowMapper.newInstance(Element.class);

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

    public Set<Long> getElementsThatDoNotExist(Set<Long> idElements) {
        if(idElements.isEmpty()) {
            return idElements;
        }

        return Sets.difference(
            idElements,
            Sets.newHashSet(
                databaseService
                    .getNamedParameterJdbcTemplate()
                    .queryForList(
                    "SELECT id FROM Element WHERE id IN (:idElements)",
                        ImmutableMap.of(
                        "idElements", idElements
                        ),
                        Long.class
                    )
            )
        );
    }

    public Optional<Element> getElement(long id) {
        try {
            return Optional.of(
                databaseService
                    .getNamedParameterJdbcTemplate()
                    .queryForObject(
                    "SELECT id FROM Element WHERE id=:id",
                        ImmutableMap.of("id", id),
                        ROW_MAPPER
                    )
            );
        } catch(EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public long createElement() {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        databaseService
            .getNamedParameterJdbcTemplate()
            .update(
                "INSERT INTO Element (id) VALUES (null)",
                new MapSqlParameterSource(),
                keyHolder
            );

        return keyHolder.getKey().longValue();
    }

    public List<Long> createMultipleElements(int number) {

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        String values = "(null),".repeat(number);
        values = values.substring(0, values.length()-1);

        databaseService
            .getNamedParameterJdbcTemplate()
            .update(
                "INSERT INTO Element (id) VALUES " + values,
                new MapSqlParameterSource(),
                keyHolder
            );

        List<Map<String, Object>> keyList = keyHolder.getKeyList();
        List<Long> ids = Lists.newArrayList();
        for(var i = 0; i < keyList.size(); ++i) {
            ids.add(
                ((BigInteger)
                    keyList
                        .get(i)
                        .get("GENERATED_KEY")
                ).longValue()
            );
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

    public void deleteMultipleElements(Set<Long> ids) {
        databaseService
            .getNamedParameterJdbcTemplate()
            .update(
                "DELETE FROM Element WHERE id IN (:ids)",
                ImmutableMap.of("ids", ids)
            );
    }
}
