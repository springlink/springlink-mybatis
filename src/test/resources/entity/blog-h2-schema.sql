--
--    Copyright 2009-2016 the original author or authors.
--
--    Licensed under the Apache License, Version 2.0 (the "License");
--    you may not use this file except in compliance with the License.
--    You may obtain a copy of the License at
--
--       http://www.apache.org/licenses/LICENSE-2.0
--
--    Unless required by applicable law or agreed to in writing, software
--    distributed under the License is distributed on an "AS IS" BASIS,
--    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--    See the License for the specific language governing permissions and
--    limitations under the License.
--

DROP TABLE IF EXISTS author;
CREATE TABLE author (
id                INT NOT NULL AUTO_INCREMENT,
username          VARCHAR(255) NOT NULL,
password          VARCHAR(255) NOT NULL,
email             VARCHAR(255) NOT NULL,
bio               CLOB,
favourite_section VARCHAR(25),
PRIMARY KEY (id)
) AUTO_INCREMENT=10000;

DROP TABLE IF EXISTS blog;
CREATE TABLE blog (
id          INT NOT NULL AUTO_INCREMENT,
author_id   INT NOT NULL,
title       VARCHAR(255),
PRIMARY KEY (id)
);

DROP TABLE IF EXISTS post;
CREATE TABLE post (
id          INT NOT NULL AUTO_INCREMENT,
blog_id     INT,
author_id   INT NOT NULL,
created_on  TIMESTAMP NOT NULL,
section     VARCHAR(25) NOT NULL,
subject     VARCHAR(255) NOT NULL,
body        CLOB NOT NULL,
draft       INT NOT NULL,
star        INT NOT NULL,
PRIMARY KEY (id),
FOREIGN KEY (blog_id) REFERENCES blog(id)
);

DROP TABLE IF EXISTS tag;
CREATE TABLE tag (
id          INT NOT NULL AUTO_INCREMENT,
name        VARCHAR(255) NOT NULL,
PRIMARY KEY (id)
);

DROP TABLE IF EXISTS post_tag;
CREATE TABLE post_tag (
post_id     INT NOT NULL,
tag_id      INT NOT NULL,
PRIMARY KEY (post_id, tag_id)
);

DROP TABLE IF EXISTS comment;
CREATE TABLE comment (
id          INT NOT NULL AUTO_INCREMENT,
post_id     INT NOT NULL,
name        LONGTEXT NOT NULL,
comment     LONGTEXT NOT NULL,
PRIMARY KEY (id)
);

CREATE TABLE node (
id  INT NOT NULL,
parent_id INT,
PRIMARY KEY(id)
);

CREATE TABLE generated_keys_table (
id1  INT NOT NULL AUTO_INCREMENT,
id2  INT NOT NULL AUTO_INCREMENT,
PRIMARY KEY(id1, id2)
);
