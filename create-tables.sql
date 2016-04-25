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
DROP TABLE Group_Member;
DROP TABLE User_Group;
DROP TABLE Message;
DROP TABLE FS_User;



------------------------------------------------------------------------------
-- This section of code was written by: Benjamin Lind (bdl22)

PROMPT ----- CREATING FS_USER -----
    
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


PROMPT ----- CREATING FRIENDSHIP -----

CREATE TABLE Friendship (
    friendship_id NUMBER(10),
    friend_initiator NUMBER(10) NOT NULL,
    friend_receiver NUMBER(10) NOT NULL,
    established NUMBER(1) NOT NULL,
    date_established TIMESTAMP,
    CONSTRAINT Friendship_PK PRIMARY KEY (friendship_id),
    CONSTRAINT Friendship_FK_Initiator FOREIGN KEY (friend_initiator)
        REFERENCES FS_User (user_id)
        ON DELETE CASCADE,
    CONSTRAINT Friendship_FK_Receiver FOREIGN KEY (friend_receiver)
        REFERENCES FS_User (user_id)
        ON DELETE CASCADE,
    CONSTRAINT Friendship_Unique UNIQUE (friend_initiator, friend_receiver)
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

-- Ensure there are no duplicate friendships
-- or people friends with themselves
CREATE OR REPLACE TRIGGER friendship_checks
BEFORE INSERT ON Friendship
FOR EACH ROW
DECLARE
    already_exists NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO already_exists
    FROM (
        SELECT friendship_id
        FROM Friendship
        WHERE friend_initiator = :new.friend_receiver
            AND friend_receiver = :new.friend_initiator
    );

    IF already_exists > 0 THEN
        raise_application_error(-20001, 'A friendship between those ids already exists.');
    ELSIF :new.friend_initiator = :new.friend_receiver THEN
        raise_application_error(-20002, 'A user cannot be friends with themselves.');
    END IF;
END;
/


    
------------------------------------------------------------------------------
-- This section of the code was written by: Autumn Good (alg161)

PROMPT ----- CREATING GROUP -----

CREATE TABLE User_Group (
    group_id NUMBER(10),
    group_name VARCHAR(64) NOT NULL,
    group_description VARCHAR(160),
    group_enroll_limit NUMBER(6),
    CONSTRAINT group_PK PRIMARY KEY (group_id)
);

DROP SEQUENCE group_seq;
CREATE SEQUENCE group_seq;
CREATE OR REPLACE TRIGGER group_auto_increment
BEFORE INSERT ON User_Group
FOR EACH ROW
BEGIN
    SELECT group_seq.nextval INTO :new.group_id FROM dual;
END;
/


PROMPT ----- CREATING GROUP_MEMBER -----

CREATE TABLE Group_Member (
    group_id NUMBER(10),
    user_id NUMBER(10),
    CONSTRAINT group_members_pk PRIMARY KEY (group_id, user_id),
    CONSTRAINT group_id_fk FOREIGN KEY (group_id)
        REFERENCES User_Group (group_id),
    CONSTRAINT user_id_fk FOREIGN KEY (user_id)
        REFERENCES FS_User (user_id)
        ON DELETE CASCADE
);

-- Trigger to ensure that groups do not go over their enrollment limits
CREATE OR REPLACE TRIGGER check_enrollment
BEFORE INSERT OR UPDATE ON Group_Member
REFERENCING NEW AS newRow
FOR EACH ROW
DECLARE 
    g_cnt NUMBER;
    g_limit NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO g_cnt
    FROM (
        SELECT group_id
        FROM Group_Member
        WHERE Group_Member.group_id = :newRow.group_id
    );

    SELECT group_enroll_limit
    INTO g_limit
    FROM User_Group
    WHERE User_Group.group_id = :newRow.group_id;

    IF g_cnt = g_limit THEN
        raise_application_error(-20002, 'The enrollment limit has been reached for this group.');
    END IF;
END;
/


    
------------------------------------------------------------------------------
-- This section of the code was written by: Fadi Alchoufete (fba4)

PROMPT ----- CREATING MESSAGE -----

CREATE TABLE Message (
    message_id NUMBER(10),
    subject VARCHAR(254),
    body VARCHAR(100),
    recipient NUMBER(10),
    sender NUMBER(10),
    date_sent TIMESTAMP,
    CONSTRAINT Message_PK PRIMARY KEY (message_id),
    CONSTRAINT Message_FK_recipient FOREIGN KEY (recipient)
        REFERENCES FS_User (user_id),
    CONSTRAINT Message_FK_sender FOREIGN KEY (sender)
        REFERENCES FS_User (user_id)
);

-- Set up auto-incrementing for message IDs
DROP SEQUENCE message_seq;
CREATE SEQUENCE message_seq;
CREATE OR REPLACE TRIGGER message_auto_increment
BEFORE INSERT ON Message
FOR EACH ROW
BEGIN
    SELECT message_seq.nextval INTO :new.message_id FROM dual;
END;
/

-- Create trigger to check that sender and recipient are not the same
-- and not null. NOTE: sender and recipient might end up null if a user
-- is deleted.
CREATE OR REPLACE TRIGGER check_message_send_rec
BEFORE INSERT ON Message
REFERENCING NEW AS newRow
FOR EACH ROW
BEGIN
    IF :newRow.sender = :newRow.recipient THEN
        raise_application_error(-20003, 'The sender and recipient for this message cannot be the same.');
    ELSIF :newRow.sender IS NULL THEN
        raise_application_error(-20004, 'The sender cannot be null.');
    ELSIF :newRow.recipient IS NULL THEN
        raise_application_error(-20005, 'The recipient cannot be null.');
    END IF;
END;
/



-- When a user is deleted, update/delete their messages
CREATE OR REPLACE TRIGGER delete_fs_user
BEFORE DELETE ON FS_User
FOR EACH ROW
BEGIN
    -- Set user to NULL in messages this user has sent/received
    UPDATE Message SET sender = NULL WHERE sender = :old.user_id;
    UPDATE Message SET recipient = NULL WHERE recipient = :old.user_id;

    -- Delete any messages that have neither sender nor recipient
    DELETE FROM Message WHERE sender IS NULL AND recipient IS NULL;
END;
/