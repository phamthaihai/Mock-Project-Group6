/* =====================================================================
   LearnHub - SQL Server schema (DROP + RECREATE)
   WARNING: this deletes ALL existing data in the tables below.
   Column names are camelCase on purpose: application.properties uses
   PhysicalNamingStrategyStandardImpl, so JPA maps field names 1:1.
   ===================================================================== */
IF DB_ID('LearnHub') IS NULL CREATE DATABASE LearnHub;
GO
USE LearnHub;
GO

/* ---------- 1. DROP (children first, so foreign keys never block) ---------- */
DROP TABLE IF EXISTS QuizAttemptAnswers;
DROP TABLE IF EXISTS QuizAttempts;
DROP TABLE IF EXISTS QuizOptions;
DROP TABLE IF EXISTS QuizQuestions;
DROP TABLE IF EXISTS Quizzes;
DROP TABLE IF EXISTS LessonProgress;
DROP TABLE IF EXISTS Lessons;
DROP TABLE IF EXISTS Sections;
DROP TABLE IF EXISTS Reviews;
DROP TABLE IF EXISTS Enrollments;
DROP TABLE IF EXISTS Courses;
DROP TABLE IF EXISTS Users;
/* old tables from earlier versions of the project, if they exist */
DROP TABLE IF EXISTS Progress;
DROP TABLE IF EXISTS Events;
DROP TABLE IF EXISTS Registrations;
GO

/* ---------- 2. USERS ---------- */
CREATE TABLE Users (
    userId      INT IDENTITY(1,1) CONSTRAINT PK_Users PRIMARY KEY,
    fullName    NVARCHAR(100) NOT NULL,
    email       NVARCHAR(150) NOT NULL CONSTRAINT UQ_Users_email UNIQUE,
    password    NVARCHAR(100) NOT NULL,            -- store a BCrypt hash (60 chars), never plain text
    role        VARCHAR(10)   NOT NULL CONSTRAINT DF_Users_role DEFAULT 'USER'
                CONSTRAINT CK_Users_role CHECK (role IN ('USER','ADMIN')),
    createdAt   DATETIME2(0)  NOT NULL CONSTRAINT DF_Users_createdAt DEFAULT SYSDATETIME()
);

/* ---------- 3. COURSES ----------
   details = JSON marketing info only: tagline, learn[], requirements[], skills[], instructorBio.
   The curriculum itself now lives in Sections / Lessons / Quizzes. */
CREATE TABLE Courses (
    courseId       INT IDENTITY(1,1) CONSTRAINT PK_Courses PRIMARY KEY,
    title          NVARCHAR(150)  NOT NULL,
    category       NVARCHAR(30)   NOT NULL,
    level          NVARCHAR(20)   NOT NULL CONSTRAINT CK_Courses_level CHECK (level IN ('Beginner','Intermediate','Advanced')),
    instructor     NVARCHAR(100)  NOT NULL,
    durationHours  INT            NOT NULL CONSTRAINT DF_Courses_hours DEFAULT 0,
    language       NVARCHAR(30)   NOT NULL CONSTRAINT DF_Courses_lang DEFAULT 'English',
    price          DECIMAL(12,0)  NOT NULL CONSTRAINT DF_Courses_price DEFAULT 0 CONSTRAINT CK_Courses_price CHECK (price >= 0),
    rating         DECIMAL(2,1)   NOT NULL CONSTRAINT DF_Courses_rating DEFAULT 0 CONSTRAINT CK_Courses_rating CHECK (rating BETWEEN 0 AND 5),
    imageUrl       NVARCHAR(500)  NULL,
    description    NVARCHAR(MAX)  NULL,
    details        NVARCHAR(MAX)  NULL CONSTRAINT CK_Courses_details CHECK (details IS NULL OR ISJSON(details) = 1),
    createdAt      DATETIME2(0)   NOT NULL CONSTRAINT DF_Courses_createdAt DEFAULT SYSDATETIME()
);
CREATE INDEX IX_Courses_category ON Courses(category);

/* ---------- 4. SECTIONS (a "module" of a course) ---------- */
CREATE TABLE Sections (
    sectionId   INT IDENTITY(1,1) CONSTRAINT PK_Sections PRIMARY KEY,
    courseId    INT           NOT NULL CONSTRAINT FK_Sections_Courses REFERENCES Courses(courseId) ON DELETE CASCADE,
    title       NVARCHAR(150) NOT NULL,
    summary     NVARCHAR(500) NULL,
    sortOrder   INT           NOT NULL,
    CONSTRAINT UQ_Sections_order UNIQUE (courseId, sortOrder)
);

