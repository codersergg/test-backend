create table author
(
    id            serial primary key,
    full_name     text   not null,
    date_creation bigint not null
);

alter table budget
    add column author_id integer;

alter table budget
    add constraint fk_author_id foreign key (author_id) references author (id);