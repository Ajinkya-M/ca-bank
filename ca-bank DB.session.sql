-- View all members
SELECT * FROM members;

-- Delete all members
DELETE FROM members;

-- Reset the auto-increment sequence to 1
ALTER SEQUENCE members_id_seq RESTART WITH 1;