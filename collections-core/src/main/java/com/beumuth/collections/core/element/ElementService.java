package com.beumuth.collections.core.element;

import com.beumuth.collections.client.element.Element;
import com.beumuth.collections.core.database.CollectionsBeanPropertyRowMapper;
import com.beumuth.collections.core.database.DatabaseService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ElementService {
    private static final CollectionsBeanPropertyRowMapper<Element> ROW_MAPPER =
        CollectionsBeanPropertyRowMapper.newInstance(Element.class);

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private GeneratedKeyHolder generatedKeyHolder;

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
        databaseService
            .getJdbcTemplate()
            .update(
                "INSERT INTO Element (id) VALUES(null)",
                generatedKeyHolder
            );
        return generatedKeyHolder.getKey().longValue();
    }

    public List<Long> createMultipleElements(int number) {
        try {
            PreparedStatement preparedStatement = databaseService
                .getCollectionsDataSource()
                .getConnection()
                .prepareStatement("INSERT INTO Element () VALUES (null)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.executeUpdate();

            ResultSet generatedKeysResultSet = preparedStatement.getGeneratedKeys();
            List<Long> generatedKeys = Lists.newArrayList();
            while(generatedKeysResultSet.next()) {
                generatedKeys.add(generatedKeysResultSet.getLong("id"));
            }
            return generatedKeys;
        } catch(SQLException e) {
            throw new RuntimeException("Could not create multiple elements", e);
        }
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
