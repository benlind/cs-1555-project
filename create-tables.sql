------------------------------------------------------------------------------
-- create-tables.sql creates the database tables necessary for the FaceSpace
-- system. It creates users, friendships, groups, and messages.
--
-- Note: this script will drop all relevant tables before recreating them.
--
-- Contributors:
--     - Benjamin Lind (bdl22)
--     - Autumn Good (alg161)
--     - Fadi Alchoufete (fba4)
------------------------------------------------------------------------------

    
-- Remove previous tables
DROP TABLE Friendships;
DROP TABLE Users;

    
------------------------------------------------------------------------------
-- This section of code was written by: Benjamin Lind (bdl22)

CREATE TABLE Users (
    user_id NUMBER(10),
    fname VARCHAR(64) NOT NULL,  -- max 64 chars
    lname VARCHAR(64) NOT NULL,  -- max 64 chars
    email VARCHAR(254) NOT NULL, -- max 254 chars
    dob DATE,
    last_login TIMESTAMP,
    CONSTRAINT Users_PK PRIMARY KEY (user_id)
);

CREATE TABLE Friendships (
    friendship_id NUMBER(10),
    friend_initiator NUMBER(10) NOT NULL,
    friend_receiver NUMBER(10) NOT NULL,
    established NUMBER(1) NOT NULL,
    date_established TIMESTAMP,
    CONSTRAINT Friendships_PK PRIMARY KEY (friendship_id),
    CONSTRAINT Friendships_FK_Initiator FOREIGN KEY (friend_initiator)
        REFERENCES Users (user_id),
    CONSTRAINT Friendships_FK_Receiver FOREIGN KEY (friend_receiver)
        REFERENCES Users (user_id)
);
