# Smart Team Generator

Smart Team Generator is an Android application built for a university software engineering project. Its purpose is to help a professor organize students into project teams using measurable skill data instead of manual guessing.

The app stores student profiles, records each student's proficiency across 12 skills, and generates teams using one of two strategies:

- `HOMOGENEOUS`: students with similar strengths are grouped together.
- `BALANCED`: stronger and weaker students are distributed across teams using a snake-draft pattern.

The application is mainly a professor-facing tool. It also includes a simple `Add Student` flow so new student profiles can be entered quickly.

## Project Definition

The problem this app solves is team formation. In many university projects, professors need to divide students into groups, but students often have different strengths. Some may be better at programming, some at mathematics, some at hardware, some at teamwork, and others at security or data science.

Instead of creating teams randomly, this app allows the professor to:

- Store each student's skill profile.
- Select the skill that matters most for the project.
- Choose whether teams should be similar internally or balanced against each other.
- Generate teams automatically.
- Inspect generated teams and student skills through searchable popups.

## Skill Model

Each student is scored from `1` to `5` for each plain skill:

```text
1 = Weak
2 = Below average
3 = Average
4 = Good
5 = Strong
```

The 12 stored skills are:

- `PROGRAMMING_LANGUAGES`
- `SYSTEM_SECURITY`
- `HARDWARE_INFRASTRUCTURE`
- `NETWORK_ARCHITECTURE`
- `ALGORITHMIC_THINKING`
- `MATHEMATICS`
- `PHYSICS`
- `DATA_SCIENCE_STATISTICS`
- `TEAM_COLLABORATION`
- `CRITICAL_THINKING`
- `ADAPTABILITY`
- `TIME_MANAGEMENT`

The professor can also choose:

- `OVERALL_AVERAGE`

`OVERALL_AVERAGE` is not stored as a separate skill. It is calculated during team generation as the mean of the 12 stored skill values.

## User Presentation

When the app opens, the first screen shows two main choices:

- `Add Student`
- `Professor Dashboard`

### Add Student

This section is only for entering student data.

The user enters:

- Student name.
- Student email.
- A score from 1 to 5 for all 12 skills.

After saving, the student is stored in the Room database. The form clears so another student can be added.

### Professor Dashboard

This is the main working area of the app.

The professor can:

- See a small sample of students directly on the page.
- Open a popup to view all students and their skills.
- Search students by name, email, skill name, or score.
- Delete students from the popup, but only when no teams are currently generated.
- Select target team size.
- Select sorting criteria.
- Select generation strategy.
- Generate teams.
- See a small sample of generated teams directly on the page.
- Open a popup to view all generated teams.
- Search teams by team name, strategy, criteria, or member.
- Delete all generated teams.

The dashboard intentionally shows only samples inline so the screen remains readable even with 120 students. Full lists are placed in searchable popup panels.

## Team Matching Algorithm

The team matching logic is implemented in `TeamGeneratorHelper.java`. It receives:

- The current list of students.
- The current list of skill rows.
- Target number of students per team.
- Selected sorting criteria.
- Selected generation strategy.

It returns:

- A list of `TeamEntity` objects.
- A list of `TeamUserCrossRef` objects that connect students to teams.
- A grouped in-memory list of students for immediate use.

## Step 1: Convert Skill Rows Into Scores

Student skills are stored in a flat Room table. Each row stores one student, one skill, and one proficiency value.

Example:

```text
studentA, PROGRAMMING_LANGUAGES, 5
studentA, MATHEMATICS, 4
studentB, PROGRAMMING_LANGUAGES, 2
```

Before sorting, the helper builds a lookup map:

```text
studentId -> skillName -> score
```

This makes it fast to ask questions like:

```text
What is Maria's score for PROGRAMMING_LANGUAGES?
What is Alex's OVERALL_AVERAGE?
```

If a skill is missing, the helper treats it as `0` for sorting. Normal saved student profiles contain all 12 skills.

## Step 2: Calculate The Sorting Score

The selected criteria controls each student's sorting score.

If the professor selects a plain skill:

```text
score = student's value for that skill
```

Example:

```text
criteria = PROGRAMMING_LANGUAGES
student score = programming score
```

If the professor selects `OVERALL_AVERAGE`:

```text
score = sum of all 12 skill scores / 12
```

Example:

```text
Scores: 5, 4, 3, 3, 4, 5, 2, 4, 3, 4, 5, 3
Total: 45
Overall average: 45 / 12 = 3.75
```

After each score is calculated, students are sorted from strongest to weakest. If two students have the same score, their names are used as a tie-breaker.

## Step 3: Calculate Number Of Teams

The target team size is used to decide how many teams should be created.

The app uses integer division:

```text
teamCount = studentCount / targetTeamSize
```

If the number of students is smaller than the target team size, the app still creates one team.

Example:

```text
4 students, target size 5
teamCount = 1
```

For 120 students and target size 5:

```text
120 / 5 = 24 teams
```

## Step 4: Distribute Remainders Evenly

