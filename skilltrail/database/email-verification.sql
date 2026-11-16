-- Run once on the SkillTrail database, after schema.sql.
-- Existing rows (admin, course authors) default to Verified = 1; new email sign-ups are saved with 0 by the app.
ALTER TABLE Users ADD Verified BIT NOT NULL CONSTRAINT DF_Users_Verified DEFAULT 1;
GO
CREATE TABLE EmailVerifications(
  VerificationId INT IDENTITY(1,1) PRIMARY KEY,
  Email NVARCHAR(150) NOT NULL UNIQUE,
  CodeHash VARCHAR(100) NOT NULL,     -- BCrypt hash of the 6-digit code, never the code itself
  ExpiresAt DATETIME2 NOT NULL,
  Tries INT NOT NULL DEFAULT 0,
  SentAt DATETIME2 NOT NULL);
GO
