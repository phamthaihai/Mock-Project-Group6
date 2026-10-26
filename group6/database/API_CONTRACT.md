# New / changed REST endpoints (what the front-end now calls)

Front-end mock lives in `static/js/common.js` (`mock()`); set `CONFIG.USE_MOCK=false` to hit these instead.
Lesson key = `<sectionOrder>.<lessonOrder>` (e.g. `2.3`). Keep this key in the API, map it to `lessonId` server-side.

| Method | Path | Notes |
|---|---|---|
| GET | `/courses/{id}` | Public outline: sections -> `lessons[{title,minutes,type}]` and `quizzes[{quizId,title,description,passPercent,timeLimitMin,questionCount}]`. NO lesson content, NO questions. |
| GET | `/courses/{id}/lessons/{s}.{l}` | Enrolled users or ADMIN. Returns `{key, section, title, type, minutes, videoUrl, objectives[], blocks[], task, resources[]}` (from `Lessons.content` JSON). |
| GET | `/courses/{id}/progress` | `{enrolled, completed:["1.1",...], quizzes:{"2011":{attempts,best,passed}}}` |
| PUT | `/courses/{id}/progress` | body `{lesson:"1.1", done:true}` -> `{courseCompleted:boolean}` |
| GET | `/quizzes/{quizId}` | Enrolled + all lessons of that section completed (else 403) or ADMIN. Returns questions WITHOUT `isCorrect`. Adds `courseId, courseTitle, sectionIndex, sectionTitle, attempts, best, passed`. |
| POST | `/quizzes/{quizId}/submit` | body `{answers:{"201101":[2011011], ...}}` (questionId -> chosen optionIds). Grade on the SERVER. A question is correct only if the chosen set equals the correct set. Returns `{attemptId, scorePercent, correctCount, totalCount, passed, passPercent, courseCompleted, review:[{questionId, correct, selected[], correctOptionIds[], explanation}]}`; insert `QuizAttempts` + `QuizAttemptAnswers`. |
| GET | `/enrollments/me` | now also returns `totalQuizzes`, `passedQuizzes`. `completedAt` is set only when ALL lessons are done AND ALL quizzes passed. |

Course completion rule: every lesson in `LessonProgress` **and** at least one passing `QuizAttempts` row for every quiz of the course.
