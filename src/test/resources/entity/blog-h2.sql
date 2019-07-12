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

INSERT INTO author (id,username, password, email, bio, favourite_section) VALUES (101,'jim','********','jim@ibatis.apache.org','','NEWS');
INSERT INTO author (id,username, password, email, bio, favourite_section) VALUES (102,'sally','********','sally@ibatis.apache.org',null,'VIDEOS');

INSERT INTO blog (id,author_id,title) VALUES (1,101,'Jim Business');
INSERT INTO blog (id,author_id,title) VALUES (2,102,'Bally Slog');

INSERT INTO post (id,blog_id,author_id,created_on,section,subject,body,draft,star) VALUES (1,1,101,'2007-12-05 00.00.00','NEWS','Title contains character %','I think if I never smelled another corn nut it would be too soon...',1,0);
INSERT INTO post (id,blog_id,author_id,created_on,section,subject,body,draft,star) VALUES (2,1,101,'2008-01-12 00.00.00','VIDEOS','Paul Hogan on Toy Dogs','That''s not a dog.  THAT''s a dog!',0,100);
INSERT INTO post (id,blog_id,author_id,created_on,section,subject,body,draft,star) VALUES (3,2,102,'2007-12-05 00.00.00','PODCASTS','Monster Trucks','I think monster trucks are great...',1,66);
INSERT INTO post (id,blog_id,author_id,created_on,section,subject,body,draft,star) VALUES (4,2,102,'2008-01-12 00.00.00','IMAGES','Tea Parties','A tea party is no place to hold a business meeting...',0,10);

-- BAD POST
INSERT INTO post (id,blog_id,author_id,created_on,section,subject,body,draft,star) VALUES (5,null,101,'2008-01-12 00.00.00','IMAGES','An orphaned post','this post is orphaned',0,7);

INSERT INTO tag (id,name) VALUES (1,'funny');
INSERT INTO tag (id,name) VALUES (2,'cool');
INSERT INTO tag (id,name) VALUES (3,'food');

INSERT INTO post_tag (post_id,tag_id) VALUES (1,1);
INSERT INTO post_tag (post_id,tag_id) VALUES (1,2);
INSERT INTO post_tag (post_id,tag_id) VALUES (1,3);
INSERT INTO post_tag (post_id,tag_id) VALUES (2,1);
INSERT INTO post_tag (post_id,tag_id) VALUES (4,3);

INSERT INTO comment (id,post_id,name,comment) VALUES (1,1,'troll','I disagree and think...');
INSERT INTO comment (id,post_id,name,comment) VALUES (2,1,'anonymous','I agree and think troll is an...');
-- INSERT INTO comment (id,post_id,name,comment) VALUES (4,2,'another','I don not agree and still think troll is an...');
INSERT INTO comment (id,post_id,name,comment) VALUES (3,3,'rider','I prefer motorcycles to monster trucks...');


--       1
--    2     3
--  4  5   6  7

INSERT INTO node (id, parent_id) VALUES (1,null);
INSERT INTO node (id, parent_id) VALUES (2,1); 
INSERT INTO node (id, parent_id) VALUES (3,1);
INSERT INTO node (id, parent_id) VALUES (4,2);
INSERT INTO node (id, parent_id) VALUES (5,2);
INSERT INTO node (id, parent_id) VALUES (6,3);
INSERT INTO node (id, parent_id) VALUES (7,3);

