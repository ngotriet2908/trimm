
-- -----------------USER----------------------
CREATE OR REPLACE FUNCTION getUserDetails(_username text)
    RETURNS SETOF general_user AS $$
BEGIN
    RETURN QUERY
        SELECT * FROM general_user AS u
        WHERE u.username = $1;

END;
$$ LANGUAGE plpgsql;
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION getUserImage(_username varchar(30))
    RETURNS TABLE(r_picture BYTEA) AS $$
BEGIN
    RETURN QUERY
        SELECT picture FROM user_picture AS u
        WHERE u.username = $1;

END;
$$ LANGUAGE plpgsql;
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION getUsersPassword(_username varchar(30))
    RETURNS TABLE(r_password text) AS $$
BEGIN
    RETURN QUERY
        SELECT u.password FROM general_user AS u
        WHERE u.username = $1;

END;
$$ LANGUAGE plpgsql;
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION getSalt(_username varchar(30))
    RETURNS TABLE(r_salt text) AS $$
BEGIN
    RETURN QUERY
        SELECT u.salt FROM general_user AS u
        WHERE u.username = $1;

END;
$$ LANGUAGE plpgsql;
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION isPremiumUser(_username varchar(30))
    RETURNS TABLE(r_username varchar(30)) AS $$
BEGIN
    RETURN QUERY
        SELECT p.username FROM premium_user AS p
        WHERE p.username = $1;

END;
$$ LANGUAGE plpgsql;
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION isUsersEmail(_username varchar(30))
    RETURNS TABLE(r_username varchar(30)) AS $$
BEGIN
    RETURN QUERY
        SELECT p.username FROM general_user AS p
        WHERE p.username = $1
          AND p.email = $1;
END;
$$ LANGUAGE plpgsql;
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION isActivated(_username varchar(30))
    RETURNS TABLE(r_is_activated boolean) AS $$
BEGIN
    RETURN QUERY
        SELECT p.is_activated FROM general_user AS p
        WHERE p.username = $1;
END;
$$ LANGUAGE plpgsql;
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION getShoes(_username varchar(30))
    RETURNS TABLE(r_shoesIndicator text, r_id integer) AS $$
BEGIN
    RETURN QUERY
        SELECT s.brand || ' ' || s.model, r.id
        FROM shoes AS s, run AS r, general_user u
        WHERE s.id = r.shoes_id
          AND u.username = $1;
END;
$$ LANGUAGE plpgsql;
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION getShoesWithRun(_runId integer)
    RETURNS TABLE(r_shoesIndicator text) AS $$
BEGIN
    RETURN QUERY
        SELECT s.brand || ' ' || s.model as shoes_name
        FROM run AS r, shoes AS s
        WHERE r.id =  $1
          AND r.shoes_id = s.id;
END;
$$ LANGUAGE plpgsql;
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION getFavoriteLayoutName(_username varchar(30))
    RETURNS TABLE(r_name varchar(30), r_lid integer) AS $$
BEGIN
    RETURN QUERY
        SELECT l.name, l.lid
        FROM favorite_layout AS l
        WHERE l.username = $1;
END;
$$ LANGUAGE plpgsql;
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION getFavoriteLayout(_layout_id integer, _username varchar(30))
    RETURNS TABLE(r_layout text) AS $$
BEGIN
    RETURN QUERY
        SELECT f.layout
        FROM favorite_layout as f
        WHERE f.lid = $1
          AND f.username = $2;
END;
$$ LANGUAGE plpgsql;
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION hasImage(_username varchar(30))
    RETURNS TABLE(r_username varchar(30)) AS $$
BEGIN
    RETURN QUERY
        SELECT u.username
        FROM user_picture as u
        WHERE username = $1;
END;
$$ LANGUAGE plpgsql;
-- -----------------RUN----------------------
CREATE OR REPLACE FUNCTION getUserTotalStats(_username varchar(30))
    RETURNS TABLE(r_total_runs BIGINT
        , r_total_time BIGINT
        , r_total_distance BIGINT
        , r_total_steps BIGINT) AS $$
BEGIN
    RETURN QUERY
        SELECT COUNT(*) AS total_runs, SUM(r.duration) AS total_time,
               SUM(r.distance) AS total_distance, SUM(r.steps) AS total_steps
        FROM run AS r
        WHERE r.username = $1;
END;
$$ LANGUAGE plpgsql;
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION getUserRunsList(_username varchar(30))
    RETURNS SETOF run AS $$
BEGIN
    RETURN QUERY
        SELECT *
        FROM run AS r
        WHERE r.username = $1
        ORDER BY r.date DESC;
END;
$$ LANGUAGE plpgsql;
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION getUsername(_runID integer)
    RETURNS TABLE(r_username varchar(30)) AS $$
BEGIN
    RETURN QUERY
        SELECT r.username
        FROM run AS r
        WHERE r.id = $1
        ORDER BY r.date DESC;
END;
$$ LANGUAGE plpgsql;
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION getLayout(_runID integer, current integer)
    RETURNS TABLE(r_layout text) AS $$
BEGIN
    RETURN QUERY
        SELECT l.layout
        FROM layout AS l
        WHERE l.run_id = $1
          AND l.lid = $2;
END;
$$ LANGUAGE plpgsql;
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION getDefaultLayout(_runID integer)
    RETURNS TABLE(r_default_layout text) AS $$
BEGIN
    RETURN QUERY
        SELECT r.default_layout
        FROM run AS r
        WHERE r.id = $1;
END;
$$ LANGUAGE plpgsql;
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION getLayoutName(_runID integer)
    RETURNS TABLE(r_name VARCHAR(30), r_lid integer) AS $$
BEGIN
    RETURN QUERY
        SELECT l.name, l.lid
        FROM layout AS l
        WHERE l.run_id = $1;
END;
$$ LANGUAGE plpgsql;
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION getCurrentLayout(_runID integer)
    RETURNS TABLE(r_current_layout integer) AS $$
BEGIN
    RETURN QUERY
        SELECT r.current_layout
        FROM run AS r
        WHERE r.id = $1;
END;
$$ LANGUAGE plpgsql;
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION getStepLength(_runID integer)
    RETURNS TABLE(r_distance integer, r_steps integer) AS $$
BEGIN
    RETURN QUERY
        SELECT r.distance, r.steps
        FROM run AS r
        WHERE r.id = $1;
END;
$$ LANGUAGE plpgsql;
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION getRun(_runID integer)
    RETURNS SETOF run AS $$
BEGIN
    RETURN QUERY
        SELECT *
        FROM run AS r
        WHERE r.id = $1;
END;
$$ LANGUAGE plpgsql;
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION isUsersRun(_runID integer, _username varchar(30))
    RETURNS TABLE(r_id integer) AS $$
BEGIN
    RETURN QUERY
        SELECT r.id
        FROM run AS r
        WHERE r.id = $1
          AND r.username = $2;
END;
$$ LANGUAGE plpgsql;
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION getNote(_runID integer)
    RETURNS TABLE(r_description VARCHAR(255)) AS $$
BEGIN
    RETURN QUERY
        SELECT description
        FROM run
        WHERE id = $1;
END;
$$ LANGUAGE plpgsql;