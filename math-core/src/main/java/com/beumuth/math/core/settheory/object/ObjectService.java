package com.beumuth.math.core.settheory.object;

import com.beumuth.math.client.settheory.object.Object;
import com.beumuth.math.core.internal.database.MathBeanPropertyRowMapper;
import com.beumuth.math.core.internal.database.DatabaseService;
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
public class ObjectService {
    private static final MathBeanPropertyRowMapper<Object> ROW_MAPPER =
        MathBeanPropertyRowMapper.newInstance(Object.class);

    @Autowired
    private DatabaseService databaseService;

    public boolean doesObjectExist(long id) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                "SELECT 1 FROM Object WHERE id=:id",
                    ImmutableMap.of("id", id),
                    Boolean.class
                );

        } catch(EmptyResultDataAccessException e) {
            return false;
        }
    }

    public Set<Long> getObjectsThatDoNotExist(Set<Long> idObjects) {
        if(idObjects.isEmpty()) {
            return idObjects;
        }

        return Sets.difference(
            idObjects,
            Sets.newHashSet(
                databaseService
                    .getNamedParameterJdbcTemplate()
                    .queryForList(
                        "SELECT id FROM Object WHERE id IN (:idObjects)",
                        ImmutableMap.of("idObjects", idObjects),
                        Long.class
                    )
            )
        );
    }

    public Optional<Object> getObject(long id) {
        try {
            return Optional.of(
                databaseService
                    .getNamedParameterJdbcTemplate()
                    .queryForObject(
                    "SELECT id FROM Object WHERE id=:id",
                        ImmutableMap.of("id", id),
                        ROW_MAPPER
                    )
            );
        } catch(EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public long createObject() {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        databaseService
            .getNamedParameterJdbcTemplate()
            .update(
                "INSERT INTO Object (id) VALUES (null)",
                new MapSqlParameterSource(),
                keyHolder
            );

        return keyHolder.getKey().longValue();
    }

    public List<Long> createMultipleObjects(int number) {

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        String values = "(null),".repeat(number);
        values = values.substring(0, values.length()-1);

        databaseService
            .getNamedParameterJdbcTemplate()
            .update(
                "INSERT INTO Object (id) VALUES " + values,
                new MapSqlParameterSource(),
                keyHolder
            );

        List<Map<String, java.lang.Object>> keyList = keyHolder.getKeyList();
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

    public void deleteObject(long id) {
        databaseService
            .getNamedParameterJdbcTemplate()
            .update(
            "DELETE FROM Object WHERE id=:id",
                ImmutableMap.of("id", id)
            );
    }

    public void deleteMultipleObjects(Set<Long> ids) {
        databaseService
            .getNamedParameterJdbcTemplate()
            .update(
                "DELETE FROM Object WHERE id IN (:ids)",
                ImmutableMap.of("ids", ids)
            );
    }
}
