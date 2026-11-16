# SkillTrail API contract (what the front-end calls)

Base URL `/api`. Auth: `Authorization: Bearer <jwt>`. Errors are JSON `{"message":"..."}`. Roles: STUDENT, ADMIN. A course's `instructor` is a course author profile (`Users.IsAuthor`), not a role; author profiles do not log in.
The front-end mock (`js/common.js`, function `mock`) returns exactly these shapes, so the real API should match.

## Auth and profile
| Method | URL | Who | Notes |
|---|---|---|---|
| POST | /auth/register | public | `{fullName,email,password}` -> new STUDENT. 400 invalid, 409 email exists |
| POST | /auth/login | public | `{email,password}` -> `{token, user:{userId,fullName,email,role}}`. 401 wrong, 403 locked |
| GET / PUT | /users/me | any | `{userId,fullName,email,role,locked}` / body `{fullName}` |
| PUT | /users/me/password | any | `{currentPassword,newPassword}` 400 if wrong. If the account has no password yet (Google sign-up), `currentPassword` is not required; `GET /users/me` should return `hasPassword` (boolean) |
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

## Course management (ADMIN only)
| Method | URL | Notes |
|---|---|---|
| GET | /instructor/courses | all courses, any status |
| GET | /instructor/courses/{id} | full course incl. lesson content and quiz questions with `correct` flags |
| PUT / DELETE | /courses/{id} | `{title,categoryId,level,price,imageUrl,description,details}`. 400 empty title / negative price, 403 not admin, DELETE 409 if students enrolled (create goes through `POST /admin/courses`) |
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

## Admin console (admin.html, ADMIN only)
| Method | URL | Notes |
|---|---|---|
| POST | /admin/courses | `{title,categoryId,level,price,imageUrl,description,status,instructorId,rejectReason}` -> Course. `status` DRAFT/PENDING/PUBLISHED/REJECTED, `instructorId` must be an ADMIN or a course author profile, REJECTED needs `rejectReason` |
| PUT | /admin/courses/{id} | same body, edits any course; omitted `details` is kept as is |
| DELETE | /admin/courses/{id} | deletes the course AND its enrollments, progress, reviews and quiz attempts (unlike the instructor DELETE, no 409) |
| GET | /admin/logs/account | newest first, max 500. Entry: `{logId,type,action,userId,userName,email,courseId,courseTitle,amount,method,detail,actor,createdAt}`. Actions: REGISTERED, LOGIN, LOGIN_FAILED, LOGIN_BLOCKED, PASSWORD_CHANGED, ROLE_CHANGED, LOCKED, UNLOCKED |
| GET | /admin/logs/payment | action PAID; `amount` + `method` filled (only when price paid > 0) |
| GET | /admin/logs/enrollment | actions ENROLLED, UNENROLLED |
| GET | /admin/logs/completion | action COMPLETED when progress goes from below 100% to 100% (all lessons done and all quizzes passed); `detail` e.g. "11 lessons, 1/1 quizzes" |

Sections and lessons inside a course reuse the existing `/courses/{id}/sections`, `/sections/{id}/lessons` and `DELETE` endpoints, which already accept ADMIN.
The server writes one `ActivityLogs` row (see schema.sql) at register, login (also failed or blocked), password change, role/lock change, enroll, unenroll, payment and course completion.
Admin account: `admin@gmail.com` / `123456` (inserted by schema.sql). The mock also has `admin@skilltrail.com` / `admin123`.

## Help & Support (support.html #support)
| Method | URL | Who | Notes |
|---|---|---|---|
| POST | /support | public (guests too) | `{name,email,category,courseId?,subject,message}` -> `{ticketId}`. `category` PAYMENT/COURSE/ACCOUNT/OTHER. When logged in, name and email come from the account. 400 invalid (subject 3+ chars, message 10+ chars) |
| GET | /support/me | any | own tickets, newest first: `{ticketId,category,courseId,courseTitle,subject,message,status,reply,repliedAt,createdAt}` |
| GET | /admin/tickets | ADMIN | all tickets, newest first (same shape plus `userId,name,email`; `userId` null = guest) |
| PUT | /admin/tickets/{id} | ADMIN | `{reply?,status}` status OPEN/RESOLVED. A non-empty `reply` is saved and the student is notified |

