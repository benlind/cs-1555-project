------------------------------------------------------------------------------
-- Class: CS 1555 Database Management Systems
-- Instructor: Prof. Mohamed Sharaf
-- Contributors:
--   - Benjamin Lind (bdl22)
--   - Autumn Good (alg161)
--   - Fadi Alchoufete (fba4)
--
-- create-tables.sql creates the database tables necessary for the FaceSpace
-- system. It creates users, friendships, groups, and messages.
--
-- Note: this script will drop all relevant tables before recreating them.
------------------------------------------------------------------------------


-- Remove previous tables
DROP TABLE Friendship;
DROP TABLE FS_User;

    
------------------------------------------------------------------------------
-- This section of code was written by: Benjamin Lind (bdl22)

CREATE TABLE FS_User (
    user_id NUMBER(10),
    name VARCHAR(128) NOT NULL,  -- max 128 chars
    email VARCHAR(254) NOT NULL, -- max 254 chars
    dob DATE,
    last_login TIMESTAMP,
    CONSTRAINT User_PK PRIMARY KEY (user_id),
    CONSTRAINT Email_Unique UNIQUE (email)
);

-- Set up auto-incrementing for user ids
DROP SEQUENCE user_seq;
CREATE SEQUENCE user_seq;
CREATE OR REPLACE TRIGGER user_auto_increment
BEFORE INSERT ON FS_User
FOR EACH ROW
BEGIN
    SELECT user_seq.nextval INTO :new.user_id FROM dual;
END;
/

CREATE TABLE Friendship (
    friendship_id NUMBER(10),
    friend_initiator NUMBER(10) NOT NULL,
    friend_receiver NUMBER(10) NOT NULL,
    established NUMBER(1) NOT NULL,
    date_established TIMESTAMP,
    CONSTRAINT Friendship_PK PRIMARY KEY (friendship_id),
    CONSTRAINT Friendship_FK_Initiator FOREIGN KEY (friend_initiator)
        REFERENCES FS_User (user_id),
    CONSTRAINT Friendship_FK_Receiver FOREIGN KEY (friend_receiver)
        REFERENCES FS_User (user_id)
);

-- Set up auto-incrementing for friendship ids
DROP SEQUENCE friendship_seq;
CREATE SEQUENCE friendship_seq;
CREATE OR REPLACE TRIGGER friendship_auto_increment
BEFORE INSERT ON Friendship
FOR EACH ROW
BEGIN
    SELECT friendship_seq.nextval INTO :new.friendship_id FROM dual;
END;
/
