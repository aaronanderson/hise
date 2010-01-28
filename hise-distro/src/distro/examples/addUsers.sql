insert into ORG_ENTITY (name, type, userpassword) values ('user1', 'USER', 'pass1');
insert into ORG_ENTITY (name, type, userpassword) values ('group1', 'GROUP', null);
insert into USER_GROUPS (USERGROUPS_NAME, ORGENTITY_NAME) values ('user1', 'group1');
