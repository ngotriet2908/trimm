DROP TABLE IF EXISTS general_user CASCADE;
DROP TABLE IF EXISTS premium_user CASCADE;
DROP TABLE IF EXISTS surface CASCADE;
DROP TABLE IF EXISTS shoes CASCADE;
DROP TABLE IF EXISTS term CASCADE;
DROP TABLE IF EXISTS run CASCADE;
DROP TABLE IF EXISTS step CASCADE;

CREATE TABLE general_user (
    username VARCHAR(30),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255),
    password text, -- when we know the actual length of salted hash, better replace with CHAR(fixed_limit)
    salt text,
    PRIMARY KEY (username)
);

CREATE TABLE premium_user (
    username VARCHAR(30),
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    PRIMARY KEY (username, start_date),
    FOREIGN KEY (username) REFERENCES general_user(username)
);

CREATE TABLE surface (
    id SERIAL,
    description VARCHAR(255),
    type VARCHAR(30),
    PRIMARY KEY (id)
);

CREATE TABLE shoes (
    id SERIAL,
    brand VARCHAR(30),
    model VARCHAR(30),
    stack_height_heel INTEGER,
    stack_height_forefoot INTEGER,
    drop_value INTEGER,
    weight_value INTEGER,
    PRIMARY KEY (id)
);

CREATE TABLE term (
    name VARCHAR(30),
    meaning VARCHAR(255),
    PRIMARY KEY (name)
);


CREATE TABLE run (
    id SERIAL,
    username VARCHAR(30),
    date TIMESTAMP,
    name VARCHAR(30),
    bodypackfile VARCHAR(30),
    distance INTEGER, -- in meters
    duration INTEGER, -- in seconds
    steps INTEGER, -- calculate total number of steps of the run
    shoes_id INTEGER,
    surface_id INTEGER,
    description VARCHAR(255),
    remarks VARCHAR(255),
    stravalink VARCHAR(255),
    layout text,
    PRIMARY KEY (id),
    FOREIGN KEY (shoes_id) REFERENCES shoes(id),
    FOREIGN KEY (surface_id) REFERENCES surface(id)
);

CREATE TABLE step (
    run_id SERIAL,
    step_no INTEGER,
    time TIMESTAMP,
    surface_id INTEGER,

    ic_right TIMESTAMP,
    to_right TIMESTAMP,
    axtibacc_right FLOAT,
    tibimpact_right NUMERIC(30,20),
    axsacacc_right NUMERIC(30,20),
    sacimpact_right NUMERIC(30,20),
    brakingforce_right NUMERIC(30,20),
    pushoffpower_right NUMERIC(30,20),
    tibintrot_right NUMERIC(30,20),
    vll_right NUMERIC(30,20),

    ic_left TIMESTAMP,
    to_left TIMESTAMP,
    axtibacc_left NUMERIC(30,20),
    tibimpact_left NUMERIC(30,20),
    axsacacc_left NUMERIC(30,20),
    sacimpact_left NUMERIC(30,20),
    brakingforce_left NUMERIC(30,20),
    pushoffpower_left NUMERIC(30,20),
    tibintrot_left NUMERIC(30,20),
    vll_left NUMERIC(30,20),

    PRIMARY KEY (run_id, step_no),
    FOREIGN KEY (surface_id) REFERENCES surface(id)
);