alter table budget
    add column author_id int;

alter table budget
    add constraint fk_author_id_key foreign key(author_id) references author(id)