/* =====================================================================
   SEED CONTENT for the mock backend (delete together with the mock when the real API is live).
   - LESSON_BODIES : hand-written lessons (rich blocks) keyed by lesson title
   - QUIZ_BANK     : hand-written quizzes keyed by course title -> section index
   - everything else is GENERATED so every course still has lessons + quizzes. Generated text is
     placeholder content: replace it through the admin panel / database when real content exists.

   Lesson block types : text | code | tip | warning | steps | list
   Quiz question      : Q(text, correct, [wrong...], explanation)            single answer
                        QM(text, [correct...], [wrong...], explanation)      "select all that apply"
   ===================================================================== */
const LTYPES = ['Video', 'Reading', 'Video', 'Assignment'];

const Q = (text, correct, wrong, explanation) => ({ text, multi: false, correct: [correct], wrong, explanation });
const QM = (text, correct, wrong, explanation) => ({ text, multi: true, correct, wrong, explanation });
const QZ = (title, description, questions, o = {}) => ({ title, description, questions, passPercent: o.pass || 70, timeLimitMin: o.time ?? null });

const LESSON_BODIES = {
  /* ---------- Intro to Web Development ---------- */
  'Page structure': {
    objectives: ['Name the tags every HTML page needs', 'Build a valid page skeleton', 'Choose semantic tags over generic ones'],
    blocks: [
      { type: 'text', title: 'The skeleton of a page', body: 'Every HTML page has the same skeleton. The doctype tells the browser which version of HTML to use. The html element wraps everything. Inside it, head holds information about the page (title, character set, linked files) and body holds everything the visitor actually sees.' },
      { type: 'code', lang: 'html', body: '<!DOCTYPE html>\n<html lang="en">\n  <head>\n    <meta charset="UTF-8">\n    <title>My first page</title>\n  </head>\n  <body>\n    <header><h1>Hello, web!</h1></header>\n    <main><p>This is my first paragraph.</p></main>\n    <footer>&copy; 2026</footer>\n  </body>\n</html>' },
      { type: 'text', title: 'Semantic tags', body: 'header, main, nav, section, article and footer describe the meaning of a region. Screen readers and search engines use them to understand your page, so prefer them to a pile of div elements.' },
      { type: 'tip', body: 'Indent child tags by two spaces. Well indented HTML makes a missing closing tag easy to spot.' },
      { type: 'warning', body: 'Headings are for structure, not for size. Use one h1 per page and do not jump from h1 to h4 just because it looks smaller.' }
    ],
    task: 'Create index.html with a header, a main area containing a heading and two paragraphs, and a footer. Open it in your browser.',
    resources: [{ name: 'HTML skeleton cheat sheet', type: 'PDF' }, { name: 'Starter files', type: 'ZIP' }]
  },
  'Forms and tables': {
    objectives: ['Build a form with labelled inputs', 'Pick the right input type', 'Mark up data with a table'],
    blocks: [
      { type: 'text', title: 'Collecting input', body: 'A form groups controls that send data somewhere. Every input needs a label, linked with the for attribute, so people using assistive technology know what to type.' },
      { type: 'code', lang: 'html', body: '<form action="/subscribe" method="post">\n  <label for="email">Email</label>\n  <input id="email" name="email" type="email" required>\n\n  <label for="level">Level</label>\n  <select id="level" name="level">\n    <option>Beginner</option>\n    <option>Intermediate</option>\n  </select>\n\n  <button type="submit">Subscribe</button>\n</form>' },
      { type: 'text', title: 'Tables are for data', body: 'Use table, tr (row), th (header cell) and td (data cell) for information that really is a grid. Never use a table just to position things on the page.' },
      { type: 'code', lang: 'html', body: '<table>\n  <thead><tr><th>Course</th><th>Hours</th></tr></thead>\n  <tbody>\n    <tr><td>Web basics</td><td>12</td></tr>\n    <tr><td>SQL</td><td>18</td></tr>\n  </tbody>\n</table>' },
      { type: 'tip', body: 'type="email", type="number" and type="date" give free validation and the right keyboard on phones.' }
    ],
    task: 'Build a sign-up form with name, email, a level dropdown and a submit button. Add a table that lists three courses and their hours.',
    resources: [{ name: 'Input types reference', type: 'PDF' }]
  },
  'Selectors and layout': {
    objectives: ['Target elements with class and id selectors', 'Explain the box model', 'Line items up with flexbox'],
    blocks: [
      { type: 'text', title: 'Selecting what to style', body: 'A CSS rule has a selector (which elements) and declarations (what to change). Element selectors match a tag, class selectors start with a dot and match any element carrying that class, and id selectors start with # and must be unique.' },
      { type: 'code', lang: 'css', body: '.card {\n  padding: 16px;\n  border: 1px solid #ddd;\n  border-radius: 12px;\n}\n\n.card h2 { margin-top: 0; }\n#hero   { background: #1a1040; color: white; }' },
      { type: 'text', title: 'The box model', body: 'Every element is a box: content, then padding, then border, then margin. Add box-sizing: border-box so that width includes padding and border. It makes sizes predictable.' },
      { type: 'code', lang: 'css', body: '.row {\n  display: flex;\n  gap: 12px;\n  justify-content: space-between;\n  align-items: center;\n}' },
      { type: 'warning', body: 'Overusing id selectors makes CSS hard to override. Prefer classes for styling and keep ids for anchors and JavaScript hooks.' }
    ],
    task: 'Style three cards so they sit in one row with a 12px gap, rounded corners and padding. Centre their text.',
    resources: [{ name: 'Flexbox cheat sheet', type: 'PDF' }, { name: 'Exercise files', type: 'ZIP' }]
  },
  'Responsive design': {
    objectives: ['Add the viewport meta tag', 'Write a media query', 'Use the Bootstrap grid'],
    blocks: [
      { type: 'text', title: 'One page, every screen', body: 'Responsive design means the same page adapts to phones, tablets and desktops. It starts with the viewport meta tag, then flexible widths, then media queries that change the layout at chosen breakpoints.' },
      { type: 'code', lang: 'html', body: '<meta name="viewport" content="width=device-width, initial-scale=1">' },
      { type: 'code', lang: 'css', body: '.cards { display: grid; grid-template-columns: 1fr; gap: 16px; }\n\n@media (min-width: 768px) {\n  .cards { grid-template-columns: repeat(3, 1fr); }\n}' },
      { type: 'text', title: 'Bootstrap does the heavy lifting', body: 'Bootstrap\'s 12-column grid lets you write col-12 col-md-4 and get one column on phones and three on tablets and up, without writing a media query yourself.' },
      { type: 'tip', body: 'Design for the smallest screen first (mobile first), then add rules for larger screens with min-width queries.' }
    ],
    task: 'Make your card layout one column on phones, two on tablets and three on desktop using the Bootstrap grid.',
    resources: [{ name: 'Breakpoints table', type: 'PDF' }]
  },
  'DOM and events': {
    objectives: ['Select elements with querySelector', 'Change text and classes from JavaScript', 'React to a click'],
    blocks: [
      { type: 'text', title: 'The DOM', body: 'The browser turns your HTML into a tree of objects called the DOM. JavaScript can find a node, change it, add new nodes, or react when the visitor does something (an event).' },
      { type: 'code', lang: 'javascript', body: 'const btn = document.querySelector("#like");\nconst out = document.querySelector("#count");\nlet n = 0;\n\nbtn.addEventListener("click", () => {\n  n++;\n  out.textContent = n;\n  btn.classList.toggle("active");\n});' },
      { type: 'steps', items: ['Find the element with querySelector', 'Attach a listener with addEventListener', 'Change textContent or classList inside the listener', 'Test in the browser console'] },
      { type: 'warning', body: 'Use textContent, not innerHTML, when inserting text typed by a user. innerHTML can run malicious scripts (XSS).' }
    ],
    task: 'Build a counter with +, - and reset buttons. Colour the number red when it goes below zero.',
    resources: [{ name: 'DOM methods reference', type: 'PDF' }, { name: 'Counter starter', type: 'ZIP' }]
  },
  'Fetching data': {
    objectives: ['Call an API with fetch', 'Handle loading and errors', 'Render JSON on the page'],
    blocks: [
      { type: 'text', title: 'Talking to a server', body: 'fetch asks a server for data and returns a promise. With async/await the code reads top to bottom. Always check response.ok, because a 404 or 500 does not throw by itself.' },
      { type: 'code', lang: 'javascript', body: 'async function loadCourses() {\n  const res = await fetch("/api/courses");\n  if (!res.ok) throw new Error("Server said " + res.status);\n  const courses = await res.json();\n  list.innerHTML = courses.map(c => `<li>${c.title}</li>`).join("");\n}\n\nloadCourses().catch(err => alert(err.message));' },
      { type: 'tip', body: 'Show a spinner while waiting and a friendly message when the request fails. Users should never stare at a blank page.' },
      { type: 'warning', body: 'Escape any text you put into innerHTML (or build elements with textContent). Data from a server is still untrusted.' }
    ],
    task: 'Fetch a list of courses from /api/courses and render each one as a card. Show a message if the request fails.',
    resources: [{ name: 'fetch cheat sheet', type: 'PDF' }]
  },
  /* ---------- SQL & Data Analysis ---------- */
  'SELECT and filtering': {
    objectives: ['Write a SELECT with WHERE', 'Sort and limit results', 'Combine conditions with AND / OR'],
    blocks: [
      { type: 'text', title: 'Asking a table a question', body: 'SELECT chooses columns, FROM chooses the table and WHERE keeps only the rows that match. Queries read almost like English, but SQL runs them in a different order: FROM, WHERE, SELECT, ORDER BY.' },
      { type: 'code', lang: 'sql', body: 'SELECT TOP 5 title, price\nFROM Courses\nWHERE category = \'Data\' AND price > 0\nORDER BY price DESC;' },
      { type: 'list', items: ['= <> < > <= >= compare values', 'BETWEEN, IN and LIKE are shortcuts for ranges, lists and patterns', 'IS NULL (never = NULL) tests for missing values'] },
      { type: 'warning', body: 'NULL is not zero or an empty string. Any comparison with NULL using = is never true, so use IS NULL or IS NOT NULL.' }
    ],
    task: 'List the five cheapest paid courses, showing title and price, cheapest first.',
    resources: [{ name: 'Practice database script', type: 'SQL' }, { name: 'SQL filtering cheat sheet', type: 'PDF' }]
  },
  'Joins': {
    objectives: ['Explain what a join does', 'Write INNER and LEFT joins', 'Avoid accidental duplicate rows'],
    blocks: [
      { type: 'text', title: 'Combining tables', body: 'Data is split across tables to avoid repeating it. A join lines up rows from two tables using a shared key, such as Enrollments.courseId = Courses.courseId.' },
      { type: 'code', lang: 'sql', body: 'SELECT u.fullName, c.title, e.enrolledAt\nFROM Enrollments e\nINNER JOIN Users   u ON u.userId   = e.userId\nINNER JOIN Courses c ON c.courseId = e.courseId\nORDER BY e.enrolledAt DESC;' },
      { type: 'text', title: 'INNER vs LEFT', body: 'INNER JOIN keeps only rows that match on both sides. LEFT JOIN keeps every row from the left table and fills the right side with NULL when nothing matches. Use LEFT JOIN to find things that have no match, for example courses nobody enrolled in.' },
      { type: 'code', lang: 'sql', body: 'SELECT c.title\nFROM Courses c\nLEFT JOIN Enrollments e ON e.courseId = c.courseId\nWHERE e.enrollmentId IS NULL;' },
      { type: 'tip', body: 'Alias your tables (c, e, u) and always qualify column names. It keeps long queries readable.' }
    ],
    task: 'List every course with the number of students enrolled, including courses with zero students.',
    resources: [{ name: 'Join diagrams', type: 'PDF' }]
  },
  'Aggregations': {
    objectives: ['Use COUNT, SUM, AVG, MIN and MAX', 'Group rows with GROUP BY', 'Filter groups with HAVING'],
    blocks: [
      { type: 'text', title: 'Summarising data', body: 'Aggregate functions collapse many rows into one value. GROUP BY makes one summary row per group. WHERE filters rows before grouping, while HAVING filters groups after.' },
      { type: 'code', lang: 'sql', body: 'SELECT category,\n       COUNT(*)      AS courses,\n       AVG(rating)   AS avgRating\nFROM Courses\nGROUP BY category\nHAVING COUNT(*) >= 2\nORDER BY avgRating DESC;' },
      { type: 'warning', body: 'Every column in SELECT must either be inside an aggregate function or appear in GROUP BY, otherwise SQL Server raises an error.' },
      { type: 'tip', body: 'COUNT(*) counts rows; COUNT(column) skips NULLs.' }
    ],
    task: 'Show revenue per course (SUM of pricePaid) and keep only courses with revenue above 1,000,000.',
    resources: [{ name: 'Aggregate functions reference', type: 'PDF' }]
  }
};

