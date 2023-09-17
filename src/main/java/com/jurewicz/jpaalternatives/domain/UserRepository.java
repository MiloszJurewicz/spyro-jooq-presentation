package com.jurewicz.jpaalternatives.domain;

import static com.testcontainers.demo.jooq.tables.Users.USERS;
import static org.jooq.Records.mapping;

import java.time.LocalDateTime;
import java.util.Optional;
import org.jooq.DSLContext;
import org.jooq.Record3;
import org.jooq.SelectConditionStep;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
    private final DSLContext dsl;

    public UserRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public User createUser(User user) {
        return this.dsl
                .insertInto(USERS)
                .set(USERS.NAME, user.name())
                .set(USERS.EMAIL, user.email())
                .set(USERS.CREATED_AT, LocalDateTime.now())
                .returningResult(USERS.ID, USERS.NAME, USERS.EMAIL)
                .fetchOne(mapping(User::new));
    }

    public Optional<User> getUserByEmail(String email) {
        return this.dsl
                .select(USERS.ID, USERS.NAME, USERS.EMAIL)
                .from(USERS)
                .where(USERS.EMAIL.equalIgnoreCase(email))
                .fetchOptional(mapping(User::new));
    }
}