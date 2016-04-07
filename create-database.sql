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
DROP TABLE Friendship;
DROP TABLE Users;

    
------------------------------------------------------------------------------
-- This section of code was written by: Benjamin Lind (bdl22)

CREATE TABLE Users (
    user_id NUMBER(10) PRIMARY KEY,
    fname VARCHAR(64),  -- max 64 chars
    lname VARCHAR(64),  -- max 64 chars
    email VARCHAR(254), -- max 254 chars
    dob DATE,
    last_login TIMESTAMP
);

CREATE TABLE Friendship (
    friendship_id NUMBER(10),
    friend_initiator NUMBER(10),
    friend_receiver NUMBER(10),
    established NUMBER(1),
    date_established TIMESTAMP,
    CONSTRAINT Friendship_PK PRIMARY KEY (friendship_id),
    CONSTRAINT Friendship_FK_Friend_Initiator FOREIGN KEY (friend_initiator)
        REFERENCES Users (user_id),
    CONSTRAINT Friendship_FK_Friend_Receiver FOREIGN KEY (friend_receiver)
        REFERENCES Users (user_id)
);