const QUIZ_BANK = {
  'Intro to Web Development': [
    [ /* HTML basics */
      QZ('HTML structure quiz', 'Check the building blocks of an HTML page.', [
        Q('Which element contains everything the visitor can see on the page?', '<body>', ['<head>', '<title>', '<meta>'], 'The body element holds the visible content. head holds information about the page.'),
        Q('What does <!DOCTYPE html> do?', 'Tells the browser to render the page as modern HTML', ['Creates the page title', 'Imports the CSS file', 'Makes the page load faster'], 'The doctype puts the browser in standards mode so the page is rendered predictably.'),
        QM('Which of these are semantic tags? (select all that apply)', ['<header>', '<footer>', '<main>'], ['<div>', '<span>'], 'header, footer and main describe their purpose. div and span carry no meaning.'),
        Q('How many h1 headings should a normal page have?', 'One', ['As many as you like', 'None', 'Exactly three'], 'One h1 describes the main topic. Use h2 to h6 for sub-sections.')
      ], { time: 10 }),
      QZ('Forms and tables quiz', 'Practice with inputs and tabular data.', [
        Q('Which attribute links a <label> to its input?', 'for', ['name', 'href', 'target'], 'label for="x" matches the input whose id is "x".'),
        Q('Which input type gives you an email keyboard and free validation?', 'type="email"', ['type="text"', 'type="mail"', 'type="at"'], 'type="email" is validated by the browser and shows an email keyboard on phones.'),
        Q('Which tag makes a header cell in a table?', '<th>', ['<td>', '<tr>', '<thead>'], 'th is a header cell, td is a data cell, tr is a row.'),
        Q('When is a table the right choice?', 'When the information really is a grid of data', ['To position page columns', 'To add colour to a page', 'To make forms load faster'], 'Tables are for data. Use CSS for page layout.')
      ])
    ],
    [ /* CSS and Bootstrap */
      QZ('CSS and layout quiz', 'Selectors, the box model and flexbox.', [
        Q('Which selector targets every element with class="card"?', '.card', ['#card', 'card', '*card'], 'A leading dot means class. A leading # means id.'),
        Q('From the inside out, what is the correct order of the box model?', 'Content, padding, border, margin', ['Content, margin, border, padding', 'Padding, content, margin, border', 'Border, content, padding, margin'], 'Content is wrapped by padding, then the border, then the margin.'),
        Q('Which declaration turns an element into a flex container?', 'display: flex;', ['flex: container;', 'position: flex;', 'layout: flexbox;'], 'Setting display: flex on the parent lays its children out in a flex row.'),
        QM('Which of these Bootstrap classes control column width? (select all that apply)', ['col-12', 'col-md-4'], ['btn-lg', 'text-center'], 'col-* classes set how many of the 12 columns an element takes at a given breakpoint.')
      ], { time: 8 }),
      QZ('Responsive design quiz', 'Make one page work on every screen.', [
        Q('Which tag is required in head for a page to scale correctly on phones?', '<meta name="viewport" ...>', ['<meta charset>', '<link rel="mobile">', '<script responsive>'], 'Without the viewport meta tag phones show a zoomed-out desktop layout.'),
        Q('What does "mobile first" mean?', 'Write styles for small screens first, then add rules for bigger ones', ['Only support phones', 'Hide the desktop version', 'Use only images on mobile'], 'Mobile first uses min-width media queries to enhance the layout as the screen grows.'),
        Q('Which media query applies styles from tablet width upwards?', '@media (min-width: 768px)', ['@media (max-width: 768px)', '@media tablet', '@media (width = 768)'], 'min-width applies from that width up.')
      ])
    ],
    [ /* JavaScript */
      QZ('JavaScript fundamentals quiz', 'The DOM, events and fetching data.', [
        Q('Which method finds the first element matching a CSS selector?', 'document.querySelector()', ['document.find()', 'document.getAll()', 'document.select()'], 'querySelector takes any CSS selector and returns the first match.'),
        Q('Which is the safest way to show text typed by a user?', 'element.textContent = text', ['element.innerHTML = text', 'document.write(text)', 'eval(text)'], 'textContent treats the value as plain text, so injected scripts cannot run.'),
        Q('What must you check after await fetch(url)?', 'response.ok', ['response.done', 'response.length', 'response.valid'], 'fetch only rejects on network failure; a 404 or 500 still resolves, so check response.ok.'),
        Q('Which method attaches a click handler?', 'addEventListener("click", fn)', ['onClickAttach(fn)', 'listen("click", fn)', 'bind.click(fn)'], 'addEventListener registers a function to run when the event happens.')
      ], { time: 10 }),
      QZ('Final check: web development', 'A mixed review of the whole course.', [
        Q('Which language describes the STRUCTURE of a page?', 'HTML', ['CSS', 'JavaScript', 'SQL'], 'HTML structures content, CSS styles it and JavaScript adds behaviour.'),
        Q('Which language adds interactivity such as reacting to clicks?', 'JavaScript', ['HTML', 'CSS', 'Markdown'], 'JavaScript runs in the browser and responds to events.'),
        QM('Which of these are good accessibility habits? (select all that apply)', ['Link labels to inputs', 'Use semantic tags', 'Keep a sensible heading order'], ['Use tables for layout', 'Skip alt text on images'], 'Labels, semantic tags and ordered headings help assistive technology. Layout tables and missing alt text hurt it.'),
        Q('In what order does the browser load these for a typical page?', 'HTML is parsed, then CSS and JS apply to it', ['JS first, then HTML', 'CSS first, then the server', 'It loads all at once with no order'], 'The browser builds the DOM from HTML, then CSS styles it and scripts manipulate it.'),
        Q('Where should you put the viewport meta tag?', 'Inside head', ['Inside body', 'After the footer', 'In the CSS file'], 'Meta tags that describe the page belong in head.')
      ], { pass: 80, time: 12 })
    ]
  ],
  'SQL & Data Analysis': [
    [ QZ('SQL basics quiz', 'SELECT, filtering, joins and aggregations.', [
        Q('Which clause keeps only rows that match a condition?', 'WHERE', ['GROUP BY', 'ORDER BY', 'SELECT'], 'WHERE filters rows before any grouping happens.'),
        Q('How do you test for a missing value?', 'column IS NULL', ['column = NULL', 'column == 0', 'column EMPTY'], 'Comparisons with NULL using = are never true, so use IS NULL.'),
        Q('Which join keeps every row from the left table, even without a match?', 'LEFT JOIN', ['INNER JOIN', 'CROSS APPLY', 'SELF JOIN'], 'LEFT JOIN fills the right side with NULL when nothing matches.'),
        Q('Which clause filters groups AFTER aggregation?', 'HAVING', ['WHERE', 'DISTINCT', 'TOP'], 'WHERE runs before grouping, HAVING after.'),
        QM('Which of these are aggregate functions? (select all that apply)', ['COUNT', 'AVG', 'MAX'], ['UPPER', 'GETDATE'], 'COUNT, AVG and MAX summarise many rows into one value.')
      ], { time: 12 }) ],
    [ QZ('Data cleaning quiz', 'Handle messy data.', [
        Q('What does TRIM do?', 'Removes leading and trailing spaces', ['Deletes the row', 'Rounds a number', 'Converts text to a date'], 'TRIM cleans stray spaces, a very common data quality problem.'),
        Q('What does COALESCE(a, 0) return when a is NULL?', '0', ['NULL', 'An error', 'An empty string'], 'COALESCE returns the first non-NULL argument.'),
        Q('Why is a CTE (WITH ...) useful?', 'It names an intermediate result so a query is easier to read', ['It makes tables permanent', 'It encrypts data', 'It adds an index'], 'A CTE lets you break a long query into readable, named steps.')
      ]) ],
    [ QZ('Schema and dashboards quiz', 'Design a schema and present the results.', [
        Q('What uniquely identifies a row in a table?', 'Primary key', ['Foreign key', 'Index name', 'View'], 'A primary key is unique and not null for each row.'),
        Q('What does a foreign key do?', 'Links a row to a row in another table', ['Encrypts a column', 'Sorts a table', 'Speeds up every query'], 'A foreign key enforces that the referenced row exists.'),
        Q('Which chart best shows change over time?', 'Line chart', ['Pie chart', 'Scatter of names', 'Word cloud'], 'Lines make trends over time easy to see.')
      ]) ]
  ]
};

