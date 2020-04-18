create table database_changelog
(
    database_changelog_id integer primary key,
    script_name           text,
    date_time_executed    text
);
