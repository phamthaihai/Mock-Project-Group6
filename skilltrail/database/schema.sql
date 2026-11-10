-- SkillTrail (online course and student progress platform) - SQL Server (T-SQL)
-- Run this whole script in SSMS. It creates a fresh SkillTrail database (any other existing database is not touched).
USE master;
GO
IF DB_ID('SkillTrail') IS NOT NULL
BEGIN
  ALTER DATABASE SkillTrail SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
  DROP DATABASE SkillTrail;
END
GO
CREATE DATABASE SkillTrail;
GO
USE SkillTrail;
GO
CREATE TABLE Users(
  UserId INT IDENTITY(1,1) PRIMARY KEY,
  FullName NVARCHAR(100) NOT NULL,
  Email NVARCHAR(150) NOT NULL UNIQUE,
  PasswordHash NVARCHAR(255) NOT NULL,
  Role VARCHAR(10) NOT NULL DEFAULT 'STUDENT' CHECK (Role IN ('STUDENT','ADMIN')),
  IsAuthor BIT NOT NULL DEFAULT 0,  -- course author profile (the teacher shown on a course); not a role, and such rows do not log in (Locked = 1)
  Locked BIT NOT NULL DEFAULT 0,
  CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME());
CREATE TABLE Categories(
  CategoryId INT IDENTITY(1,1) PRIMARY KEY,
  Name NVARCHAR(50) NOT NULL UNIQUE);
CREATE TABLE Courses(
  CourseId INT IDENTITY(1,1) PRIMARY KEY,
  Title NVARCHAR(150) NOT NULL,
  Description NVARCHAR(MAX) NULL,
  CategoryId INT NOT NULL REFERENCES Categories(CategoryId),
  CourseLevel VARCHAR(15) NOT NULL DEFAULT 'Beginner' CHECK (CourseLevel IN ('Beginner','Intermediate','Advanced')),  -- JSON: level
  Price DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (Price >= 0),                                                          -- 0 = free
  ImageUrl NVARCHAR(500) NULL,                                                                                         -- thumbnail
  Status VARCHAR(10) NOT NULL DEFAULT 'DRAFT' CHECK (Status IN ('DRAFT','PENDING','PUBLISHED','REJECTED')),
  RejectReason NVARCHAR(500) NULL,
  InstructorId INT NOT NULL REFERENCES Users(UserId),
  Details NVARCHAR(MAX) NULL CHECK (Details IS NULL OR ISJSON(Details) = 1),  -- optional: {tagline, learn[], requirements[], skills[], instructorBio}
  CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME());
CREATE TABLE Sections(
  SectionId INT IDENTITY(1,1) PRIMARY KEY,
  CourseId INT NOT NULL REFERENCES Courses(CourseId) ON DELETE CASCADE,
  Title NVARCHAR(150) NOT NULL,
  Position INT NOT NULL);
CREATE TABLE Lessons(
  LessonId INT IDENTITY(1,1) PRIMARY KEY,
  SectionId INT NOT NULL REFERENCES Sections(SectionId) ON DELETE CASCADE,
  Title NVARCHAR(150) NOT NULL,
  ContentType VARCHAR(10) NOT NULL CHECK (ContentType IN ('VIDEO','TEXT','FILE')),   -- JSON: type
  ContentUrl NVARCHAR(500) NULL,                                                       -- video link or file link
  TextContent NVARCHAR(MAX) NULL,                                                      -- JSON: text
  Minutes INT NOT NULL DEFAULT 0,
  IsPreview BIT NOT NULL DEFAULT 0,                                                    -- JSON: preview
  Position INT NOT NULL);
CREATE TABLE Enrollments(
  EnrollmentId INT IDENTITY(1,1) PRIMARY KEY,
  CourseId INT NOT NULL REFERENCES Courses(CourseId) ON DELETE CASCADE,
  UserId INT NOT NULL REFERENCES Users(UserId),
  PricePaid DECIMAL(12,2) NOT NULL DEFAULT 0,
  PaymentMethod NVARCHAR(30) NULL,                                                     -- CARD / EWALLET / BANK / DEMO / ADMIN
  EnrolledAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
  CONSTRAINT UQ_Enrollment UNIQUE (CourseId, UserId));                                 -- duplicate enroll -> 409
CREATE TABLE LessonProgress(
  UserId INT NOT NULL REFERENCES Users(UserId),
  LessonId INT NOT NULL REFERENCES Lessons(LessonId) ON DELETE CASCADE,
  CompletedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
  PRIMARY KEY (UserId, LessonId));                                                     -- progress % = completed lessons / total lessons * 100
CREATE TABLE Quizzes(
  QuizId INT IDENTITY(1,1) PRIMARY KEY,
  SectionId INT NOT NULL UNIQUE REFERENCES Sections(SectionId) ON DELETE CASCADE,      -- one quiz per section
  Title NVARCHAR(150) NOT NULL,
  PassScore INT NOT NULL DEFAULT 70 CHECK (PassScore BETWEEN 1 AND 100),
  MaxAttempts INT NOT NULL DEFAULT 0);                                                 -- 0 = unlimited
CREATE TABLE Questions(
  QuestionId INT IDENTITY(1,1) PRIMARY KEY,
  QuizId INT NOT NULL REFERENCES Quizzes(QuizId) ON DELETE CASCADE,
  QuestionText NVARCHAR(500) NOT NULL,
  Position INT NOT NULL);
