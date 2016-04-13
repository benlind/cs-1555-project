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
        REFERENCES FS_User (user_id),
    CONSTRAINT Friendship_FK_Receiver FOREIGN KEY (friend_receiver)
        REFERENCES FS_User (user_id),
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
CREATE OR REPLACE TRIGGER friendship_duplicates
BEFORE INSERT ON Friendship
FOR EACH ROW
DECLARE
    v_cnt NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO v_cnt
    FROM (
        SELECT friendship_id
        FROM Friendship
        WHERE friend_initiator = :new.friend_receiver
            AND friend_receiver = :new.friend_initiator
    );

    IF v_cnt > 0 THEN
        raise_application_error(-20001, 'A friendship between those ids already exists.');
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
--  group_enrollment NUMBER (6),
--	CONSTRAINT within_enrollment_limit
--		CHECK (group_enrollment <= group_enroll_limit)
);

PROMPT ----- CREATING GROUP_MEMBER -----

CREATE TABLE Group_Member (
	group_id NUMBER(10),
	user_id NUMBER(10),
	CONSTRAINT group_members_pk PRIMARY KEY (group_id, user_id),
	CONSTRAINT group_id_fk FOREIGN KEY (group_id)
		REFERENCES User_Group (group_id),
	CONSTRAINT user_id_fk FOREIGN KEY (user_id)
		REFERENCES FS_User (user_id)
);

--Updates the enrollment value in the group tables, where a check is enforced
--Cannot use NEW for table level triggers
--CREATE OR REPLACE TRIGGER increment_enrollment
--	BEFORE INSERT ON Group_Members
--	REFERENCING NEW AS newRow
--	BEGIN
--		UPDATE Groups SET group_enrollment = group_enrollment + 1 
--		WHERE Groups.group_id = :newRow.group_id;
--	END;
--	/
	
--Tries to count the number of records per group_id and compare it to enrollment limit
-- Group functions not allowed, neither are subqueries
--CREATE OR REPLACE TRIGGER check_enrollment
--	AFTER INSERT ON Group_Members
--	REFERENCING NEW AS newRow
--	FOR EACH ROW
--		WHEN (count(*) > groups.group_enrollment
--					FROM Group_Members 
--					where group_member.group_id = Groups.group_id
--					group by group_members.group_id 	
--				)
--	BEGIN
--		ROLLBACK
--	END;
--	/


    
------------------------------------------------------------------------------
-- This section of the code was written by: Fadi Alchoufete (fba4)

PROMPT ----- CREATING MESSAGE -----

CREATE TABLE Message (
    message_id NUMBER(10),
    subject VARCHAR(254),
    body VARCHAR(100),
    recipient NUMBER(10) NOT NULL,
    sender NUMBER(10) NOT NULL,
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
