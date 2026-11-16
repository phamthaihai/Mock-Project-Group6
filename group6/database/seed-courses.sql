USE SkillTrail;
GO
SET NOCOUNT ON;
BEGIN TRAN;

SET IDENTITY_INSERT Users ON;
INSERT INTO Users (UserId, FullName, Email, PasswordHash, Role, IsAuthor, Locked) VALUES (102, N'Nguyen Van An', N'instructor@skilltrail.com', '!', 'STUDENT', 1, 1);
INSERT INTO Users (UserId, FullName, Email, PasswordHash, Role, IsAuthor, Locked) VALUES (103, N'Le Thu Ha', N'le.thu.ha@skilltrail.com', '!', 'STUDENT', 1, 1);
INSERT INTO Users (UserId, FullName, Email, PasswordHash, Role, IsAuthor, Locked) VALUES (104, N'Marcus Reyes', N'marcus.reyes@skilltrail.com', '!', 'STUDENT', 1, 1);
INSERT INTO Users (UserId, FullName, Email, PasswordHash, Role, IsAuthor, Locked) VALUES (105, N'Dr. Linh Tran', N'dr.linh.tran@skilltrail.com', '!', 'STUDENT', 1, 1);
INSERT INTO Users (UserId, FullName, Email, PasswordHash, Role, IsAuthor, Locked) VALUES (106, N'Priya Nair', N'priya.nair@skilltrail.com', '!', 'STUDENT', 1, 1);
INSERT INTO Users (UserId, FullName, Email, PasswordHash, Role, IsAuthor, Locked) VALUES (107, N'Mai Anh', N'mai.anh@skilltrail.com', '!', 'STUDENT', 1, 1);
INSERT INTO Users (UserId, FullName, Email, PasswordHash, Role, IsAuthor, Locked) VALUES (108, N'Khanh Vy', N'khanh.vy@skilltrail.com', '!', 'STUDENT', 1, 1);
INSERT INTO Users (UserId, FullName, Email, PasswordHash, Role, IsAuthor, Locked) VALUES (109, N'Emma Walker', N'emma.walker@skilltrail.com', '!', 'STUDENT', 1, 1);
INSERT INTO Users (UserId, FullName, Email, PasswordHash, Role, IsAuthor, Locked) VALUES (110, N'Samir Oduya', N'samir.oduya@skilltrail.com', '!', 'STUDENT', 1, 1);
INSERT INTO Users (UserId, FullName, Email, PasswordHash, Role, IsAuthor, Locked) VALUES (111, N'Hoang Minh', N'hoang.minh@skilltrail.com', '!', 'STUDENT', 1, 1);
INSERT INTO Users (UserId, FullName, Email, PasswordHash, Role, IsAuthor, Locked) VALUES (112, N'Nina Petrova', N'nina.petrova@skilltrail.com', '!', 'STUDENT', 1, 1);
INSERT INTO Users (UserId, FullName, Email, PasswordHash, Role, IsAuthor, Locked) VALUES (113, N'Carlos Mendes', N'carlos.mendes@skilltrail.com', '!', 'STUDENT', 1, 1);
INSERT INTO Users (UserId, FullName, Email, PasswordHash, Role, IsAuthor, Locked) VALUES (114, N'Olivia Grant', N'olivia.grant@skilltrail.com', '!', 'STUDENT', 1, 1);
INSERT INTO Users (UserId, FullName, Email, PasswordHash, Role, IsAuthor, Locked) VALUES (115, N'Jacob Lee', N'jacob.lee@skilltrail.com', '!', 'STUDENT', 1, 1);
INSERT INTO Users (UserId, FullName, Email, PasswordHash, Role, IsAuthor, Locked) VALUES (116, N'Thanh Huong', N'thanh.huong@skilltrail.com', '!', 'STUDENT', 1, 1);
SET IDENTITY_INSERT Users OFF;

