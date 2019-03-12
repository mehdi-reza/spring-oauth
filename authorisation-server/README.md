1. Steps to create keystore for jwt tokens

`keytool -genkeypair -alias <auth-server> -keyalg RSA -keypass changethis -keystore <oauth-server-jwt.jks> -storepass changethis`

1. Create tables

`create schema projectdb default character set utf8mb4;`

CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(45) NOT NULL,
  `password` varchar(70) DEFAULT NULL COMMENT 'BCrypt encoded password',
  `principal_name` varchar(45) DEFAULT NULL COMMENT 'any unique identifier like UUID',
  `account_type` varchar(20) DEFAULT NULL COMMENT 'EMAIL, PHONE, SOCIAL_ID',
  `social_platform` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email_UNIQUE` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='';


1. Insert test users

-- will be used in password grant

INSERT INTO USERS (EMAIL, PASSWORD, PRINCIPAL_NAME, ACCOUNT_TYPE)
VALUES ('mehdi.reza@gmail.com', '{bcrypt}$2a$10$6CT/yrA3cGPnlt43tvUJteTTrgSVoPgKRPmjMlXdzclZMpbTGcWC2', 'uuid', 'EMAIL');

-- will be used in authorization code flow

INSERT INTO USERS (EMAIL, PRINCIPAL_NAME, ACCOUNT_TYPE, SOCIAL_PLATFORM)
VALUES ('mehdi@venturedive.com', 'uuid', 'SOCIAL_ID', 'GOOGLE');

-----

##### Password grant flow

`curl --user mobile-app:public http://localhost:8080/oauth/token -dgrant_type=password -dusername=mehdi.reza@gmail.com -dpassword=password -dscope=any`

##### Authorisation code flow

* Visit the following URL to generate the authorisation code

https://accounts.google.com/o/oauth2/v2/auth?scope=https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile
&access_type=offline&include_granted_scopes=true&state=state_parameter_passthrough_value&redirect_uri=http%3A%2F%2Flocalhost&response_type=code&client_id=70732567463-4k841le5j9gf6u6ufvldle1niaf24fcr.apps.googleusercontent.com

`curl http://localhost:8080/oauth/assert/google/code?code=<code>`