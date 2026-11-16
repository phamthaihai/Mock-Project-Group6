-- Run against an existing SkillTrail database before deploying the authentication changes.
IF COL_LENGTH('dbo.Users', 'IsVerified') IS NULL
BEGIN
  ALTER TABLE dbo.Users
    ADD IsVerified BIT NOT NULL
      CONSTRAINT DF_Users_IsVerified DEFAULT 1;
END
GO

ALTER TABLE dbo.Users ALTER COLUMN PasswordHash NVARCHAR(255) NULL;
GO

IF OBJECT_ID('dbo.EmailVerificationTokens', 'U') IS NULL
BEGIN
  CREATE TABLE dbo.EmailVerificationTokens(
    TokenHash CHAR(64) PRIMARY KEY,
    UserId INT NOT NULL REFERENCES dbo.Users(UserId) ON DELETE CASCADE,
    ExpiresAt DATETIME2 NOT NULL);
  CREATE INDEX IX_EmailVerificationTokens_ExpiresAt
    ON dbo.EmailVerificationTokens(ExpiresAt);
END
GO

IF OBJECT_ID('dbo.AuthSessions', 'U') IS NULL
BEGIN
  CREATE TABLE dbo.AuthSessions(
    TokenHash CHAR(64) PRIMARY KEY,
    UserId INT NOT NULL REFERENCES dbo.Users(UserId) ON DELETE CASCADE,
    ExpiresAt DATETIME2 NOT NULL);
  CREATE INDEX IX_AuthSessions_ExpiresAt
    ON dbo.AuthSessions(ExpiresAt);
END
GO