/* ---------- 5. LESSONS ----------
   content = JSON body of the lesson page:
   { "objectives":[..], "blocks":[ {"type":"text","body":".."}, {"type":"code","lang":"java","body":".."},
     {"type":"tip","body":".."}, {"type":"image","url":".."} ], "task":"..", "resources":[{"name":"..","type":"PDF","url":".."}] } */
CREATE TABLE Lessons (
    lessonId    INT IDENTITY(1,1) CONSTRAINT PK_Lessons PRIMARY KEY,
    sectionId   INT           NOT NULL CONSTRAINT FK_Lessons_Sections REFERENCES Sections(sectionId) ON DELETE CASCADE,
    title       NVARCHAR(150) NOT NULL,
    type        VARCHAR(15)   NOT NULL CONSTRAINT DF_Lessons_type DEFAULT 'Reading'
                CONSTRAINT CK_Lessons_type CHECK (type IN ('Video','Reading','Assignment')),
    minutes     INT           NOT NULL CONSTRAINT DF_Lessons_minutes DEFAULT 10,
    videoUrl    NVARCHAR(500) NULL,
    content     NVARCHAR(MAX) NULL CONSTRAINT CK_Lessons_content CHECK (content IS NULL OR ISJSON(content) = 1),
    sortOrder   INT           NOT NULL,
    CONSTRAINT UQ_Lessons_order UNIQUE (sectionId, sortOrder)
);

/* ---------- 6. ENROLLMENTS ---------- */
CREATE TABLE Enrollments (
    enrollmentId   INT IDENTITY(1,1) CONSTRAINT PK_Enrollments PRIMARY KEY,
    userId         INT           NOT NULL CONSTRAINT FK_Enrollments_Users   REFERENCES Users(userId)     ON DELETE CASCADE,
    courseId       INT           NOT NULL CONSTRAINT FK_Enrollments_Courses REFERENCES Courses(courseId) ON DELETE CASCADE,
    pricePaid      DECIMAL(12,0) NOT NULL CONSTRAINT DF_Enroll_price DEFAULT 0,
    paymentMethod  VARCHAR(10)   NULL CONSTRAINT CK_Enroll_pay CHECK (paymentMethod IS NULL OR paymentMethod IN ('CARD','EWALLET','BANK','DEMO','ADMIN')),
    enrolledAt     DATETIME2(0)  NOT NULL CONSTRAINT DF_Enroll_at DEFAULT SYSDATETIME(),
    completedAt    DATETIME2(0)  NULL,
    CONSTRAINT UQ_Enrollments_user_course UNIQUE (userId, courseId)
);
CREATE INDEX IX_Enrollments_courseId ON Enrollments(courseId);

/* ---------- 7. LESSON PROGRESS (one row = lesson marked complete) ---------- */
CREATE TABLE LessonProgress (
    userId       INT          NOT NULL CONSTRAINT FK_LP_Users   REFERENCES Users(userId) ON DELETE CASCADE,
    lessonId     INT          NOT NULL CONSTRAINT FK_LP_Lessons REFERENCES Lessons(lessonId) ON DELETE CASCADE,
    completedAt  DATETIME2(0) NOT NULL CONSTRAINT DF_LP_at DEFAULT SYSDATETIME(),
    CONSTRAINT PK_LessonProgress PRIMARY KEY (userId, lessonId)
);

/* ---------- 8. QUIZZES ----------
   A quiz belongs to a section (sectionId NOT NULL) - taken after the section's lessons, like Coursera.
   1-2 quizzes per section: use sortOrder 1 and 2. */
CREATE TABLE Quizzes (
    quizId          INT IDENTITY(1,1) CONSTRAINT PK_Quizzes PRIMARY KEY,
    sectionId       INT           NOT NULL CONSTRAINT FK_Quizzes_Sections REFERENCES Sections(sectionId) ON DELETE CASCADE,
    title           NVARCHAR(150) NOT NULL,
    description     NVARCHAR(500) NULL,
    passPercent     INT           NOT NULL CONSTRAINT DF_Quiz_pass DEFAULT 70 CONSTRAINT CK_Quiz_pass CHECK (passPercent BETWEEN 1 AND 100),
    timeLimitMin    INT           NULL,                       -- NULL = untimed
    maxAttempts     INT           NULL,                       -- NULL = unlimited
    shuffleQuestions BIT          NOT NULL CONSTRAINT DF_Quiz_shuffle DEFAULT 1,
    sortOrder       INT           NOT NULL CONSTRAINT DF_Quiz_order DEFAULT 1,
    CONSTRAINT UQ_Quizzes_order UNIQUE (sectionId, sortOrder)
);

