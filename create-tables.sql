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
DROP TABLE Group_Members;
DROP TABLE Users;
DROP TABLE Groups;


    
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

CREATE TABLE Groups (
	group_id NUMBER(10),
	group_name VARCHAR(64) NOT NULL,
	group_description VARCHAR(160),
	group_enroll_limit NUMBER(6) ,
--	group_enrollment NUMBER (6),
	CONSTRAINT group_PK PRIMARY KEY (group_id),
--	CONSTRAINT within_enrollment_limit
--		CHECK (group_enrollment <= group_enroll_limit)
);

CREATE TABLE Group_Members (
	group_id NUMBER(10),
	user_id NUMBER(10),
	CONSTRAINT group_members_pk PRIMARY KEY (group_id, user_id),
	CONSTRAINT group_id_fk FOREIGN KEY (group_id)
		REFERENCES Groups(group_id),
	CONSTRAINT user_id_fk FOREIGN KEY (user_id)
		REFERENCES Users(user_id)
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