SET IDENTITY_INSERT Courses ON;
INSERT INTO Courses (CourseId, Title, Description, CategoryId, CourseLevel, Price, ImageUrl, Status, InstructorId, Details) VALUES (1, N'Java Full-Stack Bootcamp', N'Go from Java basics to a deployed full-stack app: Spring Boot REST APIs, SQL Server, JWT security and a responsive front-end.', 1, 'Intermediate', 1290000, N'https://loremflickr.com/1200/700/coding,programming?lock=21', 'PUBLISHED', 102, N'{"tagline":"Build and ship a real full-stack application.","learn":["Design REST APIs with Spring Boot","Model data with JPA and SQL Server","Secure apps with JWT and roles","Build a responsive front-end with fetch"],"requirements":["Basic programming knowledge","A laptop with JDK 21 installed"],"skills":["Spring Boot","REST","JPA","SQL Server","JWT","Git"],"instructorBio":"Senior backend engineer with 10 years of experience building enterprise Java systems and mentoring junior developers."}');
INSERT INTO Courses (CourseId, Title, Description, CategoryId, CourseLevel, Price, ImageUrl, Status, InstructorId, Details) VALUES (2, N'Intro to Web Development', N'Learn HTML, CSS and JavaScript from zero and publish your first responsive website.', 1, 'Beginner', 0, N'https://loremflickr.com/1200/700/laptop,webdesign?lock=22', 'PUBLISHED', 103, N'{"tagline":"Your first website in a weekend. Free.","learn":["Structure pages with semantic HTML","Style layouts with CSS and Bootstrap","Add interactivity with JavaScript","Publish a site to the web"],"requirements":["No experience needed","Any computer and a browser"],"skills":["HTML","CSS","Bootstrap","JavaScript"],"instructorBio":"Front-end developer and teacher who has helped over 5,000 beginners write their first line of code."}');
INSERT INTO Courses (CourseId, Title, Description, CategoryId, CourseLevel, Price, ImageUrl, Status, InstructorId, Details) VALUES (3, N'Docker & DevOps Essentials', N'Containerize applications, automate builds and deploy with confidence using Docker, CI/CD and cloud basics.', 1, 'Intermediate', 790000, N'https://loremflickr.com/1200/700/server,datacenter?lock=23', 'PUBLISHED', 104, N'{"tagline":"Ship faster with containers and pipelines.","learn":["Write Dockerfiles and compose files","Automate tests with CI/CD","Deploy containers to the cloud","Monitor and troubleshoot services"],"requirements":["Comfort with the command line","One programming language"],"skills":["Docker","CI/CD","Linux","Cloud"],"instructorBio":"Cloud engineer who runs production platforms serving millions of requests per day."}');
INSERT INTO Courses (CourseId, Title, Description, CategoryId, CourseLevel, Price, ImageUrl, Status, InstructorId, Details) VALUES (4, N'Machine Learning Foundations', N'Understand the core ideas behind machine learning and build, evaluate and deploy your first models in Python.', 2, 'Intermediate', 1490000, N'https://loremflickr.com/1200/700/artificialintelligence,technology?lock=24', 'PUBLISHED', 105, N'{"tagline":"From data to models that actually work.","learn":["Prepare and explore real datasets","Train regression and classification models","Evaluate models and avoid overfitting","Serve a model behind a simple API"],"requirements":["Basic Python","High-school level maths"],"skills":["Python","scikit-learn","Pandas","Model evaluation"],"instructorBio":"Machine learning researcher turned industry practitioner, with a PhD in applied statistics."}');
INSERT INTO Courses (CourseId, Title, Description, CategoryId, CourseLevel, Price, ImageUrl, Status, InstructorId, Details) VALUES (5, N'SQL & Data Analysis', N'Query, clean and summarise data with SQL, then turn results into clear dashboards and insights.', 2, 'Beginner', 450000, N'https://loremflickr.com/1200/700/data,analytics?lock=25', 'PUBLISHED', 106, N'{"tagline":"Ask better questions of your data.","learn":["Write SELECT, JOIN and GROUP BY queries","Clean messy data","Design simple relational schemas","Present findings with charts"],"requirements":["No prior experience","SQL Server or any SQL database"],"skills":["SQL","T-SQL","Data cleaning","Dashboards"],"instructorBio":"Data analyst who has built reporting for retail and fintech teams."}');
INSERT INTO Courses (CourseId, Title, Description, CategoryId, CourseLevel, Price, ImageUrl, Status, InstructorId, Details) VALUES (6, N'UI/UX Design Essentials', N'Learn the full design process: research, wireframes, visual design and prototyping, with a portfolio project.', 3, 'Beginner', 790000, N'https://loremflickr.com/1200/700/design,interface?lock=26', 'PUBLISHED', 107, N'{"tagline":"Design products people love to use.","learn":["Run quick user research","Create wireframes and flows","Apply colour, type and layout","Prototype and test your design"],"requirements":["No experience needed","A free Figma account"],"skills":["Figma","Wireframing","Prototyping","Usability testing"],"instructorBio":"Product designer with 8 years of experience across fintech and e-commerce apps."}');
INSERT INTO Courses (CourseId, Title, Description, CategoryId, CourseLevel, Price, ImageUrl, Status, InstructorId, Details) VALUES (7, N'Digital Marketing Masterclass', N'Plan and run campaigns across search, social and email, and measure what actually drives growth.', 6, 'Beginner', 650000, N'https://loremflickr.com/1200/700/marketing,socialmedia?lock=27', 'PUBLISHED', 108, N'{"tagline":"Grow an audience and measure the results.","learn":["Build a simple marketing funnel","Run search and social campaigns","Write emails that convert","Read analytics and report ROI"],"requirements":["No experience needed"],"skills":["SEO","Social ads","Email marketing","Analytics"],"instructorBio":"Growth marketer who has scaled several local brands from zero to national reach."}');
INSERT INTO Courses (CourseId, Title, Description, CategoryId, CourseLevel, Price, ImageUrl, Status, InstructorId, Details) VALUES (8, N'Business English for Professionals', N'Communicate clearly in meetings, emails and negotiations, with real workplace scenarios and feedback.', 5, 'Intermediate', 890000, N'https://loremflickr.com/1200/700/meeting,office?lock=28', 'PUBLISHED', 109, N'{"tagline":"Speak and write with confidence at work.","learn":["Run and join meetings in English","Write clear professional emails","Handle calls and small talk","Negotiate and present ideas"],"requirements":["Intermediate English (B1 or above)"],"skills":["Speaking","Email writing","Presenting","Negotiation"],"instructorBio":"Certified English trainer with 12 years coaching teams at international companies."}');
INSERT INTO Courses (CourseId, Title, Description, CategoryId, CourseLevel, Price, ImageUrl, Status, InstructorId, Details) VALUES (9, N'Public Speaking & Presentation Skills', N'Overcome nerves, structure a message and deliver talks and pitches that keep an audience engaged.', 4, 'Beginner', 390000, N'https://loremflickr.com/1200/700/speaker,presentation?lock=29', 'PUBLISHED', 110, N'{"tagline":"Say it clearly. Be remembered.","learn":["Structure a talk with a clear message","Control nerves and body language","Design simple, effective slides","Handle questions with confidence"],"requirements":["No experience needed"],"skills":["Storytelling","Slide design","Confidence","Q&A handling"],"instructorBio":"Keynote speaker and coach who has trained thousands of professionals."}');
INSERT INTO Courses (CourseId, Title, Description, CategoryId, CourseLevel, Price, ImageUrl, Status, InstructorId, Details) VALUES (10, N'React for Modern Web Apps', N'Build fast, component-based interfaces with React, hooks and routing, and connect them to a REST API.', 1, 'Intermediate', 990000, N'https://loremflickr.com/1200/700/code,screen?lock=30', 'PUBLISHED', 111, N'{"tagline":"Build interactive UIs the modern way.","learn":["Build UIs from reusable components","Manage state with hooks","Add routing and forms","Fetch and display data from an API"],"requirements":["Solid HTML, CSS and JavaScript","Node.js installed"],"skills":["React","Hooks","React Router","REST APIs"],"instructorBio":"Front-end lead who has shipped React apps for startups and banks."}');
INSERT INTO Courses (CourseId, Title, Description, CategoryId, CourseLevel, Price, ImageUrl, Status, InstructorId, Details) VALUES (11, N'Data Visualization with Power BI', N'Connect data sources, model them and build interactive dashboards that tell a clear story.', 2, 'Beginner', 520000, N'https://loremflickr.com/1200/700/dashboard,analytics?lock=31', 'PUBLISHED', 112, N'{"tagline":"Turn spreadsheets into dashboards people use.","learn":["Import and clean data","Build relationships and measures","Design interactive dashboards","Publish and share reports"],"requirements":["Basic Excel skills"],"skills":["Power BI","DAX","Data modelling","Dashboards"],"instructorBio":"BI consultant who has built reporting for logistics and retail teams."}');
INSERT INTO Courses (CourseId, Title, Description, CategoryId, CourseLevel, Price, ImageUrl, Status, InstructorId, Details) VALUES (12, N'Graphic Design with Photoshop & Illustrator', N'Learn the tools and design principles to create logos, posters and social media graphics.', 3, 'Beginner', 750000, N'https://loremflickr.com/1200/700/illustration,designer?lock=32', 'PUBLISHED', 113, N'{"tagline":"Create visuals that stand out.","learn":["Master layers, masks and vector shapes","Apply colour, type and composition","Design logos and brand assets","Prepare files for print and web"],"requirements":["A computer that can run design software"],"skills":["Photoshop","Illustrator","Branding","Typography"],"instructorBio":"Brand designer with a decade of experience in agencies and freelance work."}');
INSERT INTO Courses (CourseId, Title, Description, CategoryId, CourseLevel, Price, ImageUrl, Status, InstructorId, Details) VALUES (13, N'Project Management Fundamentals', N'Plan, run and deliver projects on time using proven methods, from scope and schedule to risks and reporting.', 4, 'Beginner', 590000, N'https://loremflickr.com/1200/700/teamwork,planning?lock=33', 'PUBLISHED', 114, N'{"tagline":"Deliver projects on time and on budget.","learn":["Define scope, goals and deliverables","Build realistic schedules and budgets","Manage risks and stakeholders","Run projects with Agile and Kanban"],"requirements":["No experience needed"],"skills":["Planning","Agile","Risk management","Stakeholders"],"instructorBio":"PMP-certified project manager who has led cross-functional teams of up to 40 people."}');
INSERT INTO Courses (CourseId, Title, Description, CategoryId, CourseLevel, Price, ImageUrl, Status, InstructorId, Details) VALUES (14, N'IELTS Preparation (Academic)', N'Targeted practice for all four IELTS Academic skills, with strategies, timed tasks and feedback.', 5, 'Intermediate', 1090000, N'https://loremflickr.com/1200/700/studying,exam?lock=34', 'PUBLISHED', 115, N'{"tagline":"Aim for the band score you need.","learn":["Understand each IELTS section and task type","Improve reading and listening speed","Write clear Task 1 and Task 2 answers","Speak fluently in all three parts"],"requirements":["Intermediate English (B1 or above)"],"skills":["Reading","Listening","Academic writing","Speaking"],"instructorBio":"IELTS examiner trainer who has coached hundreds of students to their target score."}');
INSERT INTO Courses (CourseId, Title, Description, CategoryId, CourseLevel, Price, ImageUrl, Status, InstructorId, Details) VALUES (15, N'SEO & Content Marketing', N'Rank higher on search engines and build content that attracts and converts the right audience.', 6, 'Intermediate', 560000, N'https://loremflickr.com/1200/700/laptop,marketing?lock=35', 'PUBLISHED', 116, N'{"tagline":"Get found, then get chosen.","learn":["Research keywords and search intent","Optimise pages for search engines","Plan and write content that converts","Track rankings and traffic"],"requirements":["Basic marketing knowledge"],"skills":["SEO","Keyword research","Content strategy","Analytics"],"instructorBio":"SEO specialist who has grown organic traffic for e-commerce and education sites."}');
SET IDENTITY_INSERT Courses OFF;

