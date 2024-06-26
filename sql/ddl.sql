drop
extension if exists pgcrypto;
CREATE
EXTENSION pgcrypto;
drop
extension if exists citext cascade;
CREATE
EXTENSION citext;


drop table if exists Users cascade;
create table if not exists users
(
    user_id   serial
    primary key,
    email     citext      not null
    unique,
    pass_hash varchar(64) not null
    );

drop table if exists Maps cascade;
create table Maps
(
    map_id      serial PRIMARY KEY                 not NULL,
    admin_id    integer references Users (user_id) not NULL,
    pass_hash   varchar(64),
    title       varchar(255)                       not NULL,
    description varchar(255),
    created_at  timestamp                          not null default current_timestamp,
    is_removed  bool                               not NULL default false
);


drop table if exists Nodes cascade;
create table Nodes
(
    node_id        uuid PRIMARY KEY                 not NULL default gen_random_uuid(),
    map_id         integer references Maps (map_id) not NULL,
    label          varchar(255)                     not NULL,
    description    text,
    parent_node_id uuid references Nodes (node_id),
    is_removed     bool                             not NULL default false
);

drop table if exists Tests cascade;
create table Tests
(
    test_id uuid PRIMARY KEY                not null,
    node_id uuid references Nodes (node_id) not null
);

drop type if exists QuestionType cascade;
create type QuestionType as ENUM ('SINGLE_CHOICE', 'MULTIPLE_CHOICE');

drop table if exists Questions cascade;
create table Questions
(
    question_id uuid PRIMARY KEY               not null,
    test_id     uuid REFERENCES Test (test_id) NOT NULL
);

-- -- Таблица для хранения основной информации о Question
drop table if exists QuestionSnapshot cascade;
-- CREATE TABLE QuestionSnapshot
-- (
--     question_id uuid REFERENCES Questions (question_id) primary key NOT NULL,
--     text        text                                                NOT NULL,
--     type        QuestionType                                        NOT NULL,
--     is_deleted  bool                                                NOT NULL DEFAULT false
-- );

drop type if exists QuestionStateType cascade;
create type QuestionStateType as ENUM ('CREATE', 'UPDATE', 'REMOVE');

-- Таблица для хранения событий изменения Question
drop table if exists QuestionStates cascade;
CREATE TABLE QuestionStates
(
    state_id    uuid PRIMARY KEY                        NOT NULL DEFAULT gen_random_uuid(),
    question_id uuid REFERENCES Questions (question_id) NOT NULL,
    state_type  QuestionStateType                       NOT NULL,
    state_data  jsonb                                   NOT NULL,
    created_at  timestamp                               NOT NULL DEFAULT current_timestamp
);

CREATE OR REPLACE VIEW QuestionActualState AS
SELECT
    qe.state_id,
    qe.question_id,
    qe.state_type,
    qe.state_data,
    qe.created_at
FROM
    questionstates qe
        INNER JOIN (
        SELECT
            question_id,
            MAX(created_at) AS latest_event_time
        FROM
            questionstates
        GROUP BY
            question_id
    ) latest_qe ON qe.question_id = latest_qe.question_id AND qe.created_at = latest_qe.latest_event_time;

drop table if exists Answers cascade;
create table Answers
(
    answer_id   uuid PRIMARY KEY                        not null,
    question_id uuid REFERENCES Questions (question_id) NOT NULL
);

-- -- Таблица для хранения основной информации о Question
drop table if exists AnswersSnapshot cascade;
-- CREATE TABLE AnswersSnapshot
-- (
--     answer_id  uuid REFERENCES Answers (answer_id) primary key NOT NULL,
--     text       text                                            NOT NULL,
--     is_correct bool                                            NOT NULL,
--     is_deleted bool                                            NOT NULL DEFAULT false
-- );

drop type if exists AnswerStateType cascade;
create type AnswerStateType as ENUM ('CREATE', 'UPDATE', 'REMOVE');

-- Таблица для хранения событий изменения Question
drop table if exists AnswerStates cascade;
CREATE TABLE AnswerStates
(
    state_id   uuid PRIMARY KEY                    NOT NULL DEFAULT gen_random_uuid(),
    answer_id  uuid REFERENCES Answers (answer_id) NOT NULL,
    state_type AnswerStateType                     NOT NULL,
    state_data jsonb                               NOT NULL,
    created_at timestamp                           NOT NULL DEFAULT current_timestamp
);

drop view if exists AnswerActualState;
CREATE OR REPLACE VIEW AnswerActualState AS
SELECT
    ae.state_id,
    ae.answer_id,
    ae.state_type,
    ae.state_data,
    ae.created_at
FROM
    AnswerStates ae
        INNER JOIN (
        SELECT
            answer_id,
            MAX(created_at) AS latest_event_time
        FROM
            AnswerStates
        GROUP BY
            answer_id
    ) latest_ae ON ae.answer_id = latest_ae.answer_id AND ae.created_at = latest_ae.latest_event_time;


drop table if exists Sessions cascade;
create table Sessions
(
    session_id uuid                               not NULL primary key default gen_random_uuid(),
    user_id    integer references Users (user_id) not NULL,
    device_id  varchar(255)                       not NULL,
    created_at timestamp                          not null             default current_timestamp,
    is_active  bool                               not NULL             default true
);

drop table if exists NodeProgress cascade;
create table NodeProgress
(
    user_id   integer references Users (user_id) not NULL,
    node_id   uuid references Nodes (node_id)    not NULL,
    is_marked bool                               not null,
    constraint unique_marked_node unique (user_id, node_id)
);

drop table if exists MapFetchTime cascade;
create table MapFetchTime
(
    fetch_id   uuid primary key                   not null default gen_random_uuid(),
    user_id    integer references Users (user_id) not NULL,
    map_id     integer references Maps (map_id)   not NULL,
    fetched_at timestamp                          not null default current_timestamp
);

drop table if exists AnswerProgress cascade;
create table AnswerProgress
(
    user_id   integer references Users (user_id)      not NULL,
    answer_id uuid references Answers (answer_id)     not NULL,
    fetch_id  uuid references MapFetchTime (fetch_id) not null,
    constraint unique_user_select_answer unique (user_id, answer_id, fetch_id)
);

drop table if exists SelectedMaps cascade;
create table SelectedMaps
(
    user_id    integer references Users (user_id) not NULL,
    map_id     integer references Maps (map_id)   not NULL,
    is_removed bool                               not NULL default false,
    constraint unique_maps_selecting unique (user_id, map_id)
);