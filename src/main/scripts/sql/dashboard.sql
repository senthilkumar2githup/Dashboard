create database IF NOT EXISTS dashboard;

use dashboard;

DROP TABLE IF EXISTS `dash_application`;

create table dash_application (
dash_app_id VARCHAR(50) NOT NULL PRIMARY KEY,  
dash_app_name VARCHAR(50) NOT NULL
) ENGINE=InnoDB;

LOCK TABLES `dash_application` WRITE;
INSERT INTO dash_application(dash_app_id,dash_app_name) VALUES ('A001','Demo'),('A002','Telematics'),('A003','Insurance'),('circuit','Circuit');
UNLOCK TABLES;

DROP TABLE IF EXISTS `user_details`;

create table user_details (
user_id  INT(20) NOT NULL auto_increment,  
user_name  VARCHAR(40) ,
password VARCHAR(40) ,
active_flag CHAR(1),
PRIMARY KEY(user_id)
) ENGINE=InnoDB;

LOCK TABLES `user_details` WRITE;
INSERT INTO user_details(USER_NAME,PASSWORD,ACTIVE_FLAG) VALUES ('anonymous','1234','N'),('admin','1234','N'),('zkoss','1234','N');
UNLOCK TABLES;

DROP TABLE IF EXISTS `dashboard_details`;

create table dashboard_details (
dashboard_id  INT(30) NOT NULL auto_increment,  
dashboard_name  VARCHAR(40) ,
user_id INT(20) ,
column_count TINYINT(7),
sequence INT(40) ,
source_id VARCHAR(50),
application_id VARCHAR(50),
updated_date DATE,
PRIMARY KEY(dashboard_id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `WIDGET_DETAILS`;

create table WIDGET_DETAILS (
widget_id  INT(40) NOT NULL auto_increment,  
dashboard_id  INT(30) ,
widget_name VARCHAR(30) ,
widget_state CHAR(1) ,
chart_type TINYINT(7) ,
column_identifier TINYINT(7) ,
widget_sequence TINYINT(7) ,
chart_data TEXT ,
PRIMARY KEY(widget_id),
FOREIGN KEY(dashboard_id) REFERENCES dashboard_details(dashboard_id)
) ENGINE=InnoDB;
