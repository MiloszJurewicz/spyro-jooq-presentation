Plan of presentation

- Why use sql builder? orm, vs low level sql like jdbc template
- What are cool features of jooq and why it might be worth looking at it
- Simple non type safe sql, boring
- Simple type safe queries, with type safe mappings
- Complex queries with type safety and mappings

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

Initially solved this with approach suggested by
jooq [blog post](https://blog.jooq.org/using-testcontainers-to-generate-jooq-code/)
using test containers and maven plugins. But then I found that there
is [plugin](https://testcontainers.com/guides/working-with-jooq-flyway-using-testcontainers/) that already does all
necessary steps

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
