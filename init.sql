create database if not exists javaChat;
use javaChat;

CREATE TABLE USER(
    username varchar(50) primary key,
    password varchar(255) not null,
    created_at datetime default current_timestamp
);

CREATE TABLE USER_CHATS(
	user1 varchar(50) not null,
    user2 varchar(50) not null,
    chat_log TEXT,
    last_updated datetime default current_timestamp on update current_timestamp,
    primary key(user1,user2),
    foreign key (user1) references USER(username),
    foreign key(user2) references USER(username)
);