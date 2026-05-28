# Smart Team Generator

Android Studio Java application for a university software engineering project. The app helps a professor manage student skill profiles and generate project teams using either homogeneous grouping or balanced snake-draft allocation.

## Main Features

- Add student profiles with 12 skill scores from 1 to 5.
- Seed the app with 120 demo students and varied skill profiles.
- Let the professor view a small sample of students on the dashboard.
- Open a searchable popup to view all students and their skills.
- Delete students from the full student popup when no teams are currently generated.
- Generate teams by selected skill or overall average.
- Open a searchable popup to view all generated teams.
- Delete all generated teams.
- Persist students, skills, teams, and team memberships with Room.

## Architecture

The project follows an MVVM-style structure:

- XML layouts define the screens and row views.
- `MainActivity` handles UI events and binds views with View Binding.
- `ProjectViewModel` exposes app data and actions to the UI.
- `ProjectRepository` manages background database operations.
- `ProjectDao` defines Room queries and transactions.
- Room entities model users, skills, teams, and many-to-many team membership.
- Utility classes contain the team generation algorithm and demo seed data.

## Project Structure

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

## UI Components

### `activity_main.xml`

The main screen contains three major sections:

- Initial choice panel with `Add Student` and `Professor Dashboard`.
- Add Student screen for entering name, email, and all 12 skill scores.
- Professor Dashboard for viewing student samples, generating teams, viewing generated teams, and deleting teams.

The dashboard intentionally shows only a small sample inline:

- First 8 students.
- First 4 generated teams.

The complete lists are available through popup dialogs with search bars.

### `item_student.xml`

RecyclerView row for a student. It displays:

- Student name.
- Student email.
- Skill summary with all saved skill values.

Used both in the professor dashboard sample list and the full searchable student popup.

### `item_team.xml`

RecyclerView row for a generated team. It displays:

- Team name.
- Strategy and criteria used.
- Member count.
- Member names and emails.

Used both in the professor dashboard sample list and the full searchable team popup.

## Java Components

### `MainActivity.java`

Main UI controller for the whole app.

Responsibilities:

- Uses View Binding through `ActivityMainBinding`.
- Builds the dynamic 12-skill input fields for the Add Student form.
- Shows and hides the Add Student and Professor Dashboard sections.
- Observes students, skills, and teams from `ProjectViewModel`.
- Saves student profiles.
- Generates teams from professor settings.
- Deletes generated teams.
- Shows searchable popups for all students and all teams.
- Allows student deletion from the full student popup only when generated teams are cleared.
- Filters students by name, email, skill name, or skill score.
- Blocks student deletion while teams exist, so generated memberships cannot become inconsistent.
- Filters teams by team name, strategy, criteria, or member name/email.

### `ProjectViewModel.java`

MVVM ViewModel between the UI and repository.

Responsibilities:

- Exposes `LiveData<List<UserEntity>>` for students.
- Exposes `LiveData<List<UserSkillEntity>>` for all skills.
- Exposes `LiveData<List<TeamWithMembers>>` for generated teams.
- Registers student profiles.
- Generates teams through `TeamGeneratorHelper`.
- Seeds 120 demo students through `DemoDataSeeder`.
- Deletes all teams.

### `ProjectRepository.java`

Repository layer that keeps database work off the main thread.

Responsibilities:

- Wraps Room DAO calls.
- Uses a single-thread `ExecutorService` for writes.
- Inserts users and skills.
- Saves full student profiles in one repository call.
- Replaces generated teams.
- Deletes all generated teams.

## Room Data Layer

### `ProjectDatabase.java`

Room database singleton.

Includes these entities:

- `UserEntity`
- `UserSkillEntity`
- `TeamEntity`
- `TeamUserCrossRef`

Also registers `Converters` for enum persistence.

### `ProjectDao.java`

Room DAO for all database operations.

Main operations:

- Insert users.
- Insert user skills.
- Observe all students.
- Observe all user skills.
- Observe teams with members.
- Insert a full student profile transactionally.
- Insert demo student profiles transactionally.
- Replace all generated teams transactionally.
- Delete all generated teams and memberships.

### `UserEntity.java`

Represents an app user.

Fields:

- `id`
- `name`
- `email`
- `role`

For this app, seeded and manually added users are stored with role `STUDENT`.

### `UserSkillEntity.java`

Represents one skill score for one student.

Fields:

- `userId`
- `skillName`
- `proficiencyLevel`

