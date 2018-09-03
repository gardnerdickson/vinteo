-- Create table schemas for first time setup.

drop table if exists play_history;
create table play_history (
  play_history_id integer primary key,
  item_id         integer,
  date_time       text
);

drop table if exists item;
create table item (
  item_id integer primary key,
  path    text,
  name    text
);