If students do not divide perfectly into the target size, the remainder is spread across already formed teams.

Example:

```text
22 students, target size 5
22 / 5 = 4 teams
```

The app does not create a tiny leftover team of 2 students. Instead:

```text
22 students / 4 teams = base size 5
remainder = 2
```

Final result:

```text
Team 1: 6 students
Team 2: 6 students
Team 3: 5 students
Team 4: 5 students
```

This keeps team sizes realistic and avoids very small teams.

## Strategy 1: Homogeneous Matching

The homogeneous strategy is used when the professor wants students with similar ability levels grouped together.

Process:

1. Calculate each student's score for the selected criteria.
2. Sort students from highest score to lowest score.
3. Fill Team 1 first, then Team 2, then Team 3, and so on.

Example with 12 students and target size 4:

```text
Sorted students by score:
S1 5.0
S2 5.0
S3 4.8
S4 4.6
S5 4.0
S6 3.8
S7 3.4
S8 3.2
S9 2.5
S10 2.3
S11 2.0
S12 1.8
```

Generated teams:

```text
Team 1: S1, S2, S3, S4
Team 2: S5, S6, S7, S8
Team 3: S9, S10, S11, S12
```

Meaning:

- Strong students are grouped with strong students.
- Mid-level students are grouped with mid-level students.
- Weaker students are grouped with students at a similar level.

This strategy is useful when the professor wants teams to have internally similar proficiency, for example advanced groups and beginner groups.

## Strategy 2: Balanced Snake-Draft Matching

The balanced strategy is used when the professor wants every team to have a fair mix of stronger and weaker students.

Process:

1. Calculate each student's score for the selected criteria.
2. Sort students from highest score to lowest score.
3. Assign students using a snake-draft pattern.

For 4 teams, the allocation direction looks like this:

```text
Round 1: Team 1 -> Team 2 -> Team 3 -> Team 4
Round 2: Team 4 -> Team 3 -> Team 2 -> Team 1
Round 3: Team 1 -> Team 2 -> Team 3 -> Team 4
Round 4: Team 4 -> Team 3 -> Team 2 -> Team 1
```

Example with 12 students and 3 teams:

```text
Sorted students:
S1 strongest
S2
S3
S4
S5
S6
S7
S8
S9
S10
S11
S12 weakest
```

Snake draft assignment:

```text
Team 1: S1, S6, S7, S12
Team 2: S2, S5, S8, S11
Team 3: S3, S4, S9, S10
```

Why this balances teams:

- Team 1 gets the strongest student, but later receives weaker students.
- Team 3 receives two strong early students near the turn, but also receives weaker students later.
- The back-and-forth order prevents one team from receiving all top students.

This strategy is useful when the professor wants teams to compete or perform at similar average levels.

## Why Snake Draft Is Better Than Simple Round Robin

A simple round robin always assigns in one direction:

```text
Team 1 -> Team 2 -> Team 3 -> Team 1 -> Team 2 -> Team 3
```

That can unfairly favor earlier teams because they repeatedly pick earlier in each round.

Snake draft reverses the direction each round:

```text
Team 1 -> Team 2 -> Team 3 -> Team 3 -> Team 2 -> Team 1
```

This gives later teams compensation picks and produces more balanced averages.

## Team Persistence

When teams are generated, the app saves:

- One `TeamEntity` for each team.
- One `TeamUserCrossRef` for each student-team membership.

This is a many-to-many design. A team can contain many students, and the relationship is stored separately from both the student table and the team table.

When the professor taps `Delete all teams`, the app clears:

- Team membership rows.
- Team rows.

Students and skill scores remain saved.

## Student Deletion Rule

Students can be deleted from the full student popup only when no teams are currently generated.

This rule exists because generated teams contain memberships that refer to students. Deleting a student while teams exist would make the current team result harder to reason about.

The professor workflow is:

1. Delete all generated teams.
2. Open the full student popup.
3. Delete the student.
4. Generate teams again if needed.

## Demo Data

The app seeds 120 demo students when the student database is empty.

The seeded profiles include several student types:

- Technical builder.
- Security/networking student.
- Data science student.
- Hardware/physics student.
- Team organizer.
- Balanced generalist.

This makes the professor dashboard useful immediately after installation and gives the generation algorithms realistic data to work with.

Demo student emails use this format:

```text
demo.student.001@university.test
```

The seeder only runs when there are no students. This means manually deleted students do not keep reappearing after the database already has data.

## How To Use The App

1. Open the app.
2. Choose `Add Student` to add a student and their 12 skill scores.
3. Tap `Go back`.
4. Choose `Professor Dashboard`.
5. Review the inline student sample.
6. Tap `View all students and skills` to open the searchable student popup.
7. Choose target team size, sorting criterion, and generation strategy.
8. Tap `Generate teams`.
9. Review the inline team sample.
10. Tap `View all generated teams` to open the searchable team popup.
11. Tap `Delete all teams` when generated teams should be cleared.

## Technical Structure

The project follows an MVVM-style structure:

