package com.jurewicz.jpaalternatives.domain;

import org.checkerframework.checker.units.qual.A;
import org.jooq.*;
import org.jooq.Record;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static com.testcontainers.demo.jooq.tables.Author.AUTHOR;
import static com.testcontainers.demo.jooq.tables.Book.BOOK;
import static com.testcontainers.demo.jooq.tables.BookStore.BOOK_STORE;
import static com.testcontainers.demo.jooq.tables.BookToBookStore.BOOK_TO_BOOK_STORE;
import static org.jooq.impl.DSL.*;
import static org.junit.Assert.assertEquals;

@SpringBootTest
@Sql("/test-data.sql")
@Testcontainers
public class JooqAsQueryBuilder {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.3-alpine");

    /**
     * DSLContext references an org.jooq.Configuration, an object that configures jOOQ's behaviour when executing queries (see SQL execution for more details).
     * Unlike the static DSL, the DSLContext allow for creating SQL statements that are already "configured" and ready for execution.
     */
    @Autowired
    DSLContext dsl;

    @Test
    void shouldCreateQueryWithoutCodegen() {
        Query query = dsl.select(field("BOOK.TITLE"), field("AUTHOR.FIRST_NAME"), field("AUTHOR.LAST_NAME"))
                .from(table("BOOK"))
                .join(table("AUTHOR"))
                .on(field("BOOK.AUTHOR_ID").eq(field("AUTHOR.ID")))
                .where(field("BOOK.PUBLISHED_IN").eq(1948));
        List<Object> bindValues = query.getBindValues();

        /**
         * The SQL string built with the jOOQ query DSL can then be executed
         * using JDBC directly, using Spring's JdbcTemplate, using Apache DbUtils and many other tools
         */
        assertEquals("select BOOK.TITLE, AUTHOR.FIRST_NAME, AUTHOR.LAST_NAME from BOOK join AUTHOR on BOOK.AUTHOR_ID = AUTHOR.ID where BOOK.PUBLISHED_IN = ?", query.getSQL());
        assertEquals(1948, bindValues.get(0));
    }

    @Test
    void shouldCreateQueryWithCodegen() {
        Query query = dsl.select(BOOK.TITLE, AUTHOR.FIRST_NAME, AUTHOR.LAST_NAME)
                .from(BOOK)
                .join(AUTHOR)
                .on(BOOK.AUTHOR_ID.eq(AUTHOR.ID))
                .where(BOOK.PUBLISHED_IN.eq(1948));

        String sql = query.getSQL();
        List<Object> bindValues = query.getBindValues();

        assertEquals(
                "select \"public\".\"book\".\"title\", \"public\".\"author\".\"first_name\", \"public\".\"author\".\"last_name\" from \"public\".\"book\" join \"public\".\"author\" on \"public\".\"book\".\"author_id\" = \"public\".\"author\".\"id\" where \"public\".\"book\".\"published_in\" = ?",
                query.getSQL()
        );
        assertEquals(1948, bindValues.get(0));
    }

    @Test
    void findALlBooksOfAuthorOfSpecificBook() {
        var nested = dsl.select(BOOK.AUTHOR_ID)
                .from(BOOK)
                .where(BOOK.ID.eq(4))
                .limit(1);

        var author = dsl.select(AUTHOR.ID)
                .from(AUTHOR)
                .where(AUTHOR.ID.eq(nested))
                .limit(1);

        Query where = dsl.select().from(BOOK).where(BOOK.AUTHOR_ID.eq(author));

        String sql = where.getSQL();
        assertEquals(sql, "");
    }

    // Call "join" directly on the AUTHOR table
    @Test
    void findALlBooksOfAuthorUsingJoin() {
        var nested = dsl.select(BOOK.AUTHOR_ID)
                .from(BOOK)
                .where(BOOK.ID.eq(4))
                .limit(1);

        var query = dsl.select()
                .from(AUTHOR.join(BOOK)
                                .on(BOOK.AUTHOR_ID.eq(AUTHOR.ID))
                                .where(AUTHOR.ID.eq(nested)));


        String sql = query.getSQL();
        assertEquals(sql, "");
    }

    @Test
    void findAllAuthorsAvailableInStore() {
        var query = dsl.selectDistinct(AUTHOR.FIRST_NAME, AUTHOR.LAST_NAME)
                .from(BOOK_STORE)
                .join(BOOK_TO_BOOK_STORE).on(BOOK_TO_BOOK_STORE.NAME.eq(BOOK_STORE.NAME))
                .join(BOOK).on(BOOK_TO_BOOK_STORE.BOOK_ID.eq(BOOK.ID))
                .join(AUTHOR).on(AUTHOR.ID.eq(BOOK.AUTHOR_ID));

        String sql = query.getSQL();
//        assertEquals(sql, "");

        var fetch = query.fetch();
        System.out.println(fetch.toString());
    }

    @Test
    void shouldNestJoins(){
        var query = dsl.select()
                .from(AUTHOR
                        .leftOuterJoin(BOOK
                                .join(BOOK_TO_BOOK_STORE)
                                .on(BOOK_TO_BOOK_STORE.BOOK_ID.eq(BOOK.ID)))
                        .on(BOOK.AUTHOR_ID.eq(AUTHOR.ID)));

        String sql = query.getSQL();
        assertEquals(sql, "select \"public\".\"author\".\"id\", \"public\".\"author\".\"first_name\", \"public\".\"author\".\"last_name\", \"public\".\"author\".\"date_of_birth\", \"public\".\"author\".\"year_of_birth\", \"public\".\"author\".\"distinguished\", \"public\".\"book\".\"id\", \"public\".\"book\".\"author_id\", \"public\".\"book\".\"title\", \"public\".\"book\".\"published_in\", \"public\".\"book\".\"language_id\", \"public\".\"book_to_book_store\".\"name\", \"public\".\"book_to_book_store\".\"book_id\", \"public\".\"book_to_book_store\".\"stock\" from \"public\".\"author\" left outer join (\"public\".\"book\" join \"public\".\"book_to_book_store\" on \"public\".\"book_to_book_store\".\"book_id\" = \"public\".\"book\".\"id\") on \"public\".\"book\".\"author_id\" = \"public\".\"author\".\"id\"");

        Result<Record> fetch = query.fetch();

        System.out.println(fetch.toString());
    }

}