New ticket -> every ADMIN gets a notification. `GET /admin/stats` also returns `openTickets`.

## Notifications created by the server
Admin approves/rejects a course -> the course's instructor profile. A student enrolls -> the course's instructor profile. A course is submitted -> every ADMIN. A student reaches 100% -> that student.

## Shopping cart (cart.html, any logged-in user except ADMIN)
| Method | URL | Notes |
|---|---|---|
| GET | /cart | `{items:[Course + addedAt], count, total}` for the current user |
| POST | /cart/{courseId} | add. 404 unpublished or missing, 400 own course (or ADMIN), 409 already enrolled / already in cart |
| DELETE | /cart/{courseId} | remove one. 404 if it is not in the cart |
| DELETE | /cart | empty the cart |
| POST | /cart/checkout | `{paymentMethod}` (CARD/EWALLET/BANK/DEMO, required when the cart has a paid course) -> `{orderNumber,paymentMethod,items:[{enrollmentId,courseId,title,price}],skipped:[{courseId,title}],total}`. Enrolls in every course, writes the same ENROLLED / PAID logs and instructor notifications as `POST /courses/{id}/enroll`, then empties the cart. Courses that became unavailable (unpublished, now owned, already enrolled) are returned in `skipped`. 400 empty cart or missing method |

The navbar cart badge calls `GET /cart`; if the endpoint is missing the icon hides itself.

## Account extras (implemented by the Spring Boot API)
| Method | URL | Who | Notes |
|---|---|---|---|
| POST | /auth/register | public | `{fullName,email,password}` -> 201 `{userId,fullName,email,role,verificationRequired:true,emailSent}`. The account is saved with `Verified = 0` and a 6-digit code is emailed (BCrypt hash in `EmailVerifications`, 10 minutes, 5 tries, 60 s between sends). 400 invalid (same rules as `validateSignup` in common.js), 409 email already registered and verified |
| POST | /auth/verify-email | public | `{email,code}` -> 204. 400 "Verification code is invalid or expired" |
| POST | /auth/resend-code | public | `{email}` -> `{message}` (same answer whether or not the email exists). 429 when asked again within 60 s |
| POST | /auth/login | public | `{email,password}` -> `{token,user}`. 401 wrong email or password, 403 locked, 403 "Please verify your email first" |
| POST | /auth/google | public | `{credential}` (Google ID token) -> `{token,created,user}`. The token is checked with Google (tokeninfo) and its audience must equal `skilltrail.google.client-id`. A new account is created when the email is unknown; an existing account is linked and marked verified. 403 locked or unbound |
| POST | /auth/forgot-password | public | `{email}` -> `{message}` (never says whether the email exists). A 6-digit code is emailed (10 minutes, 5 tries, one per 60 s) |
| POST | /auth/reset-password | public | `{email,code,newPassword}` -> 204. 400 invalid or expired code, 400 password shorter than 6 |
| PUT | /users/me/password | any | `{currentPassword,newPassword}` -> 204. A Google-only account (no password) may set one without `currentPassword` |
| POST | /users/me/google/unbind | any | `{newPassword?,force?}` -> 204. A Google-only account must send `newPassword` (6+ chars) or `force:true` |
| GET/PUT | /users/me | any | `{userId,fullName,email,role,locked,author,avatar,hasPassword,googleLinked}`; PUT takes `{fullName,avatar?}` |

Tokens are HS256 JWTs (`skilltrail.jwt.secret`, 7 days). The user is reloaded from the database on every request, so a lock or role change applies immediately.
Run order for the database: `schema.sql`, `seed-courses.sql`, `email-verification.sql`, `backend-api.sql` (adds `Users.Avatar`, `Users.GoogleLinked` and `PasswordResets`).

