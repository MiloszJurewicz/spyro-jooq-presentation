package com.jurewicz.jpaalternatives.domain;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.jooq.impl.DSL.count;

@SpringBootTest
@Sql("/test-data.sql")
@Testcontainers
public class JooqQueryMapping {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.3-alpine");

    /**
     * DSLContext references an org.jooq.Configuration, an object that configures jOOQ's behaviour when executing queries (see SQL execution for more details).
     * Unlike the static DSL, the DSLContext allow for creating SQL statements that are already "configured" and ready for execution.
     */
    @Autowired
    DSLContext dsl;


}
