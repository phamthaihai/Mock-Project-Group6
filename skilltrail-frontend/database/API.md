# SkillTrail API contract (what the front-end calls)

Base URL `/api`. Auth: `Authorization: Bearer <jwt>`. Errors are JSON `{"message":"..."}`. Roles: STUDENT, INSTRUCTOR, ADMIN.
The front-end mock (`js/common.js`, function `mock`) returns exactly these shapes, so the real API should match.

## Auth and profile
| Method | URL | Who | Notes |
|---|---|---|---|
| POST | /auth/register | public | `{fullName,email,password}` -> new STUDENT. 400 invalid, 409 email exists |
| POST | /auth/login | public | `{email,password}` -> `{token, user:{userId,fullName,email,role}}`. 401 wrong, 403 locked |
| GET / PUT | /users/me | any | `{userId,fullName,email,role,locked}` / body `{fullName}` |
| PUT | /users/me/password | any | `{currentPassword,newPassword}` 400 if wrong |
| GET / POST | /notifications/me, /notifications/read | any | `[{notificationId,message,link,read,createdAt}]`, POST marks all read |

## Catalog (public)
| Method | URL | Notes |
|---|---|---|
| GET | /categories | `[{categoryId,name}]` |
| GET | /courses | PUBLISHED only. `[Course]` (search, filter, sort run in the browser) |
| GET | /courses/{id} | Course outline. Non-PUBLISHED: only owner or ADMIN, else 404 |
| GET | /courses/{id}/reviews | `[{reviewId,userId,userName,rating,comment,createdAt}]` |
| GET | /courses/{id}/progress | `{enrolled,completed:[lessonId],percent,nextLessonId}` (public: enrolled=false when logged out) |
| GET | /lessons/{id} | full lesson. Preview lessons: anyone. Otherwise enrolled, owner or ADMIN: 401 logged out, 403 not enrolled |

**Course** = `{courseId,title,description,categoryId,category,level,price,imageUrl,status,rejectReason,instructor:{userId,fullName},language,durationHours,details:{tagline,learn[],requirements[],skills[],instructorBio},rating,ratingCount,enrolledCount,lessonCount,sections:[Section]}`
**Section** = `{sectionId,title,position,lessons:[{lessonId,title,type,minutes,preview,position}],quiz:{quizId,title,passScore,maxAttempts,questionCount}|null}` (lesson content is NOT in the outline)
**Lesson** = `{lessonId,courseId,title,type:VIDEO|TEXT|FILE,contentUrl,text,minutes,preview,position}`

## Student
| Method | URL | Notes |
|---|---|---|
| POST | /courses/{id}/enroll | `{paymentMethod}` (CARD/EWALLET/BANK/DEMO, required when price > 0, not for ADMIN). 409 twice, 404 unpublished, 400 own course |
| DELETE | /courses/{id}/enroll | unenroll (removes progress) |
| GET | /enrollments/me | `[{enrollmentId,courseId,title,imageUrl,category,instructor,totalLessons,completedLessons,percent,nextLessonId,enrolledAt}]` |
| PUT | /courses/{id}/progress | `{lessonId,done}` -> progress object. Idempotent. 403 if not enrolled |
| GET | /quizzes/{id} | questions WITHOUT correct flags + `{passScore,maxAttempts,attemptsUsed,bestScore,passed}` |
| POST | /quizzes/{id}/attempts | `{answers:{questionId:optionId}}` -> `{score,passed,passScore,correctCount,total,correct:{questionId:optionId},attemptsUsed}`. 400 unanswered, 409 attempts used up |
| POST / PUT / DELETE | /courses/{id}/reviews, /reviews/{id} | `{rating 1-5,comment}`. 403 not enrolled / not yours, 409 reviewed twice. ADMIN may delete any |

## Instructor (owner) or ADMIN
| Method | URL | Notes |
|---|---|---|
| GET | /instructor/courses | own courses (ADMIN: all), any status |
| GET | /instructor/courses/{id} | full course incl. lesson content and quiz questions with `correct` flags |
| POST / PUT / DELETE | /courses, /courses/{id} | `{title,categoryId,level,price,imageUrl,description,details}`. Create = DRAFT. 400 empty title / negative price, 403 not owner, DELETE 409 if students enrolled |
| POST | /courses/{id}/submit | DRAFT/REJECTED -> PENDING. 400 if no section or no lesson |
| GET | /courses/{id}/students | `[{userId,fullName,email,enrolledAt,completedLessons,totalLessons,percent}]` |
| POST / PUT / DELETE | /courses/{id}/sections, /sections/{id} | `{title}` |
| POST | /sections/{id}/move, /lessons/{id}/move | `{direction:-1|1}` |
| POST / PUT / DELETE | /sections/{id}/lessons, /lessons/{id} | `{title,type,contentUrl,text,minutes,preview}` (video/file need an http(s) link) |
| PUT / DELETE | /sections/{id}/quiz | `{title,passScore,maxAttempts,questions:[{text,options:[{text,correct}]}]}` replaces the quiz. Each question: 2-4 options, exactly 1 correct |

## Admin only (403 otherwise)
| Method | URL | Notes |
|---|---|---|
| GET | /admin/courses | all courses, all statuses |
| POST | /admin/courses/{id}/approve | PENDING -> PUBLISHED, notifies the instructor |
| POST | /admin/courses/{id}/reject | `{reason}` (required) PENDING -> REJECTED, notifies the instructor |
| POST / PUT / DELETE | /categories, /categories/{id} | `{name}`. 409 duplicate, DELETE 409 if courses use it |
| GET | /admin/users | `[{userId,fullName,email,role,locked,enrollments,courses}]` |
| PUT | /admin/users/{id}/role, /admin/users/{id}/lock | `{role}` / `{locked}`. 400 on your own account |
| GET | /admin/reviews, /admin/stats | latest reviews; `{users,courses,published,pending,enrollments,reviews,topCourses[]}` |

## Notifications created by the server
Admin approves/rejects a course -> instructor. A student enrolls -> instructor. A course is submitted -> every ADMIN. A student reaches 100% -> that student.