SET IDENTITY_INSERT Sections ON;
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (1, 1, N'Java refresher', 1);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (2, 1, N'Spring Boot REST', 2);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (3, 1, N'Data and security', 3);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (4, 1, N'Capstone', 4);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (5, 2, N'HTML basics', 1);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (6, 2, N'CSS and Bootstrap', 2);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (7, 2, N'JavaScript', 3);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (8, 3, N'Containers', 1);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (9, 3, N'Pipelines', 2);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (10, 3, N'Deploy', 3);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (11, 4, N'Foundations', 1);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (12, 4, N'Core models', 2);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (13, 4, N'In practice', 3);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (14, 5, N'SQL basics', 1);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (15, 5, N'Working with data', 2);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (16, 5, N'Insights', 3);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (17, 6, N'Research', 1);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (18, 6, N'Design', 2);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (19, 6, N'Deliver', 3);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (20, 7, N'Strategy', 1);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (21, 7, N'Channels', 2);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (22, 7, N'Measure', 3);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (23, 8, N'Everyday work', 1);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (24, 8, N'Speaking up', 2);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (25, 8, N'Advanced', 3);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (26, 9, N'Foundations', 1);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (27, 9, N'Delivery', 2);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (28, 9, N'Pitching', 3);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (29, 10, N'React basics', 1);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (30, 10, N'Hooks and data', 2);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (31, 10, N'Real apps', 3);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (32, 11, N'Getting started', 1);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (33, 11, N'Modelling', 2);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (34, 11, N'Dashboards', 3);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (35, 12, N'Design basics', 1);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (36, 12, N'Photoshop', 2);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (37, 12, N'Illustrator', 3);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (38, 13, N'Foundations', 1);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (39, 13, N'Planning', 2);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (40, 13, N'Delivery', 3);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (41, 14, N'Reading and listening', 1);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (42, 14, N'Writing', 2);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (43, 14, N'Speaking', 3);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (44, 15, N'SEO basics', 1);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (45, 15, N'On-page SEO', 2);
INSERT INTO Sections (SectionId, CourseId, Title, Position) VALUES (46, 15, N'Content', 3);
SET IDENTITY_INSERT Sections OFF;

