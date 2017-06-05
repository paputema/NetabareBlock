CREATE TABLE `netabareaccount` (
  `userid` bigint(20) NOT NULL,
  PRIMARY KEY (`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `useraccount` (
  `userid` bigint(20) NOT NULL,
  `accesstoken` text,
  `accesstokensecret` text,
  PRIMARY KEY (`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
