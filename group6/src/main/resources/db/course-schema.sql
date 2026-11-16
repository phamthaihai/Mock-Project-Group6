
-- SkillTrail course database initialization
-- Existing users and role tables remain untouched.

IF OBJECT_ID('dbo.Categories', 'U') IS NULL
    EXEC(N'CREATE TABLE dbo.Categories (
        CategoryId INT IDENTITY(1,1) PRIMARY KEY,
        Name NVARCHAR(50) NOT NULL UNIQUE
    )');

^^^ END_STATEMENT ^^^

IF OBJECT_ID('dbo.Courses', 'U') IS NULL
    EXEC(N'CREATE TABLE dbo.Courses (
        CourseId INT IDENTITY(1,1) PRIMARY KEY,
        Title NVARCHAR(150) NOT NULL,
        Description NVARCHAR(MAX),
        CategoryId INT NOT NULL,
        CourseLevel VARCHAR(15) NOT NULL DEFAULT ''Beginner'',
        Price DECIMAL(12,2) NOT NULL DEFAULT 0,
        ImageUrl NVARCHAR(500),
        Status VARCHAR(15) NOT NULL DEFAULT ''DRAFT'',
        InstructorId INT NOT NULL,
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT FK_Courses_Categories
            FOREIGN KEY (CategoryId)
            REFERENCES dbo.Categories(CategoryId),
        CONSTRAINT FK_Courses_Users
            FOREIGN KEY (InstructorId)
            REFERENCES dbo.users(id),
        CONSTRAINT CK_Courses_Price CHECK (Price >= 0),
        CONSTRAINT CK_Courses_Level
            CHECK (CourseLevel IN (''Beginner'', ''Intermediate'', ''Advanced'')),
        CONSTRAINT CK_Courses_Status
            CHECK (Status IN (''DRAFT'', ''PENDING'', ''PUBLISHED'', ''REJECTED''))
    )');

^^^ END_STATEMENT ^^^

IF OBJECT_ID('dbo.Sections', 'U') IS NULL
    EXEC(N'CREATE TABLE dbo.Sections (
        SectionId INT IDENTITY(1,1) PRIMARY KEY,
        CourseId INT NOT NULL,
        Title NVARCHAR(150) NOT NULL,
        Position INT NOT NULL,
        CONSTRAINT FK_Sections_Courses
            FOREIGN KEY (CourseId)
            REFERENCES dbo.Courses(CourseId)
            ON DELETE CASCADE
    )');

^^^ END_STATEMENT ^^^

IF OBJECT_ID('dbo.Lessons', 'U') IS NULL
    EXEC(N'CREATE TABLE dbo.Lessons (
        LessonId INT IDENTITY(1,1) PRIMARY KEY,
        SectionId INT NOT NULL,
        Title NVARCHAR(150) NOT NULL,
        ContentType VARCHAR(10) NOT NULL,
        ContentUrl NVARCHAR(500),
        TextContent NVARCHAR(MAX),
        Minutes INT NOT NULL DEFAULT 0,
        IsPreview BIT NOT NULL DEFAULT 0,
        Position INT NOT NULL,
        CONSTRAINT FK_Lessons_Sections
            FOREIGN KEY (SectionId)
            REFERENCES dbo.Sections(SectionId)
            ON DELETE CASCADE,
        CONSTRAINT CK_Lessons_ContentType
            CHECK (ContentType IN (''VIDEO'', ''TEXT'', ''FILE''))
    )');

^^^ END_STATEMENT ^^^

IF OBJECT_ID('dbo.Enrollments', 'U') IS NULL
    EXEC(N'CREATE TABLE dbo.Enrollments (
        EnrollmentId INT IDENTITY(1,1) PRIMARY KEY,
        CourseId INT NOT NULL,
        UserId INT NOT NULL,
        PricePaid DECIMAL(12,2) NOT NULL DEFAULT 0,
        PaymentMethod NVARCHAR(30),
        EnrolledAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT FK_Enrollments_Courses
            FOREIGN KEY (CourseId)
            REFERENCES dbo.Courses(CourseId)
            ON DELETE CASCADE,
        CONSTRAINT FK_Enrollments_Users
            FOREIGN KEY (UserId)
            REFERENCES dbo.users(id),
        CONSTRAINT UQ_Enrollments UNIQUE (CourseId, UserId)
    )');

^^^ END_STATEMENT ^^^

IF OBJECT_ID('dbo.LessonProgress', 'U') IS NULL
    EXEC(N'CREATE TABLE dbo.LessonProgress (
        UserId INT NOT NULL,
        LessonId INT NOT NULL,
        CompletedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT PK_LessonProgress PRIMARY KEY (UserId, LessonId),
        CONSTRAINT FK_LessonProgress_Users
            FOREIGN KEY (UserId)
            REFERENCES dbo.users(id),
        CONSTRAINT FK_LessonProgress_Lessons
            FOREIGN KEY (LessonId)
            REFERENCES dbo.Lessons(LessonId)
            ON DELETE CASCADE
    )');

^^^ END_STATEMENT ^^^

IF OBJECT_ID('dbo.CartItems', 'U') IS NULL
    EXEC(N'CREATE TABLE dbo.CartItems (
        UserId INT NOT NULL,
        CourseId INT NOT NULL,
        AddedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT PK_CartItems PRIMARY KEY (UserId, CourseId),
        CONSTRAINT FK_CartItems_Users
            FOREIGN KEY (UserId)
            REFERENCES dbo.users(id)
            ON DELETE CASCADE,
        CONSTRAINT FK_CartItems_Courses
            FOREIGN KEY (CourseId)
            REFERENCES dbo.Courses(CourseId)
            ON DELETE CASCADE
    )');

^^^ END_STATEMENT ^^^

-- Add nested modules without recreating existing sections or lessons.
IF COL_LENGTH('dbo.Sections','ParentSectionId') IS NULL
    ALTER TABLE dbo.Sections ADD ParentSectionId INT NULL;

^^^ END_STATEMENT ^^^

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name='FK_Sections_Parent')
    ALTER TABLE dbo.Sections ADD CONSTRAINT FK_Sections_Parent
        FOREIGN KEY (ParentSectionId) REFERENCES dbo.Sections(SectionId);

^^^ END_STATEMENT ^^^