Primary key:

- `userId`
- `skillName`

This flat table avoids storing arrays or nested objects in Room.

### `TeamEntity.java`

Represents a generated team.

Fields:

- `id`
- `teamName`
- `strategyUsed`
- `criteriaUsed`

### `TeamUserCrossRef.java`

Join table for the many-to-many relationship between teams and users.

Fields:

- `teamId`
- `userId`

Primary key:

- `teamId`
- `userId`

### `TeamWithMembers.java`

Room relation POJO that combines:

- One `TeamEntity`.
- A list of `UserEntity` members.

Used by the UI to display generated teams with their assigned students.

### `Converters.java`

Room type converters for enums:

- `Role`
- `GenerationStrategy`

Room stores these enum values as strings.

## Model Classes

### `Role.java`

Enum for user roles:

- `PROFESSOR`
- `STUDENT`

### `GenerationStrategy.java`

Enum for team generation strategy:

- `HOMOGENEOUS`
- `BALANCED`

### `SkillName.java`

Central list of valid skill names.

The 12 plain skills:

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

Also includes:

- `OVERALL_AVERAGE`

`OVERALL_AVERAGE` is used only as a sorting/generation criterion, not as a stored skill row.

## UI Adapters

### `StudentAdapter.java`

RecyclerView adapter for student rows.

Responsibilities:

- Displays name and email.
- Builds a readable skill summary from the flat skill table.
- Used in both the dashboard sample and full student popup.

### `TeamAdapter.java`

RecyclerView adapter for team rows.

Responsibilities:

- Displays team name.
- Shows strategy, criteria, and member count.
- Lists team members.
- Used in both the dashboard sample and full team popup.

## Utility Classes

### `DemoDataSeeder.java`

Creates 120 deterministic demo students.

The seeded students include different skill profiles:

- Technical builder.
- Security/networking student.
- Data science student.
- Hardware/physics student.
- Team organizer.
- Balanced generalist.

The seeder uses fixed demo email IDs like:

```text
demo.student.001@university.test
```

Because inserts use `OnConflictStrategy.REPLACE`, running the seeder again updates the same demo students instead of creating duplicate demo accounts.

### `TeamGeneratorHelper.java`

Standalone team generation logic.

Inputs:

- Student list.
- Skill list.
- Target students per team.
- Selected criteria.
- Selected generation strategy.

Main behavior:

- Sorts students by selected skill or `OVERALL_AVERAGE`.
- Calculates team sizes with even remainder distribution.
- Creates Room-ready `TeamEntity` records.
- Creates Room-ready `TeamUserCrossRef` membership records.

## Team Generation Logic

### Remainder Distribution

The app calculates how many teams can be formed from the target team size, then spreads leftover students across existing teams.

Example:

```text
22 students, target size 5
22 / 5 = 4 teams
Final sizes: 6, 6, 5, 5
```

### Homogeneous Strategy

Students are sorted from strongest to weakest by the chosen criterion.

Then the app fills teams in order:

```text
Team 1: strongest nearby scores
Team 2: next strongest nearby scores
Team 3: next group
...
```

This groups students with similar proficiency levels.

### Balanced Strategy

Students are sorted from strongest to weakest by the chosen criterion.

Then they are distributed with a snake draft:

```text
Team 1 -> Team 2 -> Team 3 -> Team 3 -> Team 2 -> Team 1
```

This spreads high-scoring students across teams more evenly.

## Search Behavior

### Student Popup Search

The full student popup can search by:

- Student name.
- Student email.
- Skill name.
- Raw skill constant.
- Skill score.

The popup also includes a `Delete student` button for each student. A student can be deleted only when there are no generated teams. If teams exist, the professor must tap `Delete all teams` first.

### Team Popup Search

The full generated teams popup can search by:

- Team name.
- Strategy.
- Criteria.
- Member name.
- Member email.

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

## How To Use The App

1. Open the app.
2. Choose `Add Student` to add a new student and their skill scores.
3. Go back.
4. Choose `Professor Dashboard`.
5. Review the student sample or open the full searchable student popup.
6. Choose team size, sorting criterion, and generation strategy.
7. Tap `Generate teams`.
8. Review the team sample or open the full searchable teams popup.
9. Tap `Delete all teams` when you want to clear generated teams.

## Build Check

The project was verified with:

```text
./gradlew compileDebugJavaWithJavac
```

The latest compile check completed successfully.
