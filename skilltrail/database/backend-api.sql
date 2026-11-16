-- Run once on the SkillTrail database, AFTER schema.sql, seed-courses.sql and email-verification.sql.
-- Adds what the REST API needs beyond the original tables:
--   Users.Avatar        profile picture (image link or small data URL)
--   Users.GoogleLinked  NULL = never used Google sign-in, 1 = linked, 0 = unbound by the user
--   PasswordResets      6-digit "forgot password" codes (stored as a BCrypt hash, 10 minutes, 5 tries)
ALTER TABLE Users ADD Avatar NVARCHAR(MAX) NULL;
ALTER TABLE Users ADD GoogleLinked BIT NULL;
GO
CREATE TABLE PasswordResets(
  Email NVARCHAR(150) NOT NULL PRIMARY KEY,
  CodeHash VARCHAR(100) NOT NULL,
  ExpiresAt DATETIME2 NOT NULL,
  Tries INT NOT NULL DEFAULT 0,
  SentAt DATETIME2 NOT NULL);
GO
