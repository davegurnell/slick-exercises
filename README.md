# Slick Exercises

Examples and exercises for
[Essential Slick](https://underscore.io/training/courses/essential-slick).

Licensed [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0).

## Overview

There are two Scala projects in this repository:

- example - A small example app that connects to an H2 in-memory database.

- exercise - An almost-empty project that connects to a MySQL database.
  Most exercises in the course will involve writing code here
  to query and update this database.

The `exercise` project uses Docker to ensure a consistent environment.
There is a `docker-compose.yml` file that configures two containers:

- `app` - A container for your Scala code:

  - Has SBT, Scala, and a JVM pre-installed.
  - Has `/root` mapped to the repository root.
  - You can shell into it and run `sbt`.
  - It has network access to the `database` container to connect to MySQL...

- `database` - A MySQL container:

  - Has a database called `employees`.
  - Has two users - `dbuser` and `root` - both with the password `password`.
  - Has `/var/lib/mysql` mapped to `./mysql/docker-data` so your data is persisted between runs.

I've included a load of sample data from
[a dummy employee database](https://github.com/datacharmer/test_db) in `./sample-data`.
This is mapped to `/sample-data` in the `database` container.
There's a script called `./sample-data/import-data.sh`
that you can run from inside the `database` container to recreate the `employees` database.
See below for more information.

## Getting Started

You'll need:

- Scala/SBT/JVM
- Docker
- An editor (IntelliJ IDEA or VSCode will do)

Start by cloning the repo:

```bash
git clone https://github.com/spacecatio/slick-exercises
cd ./slick-exercises
```

Check your basic Scala and Slick setup by running the `example` project.
This will take a couple of minutes the first time you run it:

```bash
sbt example/run
# ... lots of output ...
#
# [info] running code.Main
# Album(Keyboard Cat,Keyboard Cat's Greatest Hits,2009,5,3)
# Album(Pink Floyd,Dark Side of the Moon,1978,5,5)
# Album(Daft Punk,Alive 2007,2007,4,1)
# Album(Spice Girls,Spice,1996,4,7)
# Album(Rick Astley,Whenever You Need Somebody,1987,3,6)
# [success]
```

Now check your Docker setup by starting the Docker containers.
This will take a couple of minutes the first time you run it:

```bash
docker compose up --wait --detach
```

Connect to the `database` container and run a handy script to set up some sample data.
This will take about a minute:

```bash
docker compose exec database bash
cd /sample-data/
./import-data.sh
# IMPORTING DATA
#
# ... lots of output ...
#
# ALL DONE! EMPLOYEE COUNT IS BELOW
#
# +----------+
# | count(*) |
# +----------+
# |   300024 |
# +----------+
```

Press `Ctrl+D` to exit the container and return to your own computer's shell.

Finally, run the `exercise` project and check that Scala can connect to the `database`:

```bash
docker compose exec app sbt
exercise/run
# ... lots of output ...
#
# Yay! There are 300024 employees in the database!
```

## Useful Commands

The following commands all assume you're in the repository root directory:

### Starting and Stopping Containers

```bash
# Start containers, wait until press Ctrl+C, stop containers, return to shell:
docker compose up

# Start containers, wait until started, return to shell while containers still running:
docker compose up --wait --detach

# Stop containers, wait until stopped, return to shell:
docker compose down
```

### Accessing Containers

```bash
# Run an interactive SBT on the `app` container:
docker compose exec app sbt

# Run an interactive shell on the `app` container:
docker compose exec app bash

# Run an interactive shell on the `database` container:
docker compose exec database bash
```

### Accessing the Database

When running a shell in the `app` container:

```bash
# Connect to the database:
mysql --host=database --database=employees --user=dbuser --password=password
```

When running a shell in the `database` container:

```bash
# Connect to the database:
mysql --host=database --database=employees --user=dbuser --password=password

# Recreate the employee database:
/sample-data/import-data.sh
```

### Interrogating the Database

At a MySQL prompt:

```sql
-- List the tables in the database
show tables;

-- Show the schema of a table
desc employees;

-- Regular SQL works, obviously
select * from employees limit 10;
```

## Connecting to the Database from Outside of Docker

The database server is externally visible on the host machine as `localhost:33060`.
You can access it with the following command:

```bash
mysql --host=localhost --port=33060 --protocol=tcp --database=employees --user=dbuser --password=password
```

You can use this in tools like _DataGrip_ to get an overview of the employee database:

- Host: `localhost`
- Port: `33060`
- Database: `employees`
- User: `dbuser`
- Password: `password`
- JDBC URL: `jdbc:mysql://localhost:33060/exercises`
