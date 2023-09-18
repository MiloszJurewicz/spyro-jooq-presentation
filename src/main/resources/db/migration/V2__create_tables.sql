create table users
(
    id         bigserial not null,
    name       varchar   not null,
    email      varchar   not null,
    created_at timestamp,
    updated_at timestamp,
    primary key (id),
    constraint user_email_unique unique (email)
);

create table posts
(
    id         bigserial                    not null,
    title      varchar                      not null,
    content    varchar                      not null,
    created_by bigint references users (id) not null,
    created_at timestamp,
    updated_at timestamp,
    primary key (id)
);

create table comments
(
    id         bigserial                    not null,
    name       varchar                      not null,
    content    varchar                      not null,
    post_id    bigint references posts (id) not null,
    created_at timestamp,
    updated_at timestamp,
    primary key (id)
);

ALTER SEQUENCE users_id_seq RESTART WITH 101;
ALTER SEQUENCE posts_id_seq RESTART WITH 101;
ALTER SEQUENCE comments_id_seq RESTART WITH 101;


CREATE TABLE language
(
    id          INT     NOT NULL PRIMARY KEY,
    cd          CHAR(2) NOT NULL,
    description VARCHAR(50)
);

CREATE TABLE author
(
    id            INT         NOT NULL PRIMARY KEY,
    first_name    VARCHAR(50),
    last_name     VARCHAR(50) NOT NULL,
    date_of_birth DATE,
    year_of_birth INT,
    distinguished INT
);

CREATE TABLE book
(
    id           INT          NOT NULL PRIMARY KEY,
    author_id    INT          NOT NULL,
    title        VARCHAR(400) NOT NULL,
    published_in INT          NOT NULL,
    language_id  INT          NOT NULL,

    CONSTRAINT fk_book_author FOREIGN KEY (author_id) REFERENCES author (id),
    CONSTRAINT fk_book_language FOREIGN KEY (language_id) REFERENCES language (id)
);

CREATE TABLE book_store
(
    name VARCHAR(400) NOT NULL UNIQUE
);

CREATE TABLE book_to_book_store
(
    name    VARCHAR(400) NOT NULL,
    book_id INTEGER      NOT NULL,
    stock   INTEGER,

    PRIMARY KEY (name, book_id),
    CONSTRAINT fk_b2bs_book_store FOREIGN KEY (name) REFERENCES book_store (name) ON DELETE CASCADE,
    CONSTRAINT fk_b2bs_book FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE CASCADE
);