CREATE TABLE Options(
  OptionId INT IDENTITY(1,1) PRIMARY KEY,
  QuestionId INT NOT NULL REFERENCES Questions(QuestionId) ON DELETE CASCADE,
  OptionText NVARCHAR(300) NOT NULL,
  IsCorrect BIT NOT NULL DEFAULT 0);                                                   -- never send this to students before they submit
CREATE TABLE QuizAttempts(
  AttemptId INT IDENTITY(1,1) PRIMARY KEY,
  QuizId INT NOT NULL REFERENCES Quizzes(QuizId) ON DELETE CASCADE,
  UserId INT NOT NULL REFERENCES Users(UserId),
  Score INT NOT NULL,
  Passed BIT NOT NULL,
  CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME());
CREATE TABLE Reviews(
  ReviewId INT IDENTITY(1,1) PRIMARY KEY,
  CourseId INT NOT NULL REFERENCES Courses(CourseId) ON DELETE CASCADE,
  UserId INT NOT NULL REFERENCES Users(UserId),
  Rating INT NOT NULL CHECK (Rating BETWEEN 1 AND 5),
  Comment NVARCHAR(1000) NULL,
  CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
  CONSTRAINT UQ_Review UNIQUE (CourseId, UserId));                                     -- one review per user per course
CREATE TABLE Notifications(
  NotificationId INT IDENTITY(1,1) PRIMARY KEY,
  UserId INT NOT NULL REFERENCES Users(UserId),
  Message NVARCHAR(500) NOT NULL,
  Link NVARCHAR(200) NULL,
  IsRead BIT NOT NULL DEFAULT 0,
  CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME());
-- Activity log shown on the admin page (Type: ACCOUNT / PAYMENT / ENROLLMENT / COMPLETION). The server inserts one row per event.
CREATE TABLE ActivityLogs(
  LogId BIGINT IDENTITY(1,1) PRIMARY KEY,
  LogType VARCHAR(12) NOT NULL CHECK (LogType IN ('ACCOUNT','PAYMENT','ENROLLMENT','COMPLETION')),
  Action VARCHAR(20) NOT NULL,        -- REGISTERED, LOGIN, LOGIN_FAILED, LOGIN_BLOCKED, PASSWORD_CHANGED, ROLE_CHANGED, LOCKED, UNLOCKED, ENROLLED, UNENROLLED, PAID, COMPLETED
  UserId INT NULL REFERENCES Users(UserId) ON DELETE SET NULL,
  Email NVARCHAR(150) NULL,           -- kept so failed logins and deleted users are still readable
  CourseId INT NULL REFERENCES Courses(CourseId) ON DELETE SET NULL,
  CourseTitle NVARCHAR(150) NULL,     -- snapshot, survives course deletion
  Amount DECIMAL(12,2) NULL,          -- PAYMENT only
  PaymentMethod NVARCHAR(30) NULL,    -- PAYMENT only
  Detail NVARCHAR(300) NULL,
  ActorName NVARCHAR(100) NULL,       -- who did it (the user, or the admin for ROLE_CHANGED / LOCKED / UNLOCKED)
  CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME());
CREATE INDEX IX_ActivityLogs_Type_Time ON ActivityLogs(LogType, CreatedAt DESC);
-- Help & Support requests (guests can send one, so UserId may be NULL)
CREATE TABLE SupportTickets(
  TicketId INT IDENTITY(1,1) PRIMARY KEY,
  UserId INT NULL REFERENCES Users(UserId) ON DELETE SET NULL,
  Name NVARCHAR(100) NOT NULL,
  Email NVARCHAR(150) NOT NULL,
  Category VARCHAR(10) NOT NULL CHECK (Category IN ('PAYMENT','COURSE','ACCOUNT','OTHER')),
  CourseId INT NULL REFERENCES Courses(CourseId) ON DELETE SET NULL,
  Subject NVARCHAR(100) NOT NULL,
  Message NVARCHAR(1000) NOT NULL,
  Status VARCHAR(10) NOT NULL DEFAULT 'OPEN' CHECK (Status IN ('OPEN','RESOLVED')),
  Reply NVARCHAR(1000) NULL,
  RepliedAt DATETIME2 NULL,
  CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME());
-- Shopping cart: one row per (user, course). Emptied by POST /cart/checkout, which creates the Enrollments rows.
CREATE TABLE CartItems(
  UserId INT NOT NULL REFERENCES Users(UserId) ON DELETE CASCADE,
  CourseId INT NOT NULL REFERENCES Courses(CourseId) ON DELETE CASCADE,
  AddedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
  PRIMARY KEY (UserId, CourseId));                                                     -- adding twice -> 409
GO
INSERT INTO Categories (Name) VALUES ('Programming'), ('Data'), ('Design'), ('Business'), ('Languages'), ('Marketing');
-- Admin account: email admin@gmail.com, password 123456 (BCrypt hash)
INSERT INTO Users (FullName, Email, PasswordHash, Role)
VALUES ('Admin', 'admin@gmail.com', '$2a$10$1KDzvoupXdNR9mm7tLFxmOeVu41iMuQJhcK06PWl.1rRTW/rHQnty', 'ADMIN');
GO
