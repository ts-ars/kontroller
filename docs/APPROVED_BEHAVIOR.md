# Approved application behavior

Baseline date: 2026-08-14
Scope: test environment `kontroller-test-app` / `kontroller-test-db`

## Change-control rule

The current structure, styles, calculations and behavior are approved. A future task may change only the element explicitly named by the owner.

Before any visual or behavioral change:

1. Preserve all unrelated behavior and page elements.
2. Show an HTML mockup or an exact diff when requested.
3. Implement only the approved delta.
4. Test desktop and mobile behavior affected by the delta.
5. If a mandatory check fails, roll back the test application.
6. Never modify production or real production data without separate explicit authorization.

## Shared layout

- Navigation order: Plan–Fact, Comments, Report, Settings, Users.
- The navigation and main page content retain the currently approved spacing and alignment.
- Pages use one shared responsive layout.
- Desktop layout must not be changed while fixing mobile behavior.
- Navigation labels must not collapse into unreadable text.
- Settings and Users are shown only where allowed by role.

## Authentication and roles

- Internal pages require authentication.
- Authentication uses a local user and PIN.
- Blocked users are not displayed in the sign-in user list.
- Roles: USER, ADMIN, OWNER.
- Users page is available only to OWNER.
- USER may edit their own explanations.
- ADMIN and OWNER may edit permitted explanations and exclude report rows.

## Default opening state

- After authentication, Plan–Fact opens sensors 5–6.
- The active shift is selected from the current production time: 07:00–15:00 or 15:00–23:00.
- Comments opens sensor 5 and the corresponding active shift.

## Signals and live updates

- Physical ADAM channels map directly to sensors 1–6. Channel 0 is not used as sensor 1.
- A received counter increment is processed; the application must not discard a valid signal because of a restart or production-day counter reset.
- Sensor 5 is currently physically connected; sensors 1–4 and 6 may appear later without remapping.
- Live data updates table counters and chart values without recreating the chart canvas or re-rendering the whole page.
- Live updates must work in desktop and mobile browsers.

## Plan–Fact

- Sensors 1–4 and sensors 5–6 retain their currently approved grouping.
- The current shift interval is selected automatically.
- Time, Plan and Actual remain a compact three-column row on mobile.
- Comment occupies a separate full-width row below them on mobile.
- Charts retain their proportions at all supported widths.
- For a completed interval, entered explanations are shown. An unexplained stoppage remains visible as unexplained.

## Stoppages and explanations

- A stoppage and its user explanation are separate records conceptually.
- Deleting an explanation does not delete the stoppage.
- An unexplained stoppage does not disappear without trace.
- Automatic stoppages are finalized only for completed intervals.
- Zero-minute stoppages are not created or displayed.
- Empty default stoppages are not generated merely because an interval exists.
- An explanation may contain category, comment, allocated minutes, allocated cans and author.
- ALLOCATION_CONFLICT must not be accepted as a correct final allocation; the user must correct the allocated time.
- Add reason is always available:
  - for an existing stoppage it adds another explanation row;
  - when no system stoppage exists, the operator may choose an interval and create a MANUAL stoppage.
- A MANUAL stoppage is the boundary between the two Add reason behaviors:
  - Add reason on a system-detected stoppage adds an explanation to that same stoppage;
  - Add reason on an existing MANUAL stoppage starts creation of a new MANUAL stoppage and opens the interval selector again.
- The interval selector is not a one-time control. After every successfully saved MANUAL stoppage, the operator can invoke Add reason again and select any available interval for the next stoppage.
- Creating a later MANUAL stoppage must not append an explanation to, overwrite or move an earlier MANUAL stoppage.
- A manual stoppage belongs to the date, sensor, shift and interval currently opened on Comments.
- Manual lost production uses the hourly plan:

  `lost cans = round(hourly plan × stoppage minutes / 60)`

- The same formula is used in the live preview and after Save.
- Example: plan 600 and 15 minutes produces 150 lost cans.

## Comments

- Existing Comments functionality must be extended, not reconstructed.
- Date selection opens the chosen production day for correction.
- Sensor and shift selection remain available.
- Existing saved rows use Save and Delete.
- Author is displayed.
- Responsive layout must keep field boundaries readable without overlapping tables or vertically spelling button labels.
- Mobile and narrowed desktop layouts retain the approved adaptive behavior.

## Report

- Sensors 1–6 remain selectable.
- Date range filtering and Export Excel remain unchanged.
- Export returns a valid `.xlsx` file.
- For sensors 1–4 and 6, the right chart groups lost cans by date. A single selected date produces one proportionate bar.
- For sensor 5, the right chart groups lost cans by source sensor.
- The left chart groups lost minutes by type.
- Chart bar width remains capped and visually proportionate.
- Approved table layout:
  - without Source: Type 20%, Minutes 10%, Cans 10%, Reason 30%, Author 15%, Actions 15%;
  - with Source: Source 10%, Type 16%, Minutes 10%, Cans 10%, Reason 28%, Author 14%, Actions 12%.
- If Actions is absent, its space is assigned to Reason.
- Reason receives priority for readable long text.
- Author content is centered.
- OWNER/ADMIN row deletion uses an application-owned centered confirmation dialog on the existing Report page:
  - the URL and page do not change;
  - the page is dimmed behind the dialog;
  - deletion reason is required;
  - Cancel closes without changes;
  - Delete uses the existing exclusion operation and then refreshes the report;
  - the browser-native prompt is not used.

## Settings

- Settings retains the approved table and content position shared with other pages.
- Plans are interpreted as hourly production rates even when display intervals are longer than 60 minutes.

## Protected behavior

Do not change without separate explicit approval:

- page structure or navigation order;
- desktop spacing while addressing mobile issues;
- sensor/channel mapping;
- default sensors 5–6 and time-based shift selection;
- stoppage visibility rules;
- hourly lost-production formula;
- chart grouping differences between sensor 5 and sensors 1–4/6;
- live updates without chart recreation;
- report column proportions;
- comment ownership and role permissions;
- deletion confirmation behavior.
