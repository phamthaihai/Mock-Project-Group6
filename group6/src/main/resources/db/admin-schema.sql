IF COL_LENGTH('dbo.Courses', 'RejectReason') IS NULL
    ALTER TABLE dbo.Courses ADD RejectReason NVARCHAR(500) NULL;

^^^ END_STATEMENT ^^^

IF COL_LENGTH('dbo.users', 'locked') IS NULL
    ALTER TABLE dbo.users ADD locked BIT NOT NULL
        CONSTRAINT DF_users_locked DEFAULT 0;

^^^ END_STATEMENT ^^^

IF OBJECT_ID('dbo.Quizzes', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Quizzes (
        QuizId INT IDENTITY(1,1) PRIMARY KEY,
        SectionId INT NOT NULL UNIQUE,
        Title NVARCHAR(150) NOT NULL,
        PassScore INT NOT NULL DEFAULT 70 CHECK (PassScore BETWEEN 1 AND 100),
        MaxAttempts INT NOT NULL DEFAULT 0,
        CONSTRAINT FK_Quizzes_Sections FOREIGN KEY (SectionId)
            REFERENCES dbo.Sections(SectionId) ON DELETE CASCADE
    );
END;

^^^ END_STATEMENT ^^^

IF OBJECT_ID('dbo.Questions', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Questions (
        QuestionId INT IDENTITY(1,1) PRIMARY KEY,
        QuizId INT NOT NULL,
        QuestionText NVARCHAR(500) NOT NULL,
        Position INT NOT NULL,
        CONSTRAINT FK_Questions_Quizzes FOREIGN KEY (QuizId)
            REFERENCES dbo.Quizzes(QuizId) ON DELETE CASCADE
    );
END;

^^^ END_STATEMENT ^^^

IF OBJECT_ID('dbo.Options', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Options (
        OptionId INT IDENTITY(1,1) PRIMARY KEY,
        QuestionId INT NOT NULL,
        OptionText NVARCHAR(300) NOT NULL,
        IsCorrect BIT NOT NULL DEFAULT 0,
        CONSTRAINT FK_Options_Questions FOREIGN KEY (QuestionId)
            REFERENCES dbo.Questions(QuestionId) ON DELETE CASCADE
    );
END;

^^^ END_STATEMENT ^^^

IF OBJECT_ID('dbo.QuizAttempts', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.QuizAttempts (
        AttemptId INT IDENTITY(1,1) PRIMARY KEY,
        QuizId INT NOT NULL,
        UserId INT NOT NULL,
        Score INT NOT NULL,
        Passed BIT NOT NULL,
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT FK_QuizAttempts_Quizzes FOREIGN KEY (QuizId)
            REFERENCES dbo.Quizzes(QuizId) ON DELETE CASCADE,
        CONSTRAINT FK_QuizAttempts_Users FOREIGN KEY (UserId)
            REFERENCES dbo.users(id)
    );
END;

^^^ END_STATEMENT ^^^

IF OBJECT_ID('dbo.Reviews', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Reviews (
        ReviewId INT IDENTITY(1,1) PRIMARY KEY,
        CourseId INT NOT NULL,
        UserId INT NOT NULL,
        Rating INT NOT NULL CHECK (Rating BETWEEN 1 AND 5),
        Comment NVARCHAR(1000) NULL,
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT FK_Reviews_Courses FOREIGN KEY (CourseId)
            REFERENCES dbo.Courses(CourseId) ON DELETE CASCADE,
        CONSTRAINT FK_Reviews_Users FOREIGN KEY (UserId)
            REFERENCES dbo.users(id),
        CONSTRAINT UQ_Reviews_Course_User UNIQUE (CourseId, UserId)
    );
END;

^^^ END_STATEMENT ^^^

IF OBJECT_ID('dbo.ActivityLogs', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.ActivityLogs (
        LogId BIGINT IDENTITY(1,1) PRIMARY KEY,
        LogType VARCHAR(12) NOT NULL CHECK (LogType IN ('ACCOUNT','PAYMENT','ENROLLMENT','COMPLETION')),
        Action VARCHAR(20) NOT NULL,
        UserId INT NULL,
        Email NVARCHAR(150) NULL,
        CourseId INT NULL,
        CourseTitle NVARCHAR(150) NULL,
        Amount DECIMAL(12,2) NULL,
        PaymentMethod NVARCHAR(30) NULL,
        Detail NVARCHAR(300) NULL,
        ActorName NVARCHAR(100) NULL,
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT FK_ActivityLogs_Users FOREIGN KEY (UserId)
            REFERENCES dbo.users(id) ON DELETE SET NULL,
        CONSTRAINT FK_ActivityLogs_Courses FOREIGN KEY (CourseId)
            REFERENCES dbo.Courses(CourseId) ON DELETE SET NULL
    );
END;

^^^ END_STATEMENT ^^^

IF OBJECT_ID('dbo.SupportTickets', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.SupportTickets (
        TicketId INT IDENTITY(1,1) PRIMARY KEY,
        UserId INT NULL,
        Name NVARCHAR(100) NOT NULL,
        Email NVARCHAR(150) NOT NULL,
        Category VARCHAR(10) NOT NULL CHECK (Category IN ('PAYMENT','COURSE','ACCOUNT','OTHER')),
        CourseId INT NULL,
        Subject NVARCHAR(100) NOT NULL,
        Message NVARCHAR(1000) NOT NULL,
        Status VARCHAR(10) NOT NULL DEFAULT 'OPEN' CHECK (Status IN ('OPEN','RESOLVED')),
        Reply NVARCHAR(1000) NULL,
        RepliedAt DATETIME2 NULL,
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT FK_SupportTickets_Users FOREIGN KEY (UserId)
            REFERENCES dbo.users(id) ON DELETE SET NULL,
        CONSTRAINT FK_SupportTickets_Courses FOREIGN KEY (CourseId)
            REFERENCES dbo.Courses(CourseId) ON DELETE SET NULL
    );
END;

^^^ END_STATEMENT ^^^
