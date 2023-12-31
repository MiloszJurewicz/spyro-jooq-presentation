package com.jurewicz.jpaalternatives.domain;

import org.jooq.DSLContext;
import org.jooq.Query;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Sql("/test-data.sql")
@Testcontainers
class DummyServiceTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.3-alpine");

    /**
     * DSLContext references an org.jooq.Configuration, an object that configures jOOQ's behaviour when executing queries (see SQL execution for more details).
     * Unlike the static DSL, the DSLContext allow for creating SQL statements that are already "configured" and ready for execution.
     */
    @Autowired
    DSLContext dsl;

    /**
     * The SQL string built with the jOOQ query DSL can then be executed using JDBC directly, using Spring's JdbcTemplate, using Apache DbUtils and many other tools
     */
    @Test
    void shouldCreateQueryWithoutCodegen() {
        Query query = dsl.select(field("BOOK.TITLE"), field("AUTHOR.FIRST_NAME"), field("AUTHOR.LAST_NAME"))
                .from(table("BOOK"))
                .join(table("AUTHOR"))
                .on(field("BOOK.AUTHOR_ID").eq(field("AUTHOR.ID")))
                .where(field("BOOK.PUBLISHED_IN").eq(1948));
        List<Object> bindValues = query.getBindValues();

        assertEquals(query.getSQL(), "select BOOK.TITLE, AUTHOR.FIRST_NAME, AUTHOR.LAST_NAME from BOOK join AUTHOR on BOOK.AUTHOR_ID = AUTHOR.ID where BOOK.PUBLISHED_IN = ?");
        assertEquals(bindValues.get(0), 1948);
    }
}