/* ---------- deterministic helpers so the mock data never changes between reloads ---------- */
const seededRng = n => { let s = (n % 2147483646) + 1; return () => (s = s * 16807 % 2147483647) / 2147483647; };
const shuf = (a, rnd) => a.map(v => [rnd(), v]).sort((x, y) => x[0] - y[0]).map(x => x[1]);
const pick = (pool, n, avoid, rnd) => shuf([...new Set(pool)].filter(x => !avoid.includes(x)), rnd).slice(0, n);

function genBlocks(title, courseTitle, objectives) {
  const t = title.toLowerCase();
  return [
    { type: 'text', title: 'Overview', body: title + ' is a core building block of ' + courseTitle + '. In this lesson you will see what it is, why it matters and how it connects to the rest of the section.' },
    { type: 'steps', items: ['Read the overview and watch the walkthrough', 'Follow the worked example yourself, step by step', 'Change one thing and predict the result before you run it', 'Write down one question to review later'] },
    { type: 'tip', body: 'Explain ' + t + ' out loud in your own words. If you can teach it, you understand it.' },
    { type: 'warning', body: 'Do not skip the practice task. Recognising an idea is much easier than using it.' },
    { type: 'list', title: 'Recap', items: objectives }
  ];
}

function lessonObj(title, minutes, r, n) {
  const objectives = ['Understand the key ideas behind ' + title.toLowerCase(), 'Apply them in a short hands-on exercise', 'Recognise common mistakes and best practices'];
  const hand = LESSON_BODIES[title];
  return {
    title, minutes, type: LTYPES[n % 4], videoUrl: null,
    objectives: hand ? hand.objectives : objectives,
    blocks: hand ? hand.blocks : genBlocks(title, r.title, objectives),
    task: hand ? hand.task : 'Complete the short exercise for "' + title + '" and compare your result with the sample solution.',
    resources: hand ? hand.resources : [{ name: title + ' (slides)', type: 'PDF' }, { name: 'Exercise and sample files', type: 'ZIP' }]
  };
}

