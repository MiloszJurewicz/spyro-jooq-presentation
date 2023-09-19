package com.jurewicz.jpaalternatives.domain;

import com.testcontainers.demo.jooq.Keys;
import com.testcontainers.demo.jooq.tables.Author;
import com.testcontainers.demo.jooq.tables.records.AuthorRecord;
import com.testcontainers.demo.jooq.tables.records.BookRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.stream.Stream;

import static com.testcontainers.demo.jooq.tables.Author.AUTHOR;
import static com.testcontainers.demo.jooq.tables.Book.BOOK;
import static org.junit.Assert.assertEquals;

/**
 * https://www.jooq.org/doc/latest/manual/sql-execution/fetching/#various-modes-of-fetching
 * https://blog.jooq.org/the-many-different-ways-to-fetch-data-in-jooq/
 */
@SpringBootTest
@Sql("/test-data.sql")
@Testcontainers
public class JooqAsQueryExecutor {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.3-alpine");

    @Autowired
    DSLContext dsl;


    @Test
    void shouldFetchQuery() {
        var where = dsl.select(BOOK.TITLE, AUTHOR.FIRST_NAME, AUTHOR.LAST_NAME)
                .from(BOOK)
                .join(AUTHOR)
                .on(BOOK.AUTHOR_ID.eq(AUTHOR.ID))
                .where(BOOK.PUBLISHED_IN.eq(1950));

        Result<Record3<String, String, String>> fetchResult = where.fetch();
        @Nullable Record3<String, String, String> fetchOneResult = where.fetchOne();
        try {
            @NotNull Record3<String, String, String> fetchSingleResult = where.fetchSingle(); //Throws Exception if not found!
        } catch (DataAccessException e) {
            //
        }
        @NotNull Optional<Record3<String, String, String>> fetchOptionalResult = where.fetchOptional();
    }

    //TODO
    @Test
    void todoNameMe() {
        BookRecord book = dsl.fetchSingle(BOOK, BOOK.ID.eq(4));

        // Find the author of a book (static imported from Keys)
        AuthorRecord author = book.fetchParent(Keys.BOOK__FK_BOOK_AUTHOR);

        System.out.println(author.toString());

        // Find other books by that author
        Result<BookRecord> books = author.fetchChildren(Keys.BOOK__FK_BOOK_AUTHOR);
        System.out.println(books.toString());
    }

    @Test
    void todoNameMe2() {
        var nested = dsl.select(BOOK.AUTHOR_ID)
                .from(BOOK)
                .where(BOOK.ID.eq(4))
                .limit(1);

        var author =  dsl.select(AUTHOR.ID)
                .from(AUTHOR)
                .where(AUTHOR.ID.eq(nested))
                .limit(1);

        Query where = dsl.select().from(BOOK).where(BOOK.AUTHOR_ID.eq(author));

        String sql = where.getSQL();
        assertEquals(sql, "");
    }


    @Test
    void shouldFetchAsync() {
        //Lazy fetching
        try (Cursor<BookRecord> cursor = dsl.selectFrom(BOOK).fetchLazy()) {
            // Cursor has similar methods as Iterator<R>
            while (cursor.hasNext()) {
                BookRecord book = cursor.fetchNext();
                //Do something with book
                System.out.println(book);
            }
        }
        //Steam lazy fetching
        try (Stream<BookRecord> stream = dsl.selectFrom(BOOK).stream()) {
            stream.forEach(System.out::println);
        }

    }
}