CREATE TABLE QuizQuestions (
    questionId    INT IDENTITY(1,1) CONSTRAINT PK_QuizQuestions PRIMARY KEY,
    quizId        INT           NOT NULL CONSTRAINT FK_QQ_Quizzes REFERENCES Quizzes(quizId) ON DELETE CASCADE,
    questionText  NVARCHAR(500) NOT NULL,
    explanation   NVARCHAR(500) NULL,                         -- shown after submitting
    allowMultiple BIT           NOT NULL CONSTRAINT DF_QQ_multi DEFAULT 0,   -- 0 = single answer, 1 = tick all that apply
    sortOrder     INT           NOT NULL
);

CREATE TABLE QuizOptions (
    optionId    INT IDENTITY(1,1) CONSTRAINT PK_QuizOptions PRIMARY KEY,
    questionId  INT           NOT NULL CONSTRAINT FK_QO_Questions REFERENCES QuizQuestions(questionId) ON DELETE CASCADE,
    optionText  NVARCHAR(300) NOT NULL,
    isCorrect   BIT           NOT NULL CONSTRAINT DF_QO_correct DEFAULT 0,
    sortOrder   INT           NOT NULL
);
/* SECURITY: the API must NEVER send isCorrect to the browser before the attempt is submitted. */

/* ---------- 9. QUIZ ATTEMPTS ---------- */
CREATE TABLE QuizAttempts (
    attemptId     INT IDENTITY(1,1) CONSTRAINT PK_QuizAttempts PRIMARY KEY,
    quizId        INT          NOT NULL CONSTRAINT FK_QA_Quizzes REFERENCES Quizzes(quizId) ON DELETE CASCADE,
    userId        INT          NOT NULL CONSTRAINT FK_QA_Users   REFERENCES Users(userId)   ON DELETE CASCADE,
    scorePercent  INT          NOT NULL,
    correctCount  INT          NOT NULL,
    totalCount    INT          NOT NULL,
    passed        BIT          NOT NULL,
    startedAt     DATETIME2(0) NOT NULL CONSTRAINT DF_QA_start DEFAULT SYSDATETIME(),
    submittedAt   DATETIME2(0) NOT NULL CONSTRAINT DF_QA_sub   DEFAULT SYSDATETIME()
);
CREATE INDEX IX_QA_user_quiz ON QuizAttempts(userId, quizId);

CREATE TABLE QuizAttemptAnswers (
    attemptId    INT NOT NULL CONSTRAINT FK_QAA_Attempts  REFERENCES QuizAttempts(attemptId) ON DELETE CASCADE,
    questionId   INT NOT NULL,   -- no FK on purpose: SQL Server forbids a 2nd cascade path (Courses -> Quizzes -> Questions and -> Attempts)
    optionId     INT NOT NULL,   -- the chosen QuizOptions.optionId (the API validates it belongs to the question)
    CONSTRAINT PK_QuizAttemptAnswers PRIMARY KEY (attemptId, questionId, optionId)
);

/* ---------- 10. REVIEWS (the course page shows student reviews) ---------- */
CREATE TABLE Reviews (
    reviewId    INT IDENTITY(1,1) CONSTRAINT PK_Reviews PRIMARY KEY,
    courseId    INT           NOT NULL CONSTRAINT FK_Reviews_Courses REFERENCES Courses(courseId) ON DELETE CASCADE,
    userId      INT           NOT NULL CONSTRAINT FK_Reviews_Users   REFERENCES Users(userId)     ON DELETE CASCADE,
    rating      TINYINT       NOT NULL CONSTRAINT CK_Reviews_rating CHECK (rating BETWEEN 1 AND 5),
    comment     NVARCHAR(1000) NULL,
    createdAt   DATETIME2(0)  NOT NULL CONSTRAINT DF_Reviews_at DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Reviews_user_course UNIQUE (userId, courseId)
);
GO

/* ---------- 11. SEED: default admin (password: admin123 - REPLACE with a BCrypt hash in the real app) ---------- */
INSERT INTO Users (fullName, email, password, role) VALUES (N'Admin', 'admin@learnhub.com', 'admin123', 'ADMIN');
GO