SET IDENTITY_INSERT Lessons ON;
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (1, 1, N'OOP essentials', 'VIDEO', N'https://example.com/videos/oop-essentials', N'## Objectives

- Understand the key ideas behind oop essentials
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- OOP essentials is a core building block of Java Full-Stack Bootcamp.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "OOP essentials" and compare your result with the sample solution.', 18, 1, 1);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (2, 1, N'Collections and streams', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind collections and streams
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Collections and streams is a core building block of Java Full-Stack Bootcamp.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Collections and streams" and compare your result with the sample solution.', 22, 0, 2);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (3, 1, N'Exceptions and testing', 'VIDEO', N'https://example.com/videos/exceptions-and-testing', N'## Objectives

- Understand the key ideas behind exceptions and testing
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Exceptions and testing is a core building block of Java Full-Stack Bootcamp.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Exceptions and testing" and compare your result with the sample solution.', 15, 0, 3);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (4, 2, N'Controllers and services', 'FILE', N'https://example.com/files/controllers-and-services.pdf', N'## Objectives

- Understand the key ideas behind controllers and services
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Controllers and services is a core building block of Java Full-Stack Bootcamp.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Controllers and services" and compare your result with the sample solution.', 25, 0, 4);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (5, 2, N'Validation and error handling', 'VIDEO', N'https://example.com/videos/validation-and-error-handling', N'## Objectives

- Understand the key ideas behind validation and error handling
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Validation and error handling is a core building block of Java Full-Stack Bootcamp.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Validation and error handling" and compare your result with the sample solution.', 20, 0, 5);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (6, 2, N'Swagger documentation', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind swagger documentation
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Swagger documentation is a core building block of Java Full-Stack Bootcamp.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Swagger documentation" and compare your result with the sample solution.', 12, 0, 6);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (7, 3, N'JPA with SQL Server', 'VIDEO', N'https://example.com/videos/jpa-with-sql-server', N'## Objectives

- Understand the key ideas behind jpa with sql server
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- JPA with SQL Server is a core building block of Java Full-Stack Bootcamp.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "JPA with SQL Server" and compare your result with the sample solution.', 28, 0, 7);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (8, 3, N'Authentication with JWT', 'FILE', N'https://example.com/files/authentication-with-jwt.pdf', N'## Objectives

- Understand the key ideas behind authentication with jwt
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Authentication with JWT is a core building block of Java Full-Stack Bootcamp.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Authentication with JWT" and compare your result with the sample solution.', 30, 0, 8);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (9, 3, N'Role-based access', 'VIDEO', N'https://example.com/videos/role-based-access', N'## Objectives

- Understand the key ideas behind role-based access
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Role-based access is a core building block of Java Full-Stack Bootcamp.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Role-based access" and compare your result with the sample solution.', 18, 0, 9);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (10, 4, N'Connect the front-end', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind connect the front-end
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Connect the front-end is a core building block of Java Full-Stack Bootcamp.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Connect the front-end" and compare your result with the sample solution.', 24, 0, 10);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (11, 4, N'Deploy the app', 'VIDEO', N'https://example.com/videos/deploy-the-app', N'## Objectives

- Understand the key ideas behind deploy the app
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Deploy the app is a core building block of Java Full-Stack Bootcamp.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Deploy the app" and compare your result with the sample solution.', 16, 0, 11);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (12, 5, N'Page structure', 'VIDEO', N'https://example.com/videos/page-structure', N'## Objectives

- Understand the key ideas behind page structure
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Page structure is a core building block of Intro to Web Development.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Page structure" and compare your result with the sample solution.', 10, 1, 1);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (13, 5, N'Forms and tables', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind forms and tables
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Forms and tables is a core building block of Intro to Web Development.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Forms and tables" and compare your result with the sample solution.', 14, 0, 2);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (14, 6, N'Selectors and layout', 'VIDEO', N'https://example.com/videos/selectors-and-layout', N'## Objectives

- Understand the key ideas behind selectors and layout
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Selectors and layout is a core building block of Intro to Web Development.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Selectors and layout" and compare your result with the sample solution.', 16, 0, 3);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (15, 6, N'Responsive design', 'FILE', N'https://example.com/files/responsive-design.pdf', N'## Objectives

- Understand the key ideas behind responsive design
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Responsive design is a core building block of Intro to Web Development.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Responsive design" and compare your result with the sample solution.', 18, 0, 4);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (16, 7, N'DOM and events', 'VIDEO', N'https://example.com/videos/dom-and-events', N'## Objectives

- Understand the key ideas behind dom and events
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- DOM and events is a core building block of Intro to Web Development.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "DOM and events" and compare your result with the sample solution.', 20, 0, 5);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (17, 7, N'Fetching data', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind fetching data
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Fetching data is a core building block of Intro to Web Development.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Fetching data" and compare your result with the sample solution.', 15, 0, 6);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (18, 8, N'Images and containers', 'VIDEO', N'https://example.com/videos/images-and-containers', N'## Objectives

- Understand the key ideas behind images and containers
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Images and containers is a core building block of Docker & DevOps Essentials.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Images and containers" and compare your result with the sample solution.', 20, 1, 1);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (19, 8, N'Dockerfile deep dive', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind dockerfile deep dive
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Dockerfile deep dive is a core building block of Docker & DevOps Essentials.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Dockerfile deep dive" and compare your result with the sample solution.', 25, 0, 2);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (20, 8, N'Docker Compose', 'VIDEO', N'https://example.com/videos/docker-compose', N'## Objectives

- Understand the key ideas behind docker compose
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Docker Compose is a core building block of Docker & DevOps Essentials.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Docker Compose" and compare your result with the sample solution.', 18, 0, 3);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (21, 9, N'Git workflows', 'FILE', N'https://example.com/files/git-workflows.pdf', N'## Objectives

- Understand the key ideas behind git workflows
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Git workflows is a core building block of Docker & DevOps Essentials.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Git workflows" and compare your result with the sample solution.', 15, 0, 4);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (22, 9, N'Build and test automation', 'VIDEO', N'https://example.com/videos/build-and-test-automation', N'## Objectives

- Understand the key ideas behind build and test automation
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Build and test automation is a core building block of Docker & DevOps Essentials.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Build and test automation" and compare your result with the sample solution.', 28, 0, 5);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (23, 10, N'Cloud deployment', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind cloud deployment
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Cloud deployment is a core building block of Docker & DevOps Essentials.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Cloud deployment" and compare your result with the sample solution.', 30, 0, 6);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (24, 10, N'Monitoring basics', 'VIDEO', N'https://example.com/videos/monitoring-basics', N'## Objectives

- Understand the key ideas behind monitoring basics
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Monitoring basics is a core building block of Docker & DevOps Essentials.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Monitoring basics" and compare your result with the sample solution.', 16, 0, 7);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (25, 11, N'What is machine learning', 'VIDEO', N'https://example.com/videos/what-is-machine-learning', N'## Objectives

- Understand the key ideas behind what is machine learning
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- What is machine learning is a core building block of Machine Learning Foundations.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "What is machine learning" and compare your result with the sample solution.', 12, 1, 1);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (26, 11, N'Data preparation', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind data preparation
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Data preparation is a core building block of Machine Learning Foundations.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Data preparation" and compare your result with the sample solution.', 26, 0, 2);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (27, 11, N'Exploratory analysis', 'VIDEO', N'https://example.com/videos/exploratory-analysis', N'## Objectives

- Understand the key ideas behind exploratory analysis
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Exploratory analysis is a core building block of Machine Learning Foundations.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Exploratory analysis" and compare your result with the sample solution.', 24, 0, 3);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (28, 12, N'Linear regression', 'FILE', N'https://example.com/files/linear-regression.pdf', N'## Objectives

- Understand the key ideas behind linear regression
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Linear regression is a core building block of Machine Learning Foundations.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Linear regression" and compare your result with the sample solution.', 28, 0, 4);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (29, 12, N'Classification', 'VIDEO', N'https://example.com/videos/classification', N'## Objectives

- Understand the key ideas behind classification
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Classification is a core building block of Machine Learning Foundations.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Classification" and compare your result with the sample solution.', 30, 0, 5);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (30, 12, N'Trees and ensembles', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind trees and ensembles
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Trees and ensembles is a core building block of Machine Learning Foundations.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Trees and ensembles" and compare your result with the sample solution.', 32, 0, 6);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (31, 13, N'Evaluation and tuning', 'VIDEO', N'https://example.com/videos/evaluation-and-tuning', N'## Objectives

- Understand the key ideas behind evaluation and tuning
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Evaluation and tuning is a core building block of Machine Learning Foundations.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Evaluation and tuning" and compare your result with the sample solution.', 26, 0, 7);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (32, 13, N'Deploying a model', 'FILE', N'https://example.com/files/deploying-a-model.pdf', N'## Objectives

- Understand the key ideas behind deploying a model
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Deploying a model is a core building block of Machine Learning Foundations.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Deploying a model" and compare your result with the sample solution.', 22, 0, 8);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (33, 14, N'SELECT and filtering', 'VIDEO', N'https://example.com/videos/select-and-filtering', N'## Objectives

- Understand the key ideas behind select and filtering
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- SELECT and filtering is a core building block of SQL & Data Analysis.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "SELECT and filtering" and compare your result with the sample solution.', 14, 1, 1);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (34, 14, N'Joins', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind joins
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Joins is a core building block of SQL & Data Analysis.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Joins" and compare your result with the sample solution.', 22, 0, 2);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (35, 14, N'Aggregations', 'VIDEO', N'https://example.com/videos/aggregations', N'## Objectives

- Understand the key ideas behind aggregations
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Aggregations is a core building block of SQL & Data Analysis.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Aggregations" and compare your result with the sample solution.', 20, 0, 3);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (36, 15, N'Cleaning data', 'FILE', N'https://example.com/files/cleaning-data.pdf', N'## Objectives

- Understand the key ideas behind cleaning data
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Cleaning data is a core building block of SQL & Data Analysis.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Cleaning data" and compare your result with the sample solution.', 18, 0, 4);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (37, 15, N'Subqueries and CTEs', 'VIDEO', N'https://example.com/videos/subqueries-and-ctes', N'## Objectives

- Understand the key ideas behind subqueries and ctes
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Subqueries and CTEs is a core building block of SQL & Data Analysis.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Subqueries and CTEs" and compare your result with the sample solution.', 24, 0, 5);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (38, 16, N'Designing a schema', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind designing a schema
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Designing a schema is a core building block of SQL & Data Analysis.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Designing a schema" and compare your result with the sample solution.', 16, 0, 6);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (39, 16, N'Building a dashboard', 'VIDEO', N'https://example.com/videos/building-a-dashboard', N'## Objectives

- Understand the key ideas behind building a dashboard
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Building a dashboard is a core building block of SQL & Data Analysis.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Building a dashboard" and compare your result with the sample solution.', 26, 0, 7);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (40, 17, N'Understanding users', 'VIDEO', N'https://example.com/videos/understanding-users', N'## Objectives

- Understand the key ideas behind understanding users
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Understanding users is a core building block of UI/UX Design Essentials.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Understanding users" and compare your result with the sample solution.', 15, 1, 1);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (41, 17, N'Personas and journeys', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind personas and journeys
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Personas and journeys is a core building block of UI/UX Design Essentials.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Personas and journeys" and compare your result with the sample solution.', 18, 0, 2);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (42, 18, N'Wireframes', 'VIDEO', N'https://example.com/videos/wireframes', N'## Objectives

- Understand the key ideas behind wireframes
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Wireframes is a core building block of UI/UX Design Essentials.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Wireframes" and compare your result with the sample solution.', 22, 0, 3);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (43, 18, N'Visual design basics', 'FILE', N'https://example.com/files/visual-design-basics.pdf', N'## Objectives

- Understand the key ideas behind visual design basics
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Visual design basics is a core building block of UI/UX Design Essentials.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Visual design basics" and compare your result with the sample solution.', 26, 0, 4);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (44, 18, N'Design systems', 'VIDEO', N'https://example.com/videos/design-systems', N'## Objectives

- Understand the key ideas behind design systems
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Design systems is a core building block of UI/UX Design Essentials.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Design systems" and compare your result with the sample solution.', 20, 0, 5);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (45, 19, N'Prototyping', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind prototyping
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Prototyping is a core building block of UI/UX Design Essentials.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Prototyping" and compare your result with the sample solution.', 24, 0, 6);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (46, 19, N'Usability testing', 'VIDEO', N'https://example.com/videos/usability-testing', N'## Objectives

- Understand the key ideas behind usability testing
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Usability testing is a core building block of UI/UX Design Essentials.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Usability testing" and compare your result with the sample solution.', 18, 0, 7);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (47, 20, N'Funnels and goals', 'VIDEO', N'https://example.com/videos/funnels-and-goals', N'## Objectives

- Understand the key ideas behind funnels and goals
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Funnels and goals is a core building block of Digital Marketing Masterclass.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Funnels and goals" and compare your result with the sample solution.', 14, 1, 1);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (48, 20, N'Audience research', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind audience research
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Audience research is a core building block of Digital Marketing Masterclass.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Audience research" and compare your result with the sample solution.', 16, 0, 2);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (49, 21, N'Search and SEO', 'VIDEO', N'https://example.com/videos/search-and-seo', N'## Objectives

- Understand the key ideas behind search and seo
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Search and SEO is a core building block of Digital Marketing Masterclass.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Search and SEO" and compare your result with the sample solution.', 24, 0, 3);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (50, 21, N'Social media ads', 'FILE', N'https://example.com/files/social-media-ads.pdf', N'## Objectives

- Understand the key ideas behind social media ads
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Social media ads is a core building block of Digital Marketing Masterclass.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Social media ads" and compare your result with the sample solution.', 26, 0, 4);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (51, 21, N'Email marketing', 'VIDEO', N'https://example.com/videos/email-marketing', N'## Objectives

- Understand the key ideas behind email marketing
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Email marketing is a core building block of Digital Marketing Masterclass.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Email marketing" and compare your result with the sample solution.', 18, 0, 5);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (52, 22, N'Analytics and reporting', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind analytics and reporting
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Analytics and reporting is a core building block of Digital Marketing Masterclass.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Analytics and reporting" and compare your result with the sample solution.', 22, 0, 6);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (53, 23, N'Meetings and discussions', 'VIDEO', N'https://example.com/videos/meetings-and-discussions', N'## Objectives

- Understand the key ideas behind meetings and discussions
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Meetings and discussions is a core building block of Business English for Professionals.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Meetings and discussions" and compare your result with the sample solution.', 20, 1, 1);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (54, 23, N'Professional emails', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind professional emails
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Professional emails is a core building block of Business English for Professionals.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Professional emails" and compare your result with the sample solution.', 18, 0, 2);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (55, 24, N'Phone and video calls', 'VIDEO', N'https://example.com/videos/phone-and-video-calls', N'## Objectives

- Understand the key ideas behind phone and video calls
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Phone and video calls is a core building block of Business English for Professionals.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Phone and video calls" and compare your result with the sample solution.', 22, 0, 3);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (56, 24, N'Small talk and networking', 'FILE', N'https://example.com/files/small-talk-and-networking.pdf', N'## Objectives

- Understand the key ideas behind small talk and networking
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Small talk and networking is a core building block of Business English for Professionals.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Small talk and networking" and compare your result with the sample solution.', 16, 0, 4);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (57, 25, N'Presentations', 'VIDEO', N'https://example.com/videos/presentations', N'## Objectives

- Understand the key ideas behind presentations
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Presentations is a core building block of Business English for Professionals.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Presentations" and compare your result with the sample solution.', 26, 0, 5);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (58, 25, N'Negotiation', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind negotiation
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Negotiation is a core building block of Business English for Professionals.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Negotiation" and compare your result with the sample solution.', 24, 0, 6);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (59, 26, N'Know your audience', 'VIDEO', N'https://example.com/videos/know-your-audience', N'## Objectives

- Understand the key ideas behind know your audience
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Know your audience is a core building block of Public Speaking & Presentation Skills.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Know your audience" and compare your result with the sample solution.', 12, 1, 1);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (60, 26, N'Structuring your message', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind structuring your message
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Structuring your message is a core building block of Public Speaking & Presentation Skills.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Structuring your message" and compare your result with the sample solution.', 16, 0, 2);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (61, 27, N'Voice and body language', 'VIDEO', N'https://example.com/videos/voice-and-body-language', N'## Objectives

- Understand the key ideas behind voice and body language
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Voice and body language is a core building block of Public Speaking & Presentation Skills.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Voice and body language" and compare your result with the sample solution.', 18, 0, 3);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (62, 27, N'Managing nerves', 'FILE', N'https://example.com/files/managing-nerves.pdf', N'## Objectives

- Understand the key ideas behind managing nerves
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Managing nerves is a core building block of Public Speaking & Presentation Skills.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Managing nerves" and compare your result with the sample solution.', 14, 0, 4);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (63, 28, N'Designing slides', 'VIDEO', N'https://example.com/videos/designing-slides', N'## Objectives

- Understand the key ideas behind designing slides
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Designing slides is a core building block of Public Speaking & Presentation Skills.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Designing slides" and compare your result with the sample solution.', 20, 0, 5);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (64, 28, N'Handling Q&A', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind handling q&a
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Handling Q&A is a core building block of Public Speaking & Presentation Skills.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Handling Q&A" and compare your result with the sample solution.', 15, 0, 6);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (65, 29, N'JSX and components', 'VIDEO', N'https://example.com/videos/jsx-and-components', N'## Objectives

- Understand the key ideas behind jsx and components
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- JSX and components is a core building block of React for Modern Web Apps.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "JSX and components" and compare your result with the sample solution.', 18, 1, 1);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (66, 29, N'Props and state', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind props and state
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Props and state is a core building block of React for Modern Web Apps.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Props and state" and compare your result with the sample solution.', 24, 0, 2);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (67, 29, N'Handling events', 'VIDEO', N'https://example.com/videos/handling-events', N'## Objectives

- Understand the key ideas behind handling events
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Handling events is a core building block of React for Modern Web Apps.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Handling events" and compare your result with the sample solution.', 16, 0, 3);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (68, 30, N'useEffect and data fetching', 'FILE', N'https://example.com/files/useeffect-and-data-fetching.pdf', N'## Objectives

- Understand the key ideas behind useeffect and data fetching
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- useEffect and data fetching is a core building block of React for Modern Web Apps.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "useEffect and data fetching" and compare your result with the sample solution.', 28, 0, 4);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (69, 30, N'Forms and validation', 'VIDEO', N'https://example.com/videos/forms-and-validation', N'## Objectives

- Understand the key ideas behind forms and validation
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Forms and validation is a core building block of React for Modern Web Apps.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Forms and validation" and compare your result with the sample solution.', 22, 0, 5);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (70, 31, N'Routing', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind routing
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Routing is a core building block of React for Modern Web Apps.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Routing" and compare your result with the sample solution.', 20, 0, 6);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (71, 31, N'Build a mini dashboard', 'VIDEO', N'https://example.com/videos/build-a-mini-dashboard', N'## Objectives

- Understand the key ideas behind build a mini dashboard
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Build a mini dashboard is a core building block of React for Modern Web Apps.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Build a mini dashboard" and compare your result with the sample solution.', 30, 0, 7);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (72, 32, N'Power BI overview', 'VIDEO', N'https://example.com/videos/power-bi-overview', N'## Objectives

- Understand the key ideas behind power bi overview
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Power BI overview is a core building block of Data Visualization with Power BI.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Power BI overview" and compare your result with the sample solution.', 12, 1, 1);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (73, 32, N'Connecting to data', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind connecting to data
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Connecting to data is a core building block of Data Visualization with Power BI.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Connecting to data" and compare your result with the sample solution.', 18, 0, 2);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (74, 33, N'Relationships and tables', 'VIDEO', N'https://example.com/videos/relationships-and-tables', N'## Objectives

- Understand the key ideas behind relationships and tables
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Relationships and tables is a core building block of Data Visualization with Power BI.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Relationships and tables" and compare your result with the sample solution.', 22, 0, 3);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (75, 33, N'Intro to DAX', 'FILE', N'https://example.com/files/intro-to-dax.pdf', N'## Objectives

- Understand the key ideas behind intro to dax
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Intro to DAX is a core building block of Data Visualization with Power BI.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Intro to DAX" and compare your result with the sample solution.', 28, 0, 4);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (76, 34, N'Visual best practices', 'VIDEO', N'https://example.com/videos/visual-best-practices', N'## Objectives

- Understand the key ideas behind visual best practices
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Visual best practices is a core building block of Data Visualization with Power BI.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Visual best practices" and compare your result with the sample solution.', 20, 0, 5);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (77, 34, N'Publishing reports', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind publishing reports
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Publishing reports is a core building block of Data Visualization with Power BI.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Publishing reports" and compare your result with the sample solution.', 16, 0, 6);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (78, 35, N'Colour and typography', 'VIDEO', N'https://example.com/videos/colour-and-typography', N'## Objectives

- Understand the key ideas behind colour and typography
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Colour and typography is a core building block of Graphic Design with Photoshop & Illustrator.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Colour and typography" and compare your result with the sample solution.', 18, 1, 1);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (79, 35, N'Composition', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind composition
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Composition is a core building block of Graphic Design with Photoshop & Illustrator.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Composition" and compare your result with the sample solution.', 16, 0, 2);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (80, 36, N'Layers and masks', 'VIDEO', N'https://example.com/videos/layers-and-masks', N'## Objectives

- Understand the key ideas behind layers and masks
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Layers and masks is a core building block of Graphic Design with Photoshop & Illustrator.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Layers and masks" and compare your result with the sample solution.', 26, 0, 3);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (81, 36, N'Photo retouching', 'FILE', N'https://example.com/files/photo-retouching.pdf', N'## Objectives

- Understand the key ideas behind photo retouching
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Photo retouching is a core building block of Graphic Design with Photoshop & Illustrator.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Photo retouching" and compare your result with the sample solution.', 24, 0, 4);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (82, 37, N'Vector shapes', 'VIDEO', N'https://example.com/videos/vector-shapes', N'## Objectives

- Understand the key ideas behind vector shapes
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Vector shapes is a core building block of Graphic Design with Photoshop & Illustrator.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Vector shapes" and compare your result with the sample solution.', 22, 0, 5);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (83, 37, N'Designing a logo', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind designing a logo
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Designing a logo is a core building block of Graphic Design with Photoshop & Illustrator.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Designing a logo" and compare your result with the sample solution.', 28, 0, 6);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (84, 38, N'What a project is', 'VIDEO', N'https://example.com/videos/what-a-project-is', N'## Objectives

- Understand the key ideas behind what a project is
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- What a project is is a core building block of Project Management Fundamentals.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "What a project is" and compare your result with the sample solution.', 12, 1, 1);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (85, 38, N'Defining scope', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind defining scope
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Defining scope is a core building block of Project Management Fundamentals.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Defining scope" and compare your result with the sample solution.', 18, 0, 2);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (86, 39, N'Schedules and budgets', 'VIDEO', N'https://example.com/videos/schedules-and-budgets', N'## Objectives

- Understand the key ideas behind schedules and budgets
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Schedules and budgets is a core building block of Project Management Fundamentals.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Schedules and budgets" and compare your result with the sample solution.', 24, 0, 3);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (87, 39, N'Risks and stakeholders', 'FILE', N'https://example.com/files/risks-and-stakeholders.pdf', N'## Objectives

- Understand the key ideas behind risks and stakeholders
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Risks and stakeholders is a core building block of Project Management Fundamentals.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Risks and stakeholders" and compare your result with the sample solution.', 20, 0, 4);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (88, 40, N'Agile and Kanban', 'VIDEO', N'https://example.com/videos/agile-and-kanban', N'## Objectives

- Understand the key ideas behind agile and kanban
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Agile and Kanban is a core building block of Project Management Fundamentals.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Agile and Kanban" and compare your result with the sample solution.', 22, 0, 5);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (89, 40, N'Reporting progress', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind reporting progress
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Reporting progress is a core building block of Project Management Fundamentals.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Reporting progress" and compare your result with the sample solution.', 14, 0, 6);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (90, 41, N'Reading strategies', 'VIDEO', N'https://example.com/videos/reading-strategies', N'## Objectives

- Understand the key ideas behind reading strategies
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Reading strategies is a core building block of IELTS Preparation (Academic).
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Reading strategies" and compare your result with the sample solution.', 28, 1, 1);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (91, 41, N'Listening practice', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind listening practice
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Listening practice is a core building block of IELTS Preparation (Academic).
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Listening practice" and compare your result with the sample solution.', 26, 0, 2);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (92, 42, N'Task 1 reports', 'VIDEO', N'https://example.com/videos/task-1-reports', N'## Objectives

- Understand the key ideas behind task 1 reports
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Task 1 reports is a core building block of IELTS Preparation (Academic).
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Task 1 reports" and compare your result with the sample solution.', 30, 0, 3);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (93, 42, N'Task 2 essays', 'FILE', N'https://example.com/files/task-2-essays.pdf', N'## Objectives

- Understand the key ideas behind task 2 essays
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Task 2 essays is a core building block of IELTS Preparation (Academic).
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Task 2 essays" and compare your result with the sample solution.', 32, 0, 4);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (94, 43, N'Parts 1 to 3', 'VIDEO', N'https://example.com/videos/parts-1-to-3', N'## Objectives

- Understand the key ideas behind parts 1 to 3
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Parts 1 to 3 is a core building block of IELTS Preparation (Academic).
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Parts 1 to 3" and compare your result with the sample solution.', 28, 0, 5);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (95, 43, N'Mock test and feedback', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind mock test and feedback
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Mock test and feedback is a core building block of IELTS Preparation (Academic).
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Mock test and feedback" and compare your result with the sample solution.', 35, 0, 6);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (96, 44, N'How search works', 'VIDEO', N'https://example.com/videos/how-search-works', N'## Objectives

- Understand the key ideas behind how search works
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- How search works is a core building block of SEO & Content Marketing.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "How search works" and compare your result with the sample solution.', 14, 1, 1);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (97, 44, N'Keyword research', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind keyword research
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Keyword research is a core building block of SEO & Content Marketing.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Keyword research" and compare your result with the sample solution.', 22, 0, 2);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (98, 45, N'Page optimisation', 'VIDEO', N'https://example.com/videos/page-optimisation', N'## Objectives

- Understand the key ideas behind page optimisation
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Page optimisation is a core building block of SEO & Content Marketing.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Page optimisation" and compare your result with the sample solution.', 24, 0, 3);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (99, 45, N'Technical SEO basics', 'FILE', N'https://example.com/files/technical-seo-basics.pdf', N'## Objectives

- Understand the key ideas behind technical seo basics
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Technical SEO basics is a core building block of SEO & Content Marketing.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Technical SEO basics" and compare your result with the sample solution.', 20, 0, 4);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (100, 46, N'Content planning', 'VIDEO', N'https://example.com/videos/content-planning', N'## Objectives

- Understand the key ideas behind content planning
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Content planning is a core building block of SEO & Content Marketing.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Content planning" and compare your result with the sample solution.', 18, 0, 5);
INSERT INTO Lessons (LessonId, SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (101, 46, N'Measuring results', 'TEXT', NULL, N'## Objectives

- Understand the key ideas behind measuring results
- Apply them in a short hands-on exercise
- Recognise common mistakes and best practices

## Key notes

- Measuring results is a core building block of SEO & Content Marketing.
- Work through the examples step by step before moving on.

## Practice

Complete the short exercise for "Measuring results" and compare your result with the sample solution.', 16, 0, 6);
SET IDENTITY_INSERT Lessons OFF;

SET IDENTITY_INSERT Quizzes ON;
INSERT INTO Quizzes (QuizId, SectionId, Title, PassScore, MaxAttempts) VALUES (1, 1, N'Java refresher quiz', 70, 0);
INSERT INTO Quizzes (QuizId, SectionId, Title, PassScore, MaxAttempts) VALUES (2, 5, N'HTML basics quiz', 70, 0);
INSERT INTO Quizzes (QuizId, SectionId, Title, PassScore, MaxAttempts) VALUES (3, 8, N'Containers quiz', 70, 0);
INSERT INTO Quizzes (QuizId, SectionId, Title, PassScore, MaxAttempts) VALUES (4, 11, N'Foundations quiz', 70, 0);
INSERT INTO Quizzes (QuizId, SectionId, Title, PassScore, MaxAttempts) VALUES (5, 14, N'SQL basics quiz', 70, 0);
INSERT INTO Quizzes (QuizId, SectionId, Title, PassScore, MaxAttempts) VALUES (6, 17, N'Research quiz', 70, 0);
INSERT INTO Quizzes (QuizId, SectionId, Title, PassScore, MaxAttempts) VALUES (7, 20, N'Strategy quiz', 70, 0);
INSERT INTO Quizzes (QuizId, SectionId, Title, PassScore, MaxAttempts) VALUES (8, 23, N'Everyday work quiz', 70, 0);
INSERT INTO Quizzes (QuizId, SectionId, Title, PassScore, MaxAttempts) VALUES (9, 26, N'Foundations quiz', 70, 0);
INSERT INTO Quizzes (QuizId, SectionId, Title, PassScore, MaxAttempts) VALUES (10, 29, N'React basics quiz', 70, 0);
INSERT INTO Quizzes (QuizId, SectionId, Title, PassScore, MaxAttempts) VALUES (11, 32, N'Getting started quiz', 70, 0);
INSERT INTO Quizzes (QuizId, SectionId, Title, PassScore, MaxAttempts) VALUES (12, 35, N'Design basics quiz', 70, 0);
INSERT INTO Quizzes (QuizId, SectionId, Title, PassScore, MaxAttempts) VALUES (13, 38, N'Foundations quiz', 70, 0);
INSERT INTO Quizzes (QuizId, SectionId, Title, PassScore, MaxAttempts) VALUES (14, 41, N'Reading and listening quiz', 70, 0);
INSERT INTO Quizzes (QuizId, SectionId, Title, PassScore, MaxAttempts) VALUES (15, 44, N'SEO basics quiz', 70, 0);
SET IDENTITY_INSERT Quizzes OFF;

SET IDENTITY_INSERT Questions ON;
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (1, 1, N'Which lesson is part of "Java refresher"?', 1);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (2, 1, N'What is the best way to learn "Exceptions and testing"?', 2);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (3, 1, N'How is your course progress calculated?', 3);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (4, 2, N'Which lesson is part of "HTML basics"?', 1);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (5, 2, N'What is the best way to learn "Forms and tables"?', 2);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (6, 2, N'How is your course progress calculated?', 3);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (7, 3, N'Which lesson is part of "Containers"?', 1);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (8, 3, N'What is the best way to learn "Docker Compose"?', 2);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (9, 3, N'How is your course progress calculated?', 3);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (10, 4, N'Which lesson is part of "Foundations"?', 1);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (11, 4, N'What is the best way to learn "Exploratory analysis"?', 2);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (12, 4, N'How is your course progress calculated?', 3);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (13, 5, N'Which lesson is part of "SQL basics"?', 1);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (14, 5, N'What is the best way to learn "Aggregations"?', 2);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (15, 5, N'How is your course progress calculated?', 3);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (16, 6, N'Which lesson is part of "Research"?', 1);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (17, 6, N'What is the best way to learn "Personas and journeys"?', 2);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (18, 6, N'How is your course progress calculated?', 3);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (19, 7, N'Which lesson is part of "Strategy"?', 1);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (20, 7, N'What is the best way to learn "Audience research"?', 2);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (21, 7, N'How is your course progress calculated?', 3);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (22, 8, N'Which lesson is part of "Everyday work"?', 1);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (23, 8, N'What is the best way to learn "Professional emails"?', 2);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (24, 8, N'How is your course progress calculated?', 3);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (25, 9, N'Which lesson is part of "Foundations"?', 1);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (26, 9, N'What is the best way to learn "Structuring your message"?', 2);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (27, 9, N'How is your course progress calculated?', 3);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (28, 10, N'Which lesson is part of "React basics"?', 1);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (29, 10, N'What is the best way to learn "Handling events"?', 2);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (30, 10, N'How is your course progress calculated?', 3);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (31, 11, N'Which lesson is part of "Getting started"?', 1);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (32, 11, N'What is the best way to learn "Connecting to data"?', 2);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (33, 11, N'How is your course progress calculated?', 3);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (34, 12, N'Which lesson is part of "Design basics"?', 1);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (35, 12, N'What is the best way to learn "Composition"?', 2);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (36, 12, N'How is your course progress calculated?', 3);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (37, 13, N'Which lesson is part of "Foundations"?', 1);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (38, 13, N'What is the best way to learn "Defining scope"?', 2);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (39, 13, N'How is your course progress calculated?', 3);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (40, 14, N'Which lesson is part of "Reading and listening"?', 1);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (41, 14, N'What is the best way to learn "Listening practice"?', 2);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (42, 14, N'How is your course progress calculated?', 3);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (43, 15, N'Which lesson is part of "SEO basics"?', 1);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (44, 15, N'What is the best way to learn "Keyword research"?', 2);
INSERT INTO Questions (QuestionId, QuizId, QuestionText, Position) VALUES (45, 15, N'How is your course progress calculated?', 3);
SET IDENTITY_INSERT Questions OFF;

SET IDENTITY_INSERT Options ON;
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (1, 1, N'JPA with SQL Server', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (2, 1, N'Controllers and services', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (3, 1, N'OOP essentials', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (4, 1, N'Swagger documentation', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (5, 2, N'Watch, then practise the example yourself', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (6, 2, N'Skip the exercises', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (7, 2, N'Only read the title', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (8, 2, N'Memorise without trying', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (9, 3, N'By time spent on the site', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (10, 3, N'By number of logins', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (11, 3, N'Completed lessons divided by total lessons', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (12, 3, N'By course price', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (13, 4, N'Deploy the app', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (14, 4, N'Page structure', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (15, 4, N'Fetching data', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (16, 4, N'JPA with SQL Server', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (17, 5, N'Watch, then practise the example yourself', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (18, 5, N'Skip the exercises', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (19, 5, N'Only read the title', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (20, 5, N'Memorise without trying', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (21, 6, N'By time spent on the site', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (22, 6, N'Completed lessons divided by total lessons', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (23, 6, N'By number of logins', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (24, 6, N'By course price', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (25, 7, N'Validation and error handling', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (26, 7, N'Images and containers', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (27, 7, N'JPA with SQL Server', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (28, 7, N'Page structure', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (29, 8, N'Watch, then practise the example yourself', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (30, 8, N'Skip the exercises', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (31, 8, N'Memorise without trying', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (32, 8, N'Only read the title', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (33, 9, N'Completed lessons divided by total lessons', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (34, 9, N'By number of logins', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (35, 9, N'By course price', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (36, 9, N'By time spent on the site', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (37, 10, N'Build and test automation', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (38, 10, N'OOP essentials', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (39, 10, N'What is machine learning', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (40, 10, N'JPA with SQL Server', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (41, 11, N'Only read the title', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (42, 11, N'Memorise without trying', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (43, 11, N'Watch, then practise the example yourself', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (44, 11, N'Skip the exercises', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (45, 12, N'By course price', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (46, 12, N'Completed lessons divided by total lessons', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (47, 12, N'By number of logins', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (48, 12, N'By time spent on the site', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (49, 13, N'SELECT and filtering', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (50, 13, N'Trees and ensembles', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (51, 13, N'Collections and streams', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (52, 13, N'OOP essentials', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (53, 14, N'Only read the title', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (54, 14, N'Watch, then practise the example yourself', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (55, 14, N'Skip the exercises', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (56, 14, N'Memorise without trying', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (57, 15, N'By course price', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (58, 15, N'By number of logins', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (59, 15, N'By time spent on the site', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (60, 15, N'Completed lessons divided by total lessons', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (61, 16, N'Understanding users', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (62, 16, N'Deploy the app', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (63, 16, N'JPA with SQL Server', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (64, 16, N'Controllers and services', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (65, 17, N'Skip the exercises', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (66, 17, N'Only read the title', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (67, 17, N'Memorise without trying', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (68, 17, N'Watch, then practise the example yourself', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (69, 18, N'By time spent on the site', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (70, 18, N'By number of logins', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (71, 18, N'Completed lessons divided by total lessons', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (72, 18, N'By course price', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (73, 19, N'Funnels and goals', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (74, 19, N'Classification', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (75, 19, N'Data preparation', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (76, 19, N'Exploratory analysis', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (77, 20, N'Watch, then practise the example yourself', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (78, 20, N'Memorise without trying', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (79, 20, N'Only read the title', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (80, 20, N'Skip the exercises', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (81, 21, N'By number of logins', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (82, 21, N'By course price', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (83, 21, N'By time spent on the site', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (84, 21, N'Completed lessons divided by total lessons', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (85, 22, N'Meetings and discussions', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (86, 22, N'Prototyping', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (87, 22, N'Social media ads', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (88, 22, N'Analytics and reporting', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (89, 23, N'Only read the title', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (90, 23, N'Watch, then practise the example yourself', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (91, 23, N'Skip the exercises', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (92, 23, N'Memorise without trying', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (93, 24, N'Completed lessons divided by total lessons', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (94, 24, N'By time spent on the site', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (95, 24, N'By number of logins', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (96, 24, N'By course price', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (97, 25, N'Fetching data', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (98, 25, N'Authentication with JWT', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (99, 25, N'Know your audience', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (100, 25, N'Connect the front-end', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (101, 26, N'Skip the exercises', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (102, 26, N'Watch, then practise the example yourself', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (103, 26, N'Only read the title', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (104, 26, N'Memorise without trying', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (105, 27, N'By time spent on the site', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (106, 27, N'By number of logins', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (107, 27, N'By course price', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (108, 27, N'Completed lessons divided by total lessons', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (109, 28, N'JPA with SQL Server', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (110, 28, N'Visual design basics', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (111, 28, N'JSX and components', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (112, 28, N'Design systems', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (113, 29, N'Watch, then practise the example yourself', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (114, 29, N'Memorise without trying', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (115, 29, N'Skip the exercises', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (116, 29, N'Only read the title', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (117, 30, N'By time spent on the site', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (118, 30, N'By course price', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (119, 30, N'By number of logins', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (120, 30, N'Completed lessons divided by total lessons', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (121, 31, N'Aggregations', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (122, 31, N'Search and SEO', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (123, 31, N'Power BI overview', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (124, 31, N'Build a mini dashboard', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (125, 32, N'Skip the exercises', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (126, 32, N'Only read the title', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (127, 32, N'Memorise without trying', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (128, 32, N'Watch, then practise the example yourself', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (129, 33, N'By number of logins', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (130, 33, N'By course price', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (131, 33, N'Completed lessons divided by total lessons', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (132, 33, N'By time spent on the site', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (133, 34, N'Colour and typography', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (134, 34, N'SELECT and filtering', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (135, 34, N'Usability testing', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (136, 34, N'Phone and video calls', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (137, 35, N'Watch, then practise the example yourself', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (138, 35, N'Skip the exercises', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (139, 35, N'Memorise without trying', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (140, 35, N'Only read the title', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (141, 36, N'By time spent on the site', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (142, 36, N'Completed lessons divided by total lessons', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (143, 36, N'By number of logins', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (144, 36, N'By course price', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (145, 37, N'Handling Q&A', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (146, 37, N'Swagger documentation', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (147, 37, N'What a project is', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (148, 37, N'Phone and video calls', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (149, 38, N'Only read the title', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (150, 38, N'Memorise without trying', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (151, 38, N'Skip the exercises', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (152, 38, N'Watch, then practise the example yourself', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (153, 39, N'Completed lessons divided by total lessons', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (154, 39, N'By time spent on the site', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (155, 39, N'By course price', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (156, 39, N'By number of logins', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (157, 40, N'Building a dashboard', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (158, 40, N'Reading strategies', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (159, 40, N'Parts 1 to 3', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (160, 40, N'DOM and events', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (161, 41, N'Memorise without trying', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (162, 41, N'Only read the title', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (163, 41, N'Skip the exercises', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (164, 41, N'Watch, then practise the example yourself', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (165, 42, N'Completed lessons divided by total lessons', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (166, 42, N'By course price', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (167, 42, N'By number of logins', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (168, 42, N'By time spent on the site', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (169, 43, N'How search works', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (170, 43, N'Collections and streams', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (171, 43, N'Schedules and budgets', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (172, 43, N'Classification', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (173, 44, N'Skip the exercises', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (174, 44, N'Only read the title', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (175, 44, N'Memorise without trying', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (176, 44, N'Watch, then practise the example yourself', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (177, 45, N'Completed lessons divided by total lessons', 1);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (178, 45, N'By number of logins', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (179, 45, N'By time spent on the site', 0);
INSERT INTO Options (OptionId, QuestionId, OptionText, IsCorrect) VALUES (180, 45, N'By course price', 0);
SET IDENTITY_INSERT Options OFF;

COMMIT;
GO
