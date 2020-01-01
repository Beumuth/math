package com.beumuth.math.core.settheory.orderedpair;

import com.beumuth.math.client.settheory.orderedpair.CrupdateOrderedPairRequest;
import com.beumuth.math.client.settheory.orderedpair.OrderedPair;
import com.beumuth.math.core.internal.database.DatabaseService;
import com.beumuth.math.core.internal.database.MathBeanPropertyRowMapper;
import com.beumuth.math.core.settheory.object.ObjectService;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrderedPairService {

    private static final MathBeanPropertyRowMapper<OrderedPair> ROW_MAPPER =
        MathBeanPropertyRowMapper.newInstance(OrderedPair.class);

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private ObjectService objectService;

    public boolean doesOrderedPairExist(long id) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                    "SELECT 1 FROM OrderedPair WHERE id=:id",
                    ImmutableMap.of("id", id),
                    Boolean.class
                );
        } catch(EmptyResultDataAccessException e) {
            return false;
        }
    }

    public Optional<OrderedPair> getOrderedPair(long id) {
        try {
            return Optional.of (
                databaseService
                    .getNamedParameterJdbcTemplate()
                    .queryForObject(
                        "SELECT " +
                            "id, " +
                            "idObject, " +
                            "idLeft, " +
                            "idRight " +
                        "FROM " +
                            "OrderedPair " +
                        "WHERE " +
                            "id=:id",
                        ImmutableMap.of("id", id),
                        ROW_MAPPER
                    )
            );
        } catch(EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public long createOrderedPair(CrupdateOrderedPairRequest request) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        databaseService
            .getNamedParameterJdbcTemplate()
            .update(
                "INSERT INTO OrderedPair ( " +
                    "idObject, " +
                    "idLeft, " +
                    "idRight " +
                ") VALUES ( " +
                    ":idObject, " +
                    ":idLeft, " +
                    ":idRight " +
                ")",
                new MapSqlParameterSource(
                    ImmutableMap.of(
                        "idObject", objectService.createObject(),
                        "idLeft", request.getIdLeft(),
                        "idRight", request.getIdRight()
                    )
                ),
                keyHolder
            );
        return keyHolder.getKey().longValue();
    }

    public void updateOrderedPair(long id, CrupdateOrderedPairRequest request) {
        databaseService
            .getNamedParameterJdbcTemplate()
            .update(
                "UPDATE " +
                    "OrderedPair " +
                "SET " +
                    "idLeft=:idLeft, " +
                    "idRight=:idRight " +
                "WHERE " +
                    "id=:id",
                new MapSqlParameterSource(
                    ImmutableMap.of(
                        "id", id,
                        "idLeft", request.getIdLeft(),
                        "idRight", request.getIdRight()
                    )
                )
            );
    }

    public void deleteOrderedPair(long id) {
        //Delete both the OrderedPair and the OrderedPairClient's Object at the same time - cascading delete.
        databaseService
            .getNamedParameterJdbcTemplate()
            .update(
                "DELETE FROM Object WHERE id = ( " +
                    "SELECT idObject FROM OrderedPair WHERE id=:id" +
                ")",
                ImmutableMap.of("id", id)
            );
    }

    /**
     * Swap the left and right objects of the OrderedPair.
     * @param id
     */
    public void swap(long id) {
        databaseService
            .getNamedParameterJdbcTemplate()
            .update(
                "UPDATE " +
                    "OrderedPair " +
                "SET " +
                    "idLeft=(@temp:=idLeft), " +
                    "idLeft=idRight, " +
                    "idRight=@temp " +
                "WHERE id=:id",
                ImmutableMap.of("id", id)
            );
    }
}
