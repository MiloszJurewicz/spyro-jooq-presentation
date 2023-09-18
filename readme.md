Plan of presentation

- Why use sql builder? orm, vs low level sql like jdbc template
- What are cool features of jooq and why it might be worth looking at it
- Simple non type safe sql, boring
- Simple type safe queries, with type safe mappings
- Complex queries with type safety and mappings

## What is jooq - jOOQ java Object Oriented Querying
- sql query builder
- light database-mapping software library in Java that implements the active record pattern.
- abstraction over jdbc, close to sql, not orm
- type safe
- vendor neutral
- fluent api

## Why use query builder? 

Orm vs QueryBuilder vs plain sql discussion



#### Why use orm? Why not use orm? 

<details>
  <summary>ORM</summary>
Some of the good
- Fast development, Allow for really quick prototyping if you don't have schema locked yet
- No SQL 
- Caching done for you
- Crud for free
- Less boilerplate
- Sometimes you end up writing your own ORM so why not use something that already works

Some of the bad
- Can get slow very quickly as you don't really know what sql will be executed without looking under the hood
- When using an ORM, rather than getting further from SQL (which seems to be the intent), you spend tweaking ORM to generate performant SQL;
- For complex queries you often end writing sql anyway in inferior sql dialect (JPQL)
- The object-relational mismatch

- Another layer that you need to learn, Lots of "gotchas" when using ORMs. Example for jpa specifically:
  - [n+1](https://medium.com/geekculture/resolve-hibernate-n-1-problem-f0e049e689ab)
  - [Hibernate Lifecycle | States in Hibernate: Transient, Persistent, Detached, Removed](https://nikhilsukhani.medium.com/hibernate-lifecycle-states-in-hibernate-transient-persistent-detached-removed-40ba2f689b07)
  - FetchType.LAZY vs FetchType.EAGER - [LazyInitializationException](https://thorben-janssen.com/lazyinitializationexception/)
  - Pessimistic Locking vs Optimistic Locking - [OptimisticLockException](https://www.baeldung.com/jpa-optimistic-locking)
  - [Identity - equals & hashcode](https://thorben-janssen.com/ultimate-guide-to-implementing-equals-and-hashcode-with-hibernate/#when-and-why-you-need-to-implement-equals-and-hashcode)
  - [Open Session in View anti-pattern](https://vladmihalcea.com/the-open-session-in-view-anti-pattern/) 
  - Like everywhere caching is hard and comes [with its own problems](https://blog.lunatech.com/posts/2020-03-23-when-hibernate-caching-can-go-wrong)

</details>

#### PlainSql

<details>
  <summary>Plain SQL?</summary>

Good

Bad
- No typesafety
- No syntax safety
- No bind value index safety
- Verbose SQL String concatenation
- Boring bind value indexing techniques
- Verbose resource and exception handling in JDBC
- A very "stateful", not very object-oriented JDBC API, which is hard to use
</details>

#### QueryBuilder
TODO
https://www.jooq.org/doc/3.19/manual-single-page/#use-cases

## Basic showcase


## Code generation

You can use jooq code gen functionality to generate classes based on your db, those later can be used to write typesafe
queries.
Full tutorial for that [here](https://www.jooq.org/doc/latest/manual/code-generation/codegen-configuration/).

You can run codegen programatically, from gradle and from maven. Codegen itself is fairly powerful and has a bunch of
configuration options.

In this example I used maven. We also use flyway for migrations
versioning. [Jooq with flyway using maven](https://www.jooq.org/doc/latest/manual/getting-started/tutorials/jooq-with-flyway/)

### To generate code you can either

#### - Run codegen in connection free mode where you don't have to connect to actual database

- The JPADatabase, if you have a pre-existing JPA entity based meta model.
- The XMLDatabase, if you have some form of XML version of your schema, which you can XSL transform to jOOQ’s format
- The DDLDatabase, which can interpret your DDL scripts, e.g. the ones you pass to Flyway, or the ones produced by
  pg_dump.
- The LiquibaseDatabase, which simulates a Liquibase database migration and uses the simulated database output as a
  source for meta information of the code generator

But all of the above have the same limitation. You can’t really use many vendor-specific features, such as advanced
stored procedures, data types, etc.

#### - Run codegen against real database.

But this comes with its own set of challenges.

- you need to have database locally/remotely with all migrations applied.
- you need to make sure you have all migrations applied before launching codegen.
- you most likely want same database vendor as in production, so you can use vendor specific features. (So h2 works
  but )
- you want this process to be streamlined and easy for developers.

Initially solved this with approach suggested by jooq [blog post](https://blog.jooq.org/using-testcontainers-to-generate-jooq-code/) 
using test containers and maven plugins. 
But then I found that there is [plugin](https://testcontainers.com/guides/working-with-jooq-flyway-using-testcontainers/) that already does all necessary steps 

Old approach.

- we use groovy-maven-plugin to run container with postgres in it
- we run flyway plugin for migrations against that container
- we run jooq codegen against that container
- (optionally) we KEEP container to use for any integration tests if we have them
- we run tests using whatever plugin we want
- we kill container using groovy-maven-plugin

### Other questions to consider?

- Should generated code be part of version control?
  Or should it be derived artifact generated during project build
  time -https://www.jooq.org/doc/latest/manual/code-generation/codegen-version-control/