/* generated quiz from the course's own data: lessons, objectives, skills, instructor */
function genQuizzes(r, sIdx, sec, courseNo, raw, isLast) {
  const rnd = seededRng(courseNo * 97 + sIdx * 13 + 5);
  const otherLessons = raw.flatMap((x, i) => i === courseNo - 1 ? [] : x.cur.flatMap(s => s.split('::')[1].split(';').map(l => l.split('|')[0])));
  const sameCourseOther = r.cur.flatMap((s, i) => i === sIdx ? [] : s.split('::')[1].split(';').map(l => l.split('|')[0]));
  const lessonPool = [...sameCourseOther, ...otherLessons];
  const otherLearn = raw.flatMap((x, i) => i === courseNo - 1 ? [] : x.learn), otherSkills = raw.flatMap((x, i) => i === courseNo - 1 ? [] : x.skills);
  const otherInstr = raw.filter((x, i) => i !== courseNo - 1).map(x => x.instructor), otherLevels = ['Beginner', 'Intermediate', 'Advanced'];
  const titles = sec.lessons.map(l => l.title);
  const qs = [
    Q('Which lesson belongs to the section "' + sec.title + '"?', titles[0], pick(lessonPool, 3, titles, rnd), 'This lesson is part of the "' + sec.title + '" section of ' + r.title + '.'),
    Q('Which of these is a goal of the lesson "' + titles[titles.length - 1] + '"?', 'Understand the key ideas behind ' + titles[titles.length - 1].toLowerCase(),
      pick(lessonPool, 3, titles, rnd).map(x => 'Understand the key ideas behind ' + x.toLowerCase()), 'Each lesson starts with understanding its key ideas before practising them.'),
    Q('Which of these skills does ' + r.title + ' teach?', r.skills[0], pick(otherSkills, 3, r.skills, rnd), r.title + ' covers: ' + r.skills.join(', ') + '.'),
    QM('Which of these will you learn in this course? (select all that apply)', pick(r.learn, 2, [], rnd), pick(otherLearn, 2, r.learn, rnd), 'The course outcomes are: ' + r.learn.join('; ') + '.')
  ];
  const quizzes = [QZ(sec.title + ' quiz', 'Check what you learned in this section.', qs, { time: 8 })];
  if (isLast) quizzes.push(QZ('Final check: ' + r.title, 'A short mixed review of the whole course.', [
    Q('Who teaches ' + r.title + '?', r.instructor, pick(otherInstr, 3, [r.instructor], rnd), r.instructor + ' is the instructor of this course.'),
    Q('What is the level of ' + r.title + '?', r.level, otherLevels.filter(x => x !== r.level), 'This course is aimed at ' + r.level.toLowerCase() + ' learners.'),
    Q('Which skill is part of this course?', r.skills[r.skills.length - 1], pick(otherSkills, 3, r.skills, rnd), 'Skills you gain: ' + r.skills.join(', ') + '.'),
    QM('Select the outcomes of this course.', pick(r.learn, 2, [], rnd), pick(otherLearn, 2, r.learn, rnd), 'The course outcomes are: ' + r.learn.join('; ') + '.')
  ], { pass: 75, time: 8 }));
  return quizzes;
}

