create table testingdata (
  id int not null,
  username varchar(80),
  ip varchar(15),
  email varchar(100),
  is_locked tinyint not null,
  is_active tinyint not null default 3,
  last_login bigint default -123,
  ratio float,
  primary key(id)
);

insert into testingdata (id, username, ip, email, is_locked, is_active, last_login, ratio)
VALUES
 ( 1, 'Alex',  '111.111.111.111', NULL,               1, 0, 123456,  NULL),
 ( 2, 'Brett', '111.111.111.111', 'test@example.com', 0, 1, 123456,   3.0),
 ( 3, 'Cody',  '22.22.22.22',     'test@example.com', 0, 0, 888888,   2.21),
 ( 4, 'Dan',   '22.22.22.22',     NULL,               1, 1, NULL,    -4.04),
 ( 5, 'Emily', NULL,              NULL,               0, 1, 888888,  NULL),
 ( 6, 'Finn',  '111.111.111.111', 'finn@example.org', 0, 0, NULL,    NULL),
 ( 7, 'Gary',  '44.144.41.144',   'test@example.com', 0, 1, 123456,  32.59),
 ( 8, 'Hans',  NULL,              'other@test.tld',   1, 0, 77665544, 6.18),
 ( 9, 'Igor',  '22.22.22.22',     'other@test.tld',   0, 1, 725124,  -7.41),
 (10, 'Jake',  '44.144.41.144',   NULL,               0, 0, 123456,  NULL),
 (11, 'Keane', '22.22.22.22',     'test@example.com', 0, 1, 888888,  NULL),
 (12, 'Louis', NULL,              'other@test.tld',   0, 1, 732452,  147.532);
