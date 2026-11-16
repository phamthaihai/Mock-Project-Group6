-- Sets the course pictures back to the ones used by the mock data (same links as seed-courses.sql).
-- Run in SSMS on the SkillTrail database. Safe to run more than once.
USE SkillTrail;
GO
UPDATE Courses SET ImageUrl = N'https://loremflickr.com/1200/700/coding,programming?lock=21' WHERE CourseId = 1;  -- Java Full-Stack Bootcamp
UPDATE Courses SET ImageUrl = N'https://loremflickr.com/1200/700/laptop,webdesign?lock=22' WHERE CourseId = 2;  -- Intro to Web Development
UPDATE Courses SET ImageUrl = N'https://loremflickr.com/1200/700/server,datacenter?lock=23' WHERE CourseId = 3;  -- Docker & DevOps Essentials
UPDATE Courses SET ImageUrl = N'https://loremflickr.com/1200/700/artificialintelligence,technology?lock=24' WHERE CourseId = 4;  -- Machine Learning Foundations
UPDATE Courses SET ImageUrl = N'https://loremflickr.com/1200/700/data,analytics?lock=25' WHERE CourseId = 5;  -- SQL & Data Analysis
UPDATE Courses SET ImageUrl = N'https://loremflickr.com/1200/700/design,interface?lock=26' WHERE CourseId = 6;  -- UI/UX Design Essentials
UPDATE Courses SET ImageUrl = N'https://loremflickr.com/1200/700/marketing,socialmedia?lock=27' WHERE CourseId = 7;  -- Digital Marketing Masterclass
UPDATE Courses SET ImageUrl = N'https://loremflickr.com/1200/700/meeting,office?lock=28' WHERE CourseId = 8;  -- Business English for Professionals
UPDATE Courses SET ImageUrl = N'https://loremflickr.com/1200/700/speaker,presentation?lock=29' WHERE CourseId = 9;  -- Public Speaking & Presentation Skills
UPDATE Courses SET ImageUrl = N'https://loremflickr.com/1200/700/code,screen?lock=30' WHERE CourseId = 10;  -- React for Modern Web Apps
UPDATE Courses SET ImageUrl = N'https://loremflickr.com/1200/700/dashboard,analytics?lock=31' WHERE CourseId = 11;  -- Data Visualization with Power BI
UPDATE Courses SET ImageUrl = N'https://loremflickr.com/1200/700/illustration,designer?lock=32' WHERE CourseId = 12;  -- Graphic Design with Photoshop & Illustrator
UPDATE Courses SET ImageUrl = N'https://loremflickr.com/1200/700/teamwork,planning?lock=33' WHERE CourseId = 13;  -- Project Management Fundamentals
UPDATE Courses SET ImageUrl = N'https://loremflickr.com/1200/700/studying,exam?lock=34' WHERE CourseId = 14;  -- IELTS Preparation (Academic)
UPDATE Courses SET ImageUrl = N'https://loremflickr.com/1200/700/laptop,marketing?lock=35' WHERE CourseId = 15;  -- SEO & Content Marketing
GO
SELECT CourseId, Title, ImageUrl FROM Courses ORDER BY CourseId;
