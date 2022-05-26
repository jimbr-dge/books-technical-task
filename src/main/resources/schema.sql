DROP TABLE IF EXISTS books;
CREATE TABLE books
(
   isbn varchar(10) not null,
   title varchar(255) not null,
   price integer not null,
   primary key(isbn)
);