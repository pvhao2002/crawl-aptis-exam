create database if not exists aptis;
use aptis;
create table de_thi
(
    id   int primary key auto_increment,
    name varchar(255),
    des  text,
    stt  int
);
