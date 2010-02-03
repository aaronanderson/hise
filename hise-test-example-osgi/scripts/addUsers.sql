insert into ORG_ENTITY (name, type, userpassword) values ('user1', 'USER', 'user1pass');
insert into ORG_ENTITY (name, type, userpassword) values ('user2', 'USER', 'user2pass');
insert into ORG_ENTITY (name, type, userpassword) values ('user5', 'USER', 'user5pass');
insert into ORG_ENTITY (name, type, userpassword) values ('group1', 'GROUP', null);
insert into USER_GROUPS (USERGROUPS_NAME, ORGENTITY_NAME) values ('user1', 'group1');
