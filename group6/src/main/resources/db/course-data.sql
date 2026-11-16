
-- Default course categories
-- Safe to execute every time the application starts.

INSERT INTO dbo.Categories (Name)
SELECT v.Name
FROM (VALUES
    (N'Programming'),
    (N'Data'),
    (N'Design'),
    (N'Business'),
    (N'Languages'),
    (N'Marketing')
) AS v(Name)
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.Categories c
    WHERE c.Name = v.Name
);

DECLARE @InstructorId INT;

SELECT TOP (1) @InstructorId = id
FROM dbo.users
ORDER BY id;

IF @InstructorId IS NOT NULL
BEGIN
    DECLARE @Samples TABLE (
        Title NVARCHAR(200),
        Description NVARCHAR(MAX),
        CategoryName NVARCHAR(50),
        CourseLevel NVARCHAR(30),
        Price DECIMAL(12,2),
        ImageUrl NVARCHAR(500)
    );

    INSERT INTO @Samples
    (Title, Description, CategoryName, CourseLevel, Price, ImageUrl)
    VALUES
    (
        N'Java Full-Stack Bootcamp',
        N'Build complete web applications using Java, Spring Boot, HTML, CSS and SQL.',
        N'Programming', N'Intermediate', 1290000,
        N'https://picsum.photos/seed/Programming1/1200/700'
    ),
    (
        N'Intro to Web Development',
        N'Learn HTML, CSS and JavaScript to build your first website.',
        N'Programming', N'Beginner', 0,
        N'https://picsum.photos/seed/Programming2/1200/700'
    ),
    (
        N'Docker & DevOps Essentials',
        N'Learn containers, Docker images, deployment and DevOps fundamentals.',
        N'Programming', N'Intermediate', 790000,
        N'https://picsum.photos/seed/Programming3/1200/700'
    ),
    (
        N'Machine Learning Foundations',
        N'Understand machine learning algorithms and practical model training.',
        N'Data', N'Intermediate', 1490000,
        N'https://picsum.photos/seed/Data4/1200/700'
    ),
    (
        N'SQL & Data Analysis',
        N'Learn SQL queries, joins, grouping and database analysis.',
        N'Data', N'Beginner', 450000,
        N'https://picsum.photos/seed/Data5/1200/700'
    ),
    (
        N'UI/UX Design Essentials',
        N'Learn user interface design, wireframes and user experience principles.',
        N'Design', N'Beginner', 790000,
        N'https://picsum.photos/seed/Design6/1200/700'
    ),
    (
        N'Digital Marketing Masterclass',
        N'Explore digital marketing, social media campaigns and online advertising.',
        N'Marketing', N'Beginner', 650000,
        N'https://picsum.photos/seed/Marketing7/1200/700'
    ),
    (
        N'Business English for Professionals',
        N'Improve professional communication, meetings and business writing.',
        N'Languages', N'Intermediate', 890000,
        N'https://picsum.photos/seed/Languages8/1200/700'
    ),
    (
        N'Public Speaking & Presentation Skills',
        N'Learn to speak confidently and create effective presentations.',
        N'Business', N'Beginner', 390000,
        N'https://picsum.photos/seed/Business9/1200/700'
    ),
    (
        N'React for Modern Web Apps',
        N'Build interactive frontend applications using React components and hooks.',
        N'Programming', N'Intermediate', 990000,
        N'https://picsum.photos/seed/Programming10/1200/700'
    ),
    (
        N'Data Visualization with Power BI',
        N'Create dashboards, reports and visualizations using Power BI.',
        N'Data', N'Beginner', 520000,
        N'https://picsum.photos/seed/Data11/1200/700'
    ),
    (
        N'Graphic Design with Photoshop & Illustrator',
        N'Learn essential graphic design techniques using Adobe creative tools.',
        N'Design', N'Beginner', 750000,
        N'https://picsum.photos/seed/Design12/1200/700'
    ),
    (
        N'Project Management Fundamentals',
        N'Learn project planning, scheduling, teamwork and risk management.',
        N'Business', N'Beginner', 590000,
        N'https://picsum.photos/seed/Business13/1200/700'
    ),
    (
        N'IELTS Preparation (Academic)',
        N'Practice IELTS reading, writing, listening and speaking skills.',
        N'Languages', N'Intermediate', 1090000,
        N'https://picsum.photos/seed/Languages14/1200/700'
    ),
    (
        N'SEO & Content Marketing',
        N'Learn search engine optimization, keyword research and content strategy.',
        N'Marketing', N'Intermediate', 560000,
        N'https://picsum.photos/seed/Marketing15/1200/700'
    );

    INSERT INTO dbo.Courses
    (
        Title,
        Description,
        CategoryId,
        CourseLevel,
        Price,
        ImageUrl,
        Status,
        InstructorId
    )
    SELECT
        s.Title,
        s.Description,
        cat.CategoryId,
        s.CourseLevel,
        s.Price,
        s.ImageUrl,
        'PUBLISHED',
        @InstructorId
    FROM @Samples s
    INNER JOIN dbo.Categories cat
        ON cat.Name = s.CategoryName
    WHERE NOT EXISTS (
        SELECT 1
        FROM dbo.Courses c
        WHERE c.Title = s.Title
    );

    INSERT INTO dbo.Sections (CourseId, Title, Position)
    SELECT
        c.CourseId,
        N'Getting Started',
        1
    FROM dbo.Courses c
    INNER JOIN @Samples s
        ON s.Title = c.Title
    WHERE c.Status = 'PUBLISHED'
      AND NOT EXISTS (
          SELECT 1
          FROM dbo.Sections sec
          WHERE sec.CourseId = c.CourseId
      );

    INSERT INTO dbo.Lessons
    (
        SectionId,
        Title,
        ContentType,
        TextContent,
        Minutes,
        IsPreview,
        Position
    )
    SELECT
        sec.SectionId,
        lesson.Title,
        'TEXT',
        lesson.Content,
        lesson.Minutes,
        lesson.IsPreview,
        lesson.Position
    FROM dbo.Sections sec
    INNER JOIN dbo.Courses c
        ON c.CourseId = sec.CourseId
    INNER JOIN @Samples s
        ON s.Title = c.Title
    CROSS APPLY (
        VALUES
        (
            N'Introduction to ' + c.Title,
            N'Welcome to this course. Learn the goals, topics and skills covered.',
            10, 1, 1
        ),
        (
            N'Core Concepts and Fundamentals',
            N'Study the essential concepts and complete the practice activities.',
            25, 0, 2
        ),
        (
            N'Practical Project and Review',
            N'Apply your knowledge through a practical project and review.',
            35, 0, 3
        )
    ) lesson(Title, Content, Minutes, IsPreview, Position)
    WHERE sec.Position = 1
      AND c.Status = 'PUBLISHED'
      AND NOT EXISTS (
          SELECT 1
          FROM dbo.Lessons l
          WHERE l.SectionId = sec.SectionId
            AND l.Position = lesson.Position
      );
END;


^^^ END_STATEMENT ^^^