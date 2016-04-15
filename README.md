# CS 1555 Project

This is the repository for our CS 1555 Database Management Systems project.

## Setting up the Database

To create the database tables, run the *create-tables.sql* script from sqlplus. To add test data, run *add-test-data.sql*.

If you want to re-generate test data, run `python generate-example-data-sql.py`.

## Compiling and Running the Driver

To compile the driver, run `javac Driver.java`. To run it, run `java Driver`.

## Important files

- *create-tables.sql:* sets up the database tables necessary for FaceSpace
- *add-test-data.sql:* adds random test data to the database tables
- *Driver.java:* runs a driver for interacting with the database through the methods laid out in the project specification
- *generate-example-data-sql.py:* generates random SQL INSERT statements to populate the database tables with test data

## Collaborators

- **Benjamin Lind** (bdl22)
- **Autumn Good** (alg161)
- **Fadi Alchoufete** (fba4)

## Concurrency

This program assumes that only one person will access the database at a time. There are not protections in place to ensure concurrent multi-user access works correctly.

## Purpose

"The primary goal of this project is to implement a single Java application program that will back a new social networking system called FaceSpace." It will include "the basic information found in a social networking system such as user profiles, friends, groups, messages, etc."