- XML layouts define screens and row views.
- `MainActivity` handles UI events and binds views with View Binding.
- `ProjectViewModel` exposes app data and actions to the UI.
- `ProjectRepository` manages background database operations.
- `ProjectDao` defines Room queries and transactions.
- Room entities model users, skills, teams, and many-to-many team membership.
- Utility classes contain the team generation algorithm and demo seed data.

## Project Files

```text
app/src/main/java/com/example/matchy_team_generator/
  MainActivity.java
  data/
    Converters.java
    ProjectDao.java
    ProjectDatabase.java
    TeamEntity.java
    TeamUserCrossRef.java
    TeamWithMembers.java
    UserEntity.java
    UserSkillEntity.java
  model/
    GenerationStrategy.java
    Role.java
    SkillName.java
  repository/
    ProjectRepository.java
  ui/
    StudentAdapter.java
    TeamAdapter.java
  util/
    DemoDataSeeder.java
    TeamGeneratorHelper.java
  viewmodel/
    ProjectViewModel.java

app/src/main/res/
  layout/
    activity_main.xml
    item_student.xml
    item_team.xml
  values/
    arrays.xml
    colors.xml
    strings.xml
    styles.xml
```

## UI Files

### `activity_main.xml`

Main layout file. It contains:

- The initial choice panel.
- The Add Student form.
- The Professor Dashboard.
- Buttons for viewing full student and team popups.
- Controls for team size, sorting criteria, and strategy.

### `item_student.xml`

RecyclerView row for student display.

Shows:

- Name.
- Email.
- Skill summary.
- Delete button when used inside the full student popup.

### `item_team.xml`

RecyclerView row for generated team display.

Shows:

- Team name.
- Strategy and criteria.
- Member count.
- Member names and emails.

## Java Files

### `MainActivity.java`

Main UI controller.

Responsibilities:

- Shows and hides the Add Student and Professor Dashboard sections.
- Creates dynamic skill input fields.
- Observes `LiveData` from the ViewModel.
- Saves student profiles.
- Generates teams.
- Deletes generated teams.
- Opens searchable student and team popups.
- Blocks student deletion when teams exist.

### `ProjectViewModel.java`

Connects the UI to the repository.

Responsibilities:

- Exposes students, skills, and teams as `LiveData`.
- Registers student profiles.
- Calls the team generator.
- Seeds demo students.
- Deletes teams.
- Deletes students.

### `ProjectRepository.java`

Repository layer for database operations.

Responsibilities:

- Runs writes on an `ExecutorService`.
- Wraps Room DAO calls.
- Inserts students and skills.
- Saves generated teams.
- Deletes teams and students.

### `ProjectDao.java`

Room DAO.

Responsibilities:

- Insert users.
- Insert skills.
- Observe students.
- Observe skills.
- Observe teams with members.
- Replace generated teams transactionally.
- Delete teams.
- Delete students.

### `ProjectDatabase.java`

Room database singleton.

Stores:

- Users.
- User skills.
- Teams.
- Team-user cross references.

### `UserEntity.java`

Room entity for app users.

Fields:

- `id`
- `name`
- `email`
- `role`

### `UserSkillEntity.java`

Room entity for skill scores.

Fields:

- `userId`
- `skillName`
- `proficiencyLevel`

Primary key:

- `userId`
- `skillName`

### `TeamEntity.java`

Room entity for generated teams.

Fields:

- `id`
- `teamName`
- `strategyUsed`
- `criteriaUsed`

### `TeamUserCrossRef.java`

Join table between teams and users.

Fields:

- `teamId`
- `userId`

### `TeamWithMembers.java`

Room relation object that combines:

- One `TeamEntity`.
- A list of `UserEntity` members.

### `Converters.java`

Room type converters for:

- `Role`
- `GenerationStrategy`

### `SkillName.java`

Central skill constants and validation logic.

### `Role.java`

Enum for:

- `PROFESSOR`
- `STUDENT`

### `GenerationStrategy.java`

Enum for:

- `HOMOGENEOUS`
- `BALANCED`

### `StudentAdapter.java`

RecyclerView adapter for students.

Used by:

- Dashboard sample list.
- Full student popup.

### `TeamAdapter.java`

RecyclerView adapter for generated teams.

Used by:

- Dashboard sample list.
- Full team popup.

### `DemoDataSeeder.java`

Creates deterministic demo students with varied skill profiles.

### `TeamGeneratorHelper.java`

Contains the core matching logic:

- Score calculation.
- Team size calculation.
- Homogeneous grouping.
- Balanced snake-draft grouping.
- Creation of team entities and membership rows.

## Gradle Configuration

View Binding is enabled in `app/build.gradle.kts`.

Important dependencies:

- AppCompat
- Material Components
- Lifecycle LiveData
- Lifecycle ViewModel
- Room Runtime
- Room Compiler through `annotationProcessor`
- RecyclerView

## Build Check

The project was verified with:

```text
./gradlew compileDebugJavaWithJavac
```

The latest compile check completed successfully.
