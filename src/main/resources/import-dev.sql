insert into users (id, username, password) values (nextval('hibernate_sequence'), 'jpkeam', 'password');
insert into todos(id, title, user_id) select nextval('hibernate_sequence'), 'Brush Teeth', id from users where username='jpkeam';

insert into users (id, username, password) values (nextval('hibernate_sequence'), 'janedoe', 'password');
insert into todos(id, title, user_id) select nextval('hibernate_sequence'), 'Walk Dog', id from users where username='janedoe';