/* turns a quiz template into the stored shape with stable ids; options are shuffled once, deterministically */
function finishQuiz(qz, quizId, rndSeed) {
  const rnd = seededRng(rndSeed);
  return { quizId, title: qz.title, description: qz.description, passPercent: qz.passPercent, timeLimitMin: qz.timeLimitMin,
    questions: qz.questions.map((q, qi) => { const questionId = quizId * 100 + qi + 1;
      return { questionId, text: q.text, multi: q.multi, explanation: q.explanation,
        options: shuf([...q.correct.map(t => [t, true]), ...q.wrong.map(t => [t, false])], rnd).map(([text, correct], oi) => ({ optionId: questionId * 10 + oi + 1, text, correct })) }; }) };
}

/* builds the curriculum stored in the mock database: sections -> lessons (+ rich content) and quizzes */
function buildCurriculum(r, courseNo, raw) {
  let n = 0;
  raw = raw.map(x => ({ ...x, cur: x.cur }));
  return r.cur.map((str, sIdx) => {
    const [title, ls] = str.split('::');
    const lessons = ls.split(';').map(l => { const [t, m] = l.split('|'); return lessonObj(t, +m, r, n++); });
    const sec = { title, lessons };
    sec.summary = 'In this section: ' + lessons.map(l => l.title.toLowerCase()).join(', ') + '.';
    const tpl = QUIZ_BANK[r.title]?.[sIdx] || genQuizzes(r, sIdx, sec, courseNo, raw, sIdx === r.cur.length - 1);
    sec.quizzes = tpl.map((qz, k) => finishQuiz(qz, courseNo * 1000 + (sIdx + 1) * 10 + k + 1, courseNo * 31 + sIdx * 7 + k));
    return sec;
  });
}
