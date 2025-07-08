CREATE TYPE CatColors AS ENUM (
    'BLACK',
    'WHITE',
    'GREY',
    'RED',
    'MULTICOLOURED'
);

CREATE TABLE CATS (
                      id BIGINT PRIMARY KEY,
                      name VARCHAR(255) NOT NULL,
                      birth_date DATE NOT NULL,
                      breed_name VARCHAR(255) NOT NULL,
                      color CatColors NOT NULL,
                      owner_id INT NOT NULL
);
