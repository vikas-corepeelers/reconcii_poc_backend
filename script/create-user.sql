CREATE USER 'reconcii-application-client'@'%' IDENTIFIED BY 'reconcii8a780c6d';
GRANT SELECT, INSERT, UPDATE, DELETE ON reconcii.* TO 'reconcii-application-client'@'%';