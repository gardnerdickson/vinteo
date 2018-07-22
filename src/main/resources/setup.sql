-- Create table schemas for first time setup.

drop table if exists file_extension;
create table file_extension (
  file_extension_id     integer primary key,
  file_extension        text
);

drop table if exists play_command;
create table play_command (
  play_command_id   integer primary key,
  name              text,
  executable_path   text,
  argument_string   text
);

drop table if exists directory;
create table directory (
  directory_id      integer primary key,
  path              text
);

drop table if exists play_history;
create table play_history (
  play_history_id   integer primary key,
  directory_id      integer,
  date_time         text
);