/* ---------- 12. SEED: sample course with 2 sections, lessons and quizzes (copy the pattern for the rest) ---------- */
INSERT INTO Courses (title, category, level, instructor, durationHours, language, price, rating, imageUrl, description, details)
VALUES (N'Intro to Web Development', N'Programming', N'Beginner', N'Le Thu Ha', 12, N'English', 0, 4.6, NULL,
        N'Learn HTML, CSS and JavaScript from zero and publish your first responsive website.',
        N'{"tagline":"Your first website in a weekend. Free.","learn":["Structure pages with semantic HTML","Style layouts with CSS and Bootstrap","Add interactivity with JavaScript","Publish a site to the web"],"requirements":["No experience needed","Any computer and a browser"],"skills":["HTML","CSS","Bootstrap","JavaScript"],"instructorBio":"Front-end developer and teacher who has helped over 5,000 beginners write their first line of code."}');
DECLARE @c INT = SCOPE_IDENTITY();

INSERT INTO Sections (courseId, title, summary, sortOrder) VALUES (@c, N'HTML basics', N'Build the structure of a web page.', 1);
DECLARE @s1 INT = SCOPE_IDENTITY();
INSERT INTO Sections (courseId, title, summary, sortOrder) VALUES (@c, N'CSS and Bootstrap', N'Make the page look good on any screen.', 2);
DECLARE @s2 INT = SCOPE_IDENTITY();

INSERT INTO Lessons (sectionId, title, type, minutes, content, sortOrder) VALUES
 (@s1, N'Page structure', 'Video', 10,
  N'{"objectives":["Name the main HTML tags","Build a valid page skeleton"],"blocks":[{"type":"text","body":"Every page starts with a doctype, then html, head and body."},{"type":"code","lang":"html","body":"<!DOCTYPE html>\n<html>\n  <head><title>Hello</title></head>\n  <body><h1>Hello web</h1></body>\n</html>"},{"type":"tip","body":"Use semantic tags such as header, main and footer."}],"task":"Create index.html with a heading and a paragraph.","resources":[{"name":"Slides","type":"PDF"}]}', 1),
 (@s1, N'Forms and tables', 'Reading', 14,
  N'{"objectives":["Create a form","Mark up tabular data"],"blocks":[{"type":"text","body":"Forms collect input. Tables show rows and columns of data."}],"task":"Build a contact form with 3 fields.","resources":[]}', 2),
 (@s2, N'Selectors and layout', 'Video', 16,
  N'{"objectives":["Target elements with selectors","Use flexbox"],"blocks":[{"type":"text","body":"Selectors choose elements; flexbox lines them up."}],"task":"Centre a card on the page.","resources":[]}', 1);

INSERT INTO Quizzes (sectionId, title, description, passPercent, timeLimitMin, sortOrder)
VALUES (@s1, N'HTML basics quiz', N'Check what you learned in this section.', 70, 10, 1);
DECLARE @q1 INT = SCOPE_IDENTITY();

INSERT INTO QuizQuestions (quizId, questionText, explanation, allowMultiple, sortOrder)
VALUES (@q1, N'Which tag holds the visible content of a page?', N'The body element contains everything the visitor sees.', 0, 1);
DECLARE @qq1 INT = SCOPE_IDENTITY();
INSERT INTO QuizOptions (questionId, optionText, isCorrect, sortOrder) VALUES
 (@qq1, N'<head>', 0, 1), (@qq1, N'<body>', 1, 2), (@qq1, N'<title>', 0, 3), (@qq1, N'<meta>', 0, 4);

INSERT INTO QuizQuestions (quizId, questionText, explanation, allowMultiple, sortOrder)
VALUES (@q1, N'Which of these are semantic tags? (select all that apply)', N'header and footer describe meaning; div and span do not.', 1, 2);
DECLARE @qq2 INT = SCOPE_IDENTITY();
INSERT INTO QuizOptions (questionId, optionText, isCorrect, sortOrder) VALUES
 (@qq2, N'<header>', 1, 1), (@qq2, N'<div>', 0, 2), (@qq2, N'<footer>', 1, 3), (@qq2, N'<span>', 0, 4);
GO

/* quick check */
SELECT 'Users' t, COUNT(*) n FROM Users UNION ALL SELECT 'Courses', COUNT(*) FROM Courses UNION ALL SELECT 'Sections', COUNT(*) FROM Sections
UNION ALL SELECT 'Lessons', COUNT(*) FROM Lessons UNION ALL SELECT 'Quizzes', COUNT(*) FROM Quizzes UNION ALL SELECT 'QuizQuestions', COUNT(*) FROM QuizQuestions;
