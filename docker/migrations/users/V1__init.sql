CREATE TYPE RoleTypes AS ENUM (
    'ROLE_USER',
    'ROLE_ADMIN'
);

CREATE TABLE ROLES (
                       id BIGINT PRIMARY KEY,
                       name RoleTypes NOT NULL
);

CREATE TABLE USERS (
                       id BIGINT PRIMARY KEY,
                       username VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(100) NOT NULL
);

CREATE TABLE USER_ROLES (
                            user_id BIGINT,
                            role_id BIGINT,
                            PRIMARY KEY (user_id, role_id),
                            CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES USERS(id) ON DELETE CASCADE,
                            CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES ROLES(id) ON DELETE CASCADE
);
