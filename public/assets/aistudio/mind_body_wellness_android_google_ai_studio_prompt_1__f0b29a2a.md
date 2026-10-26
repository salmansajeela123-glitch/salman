# Google AI Studio Prompt: Mental & Physical Wellness Android App

Use the prompt below in **Google AI Studio** to generate an Android application focused on building healthy mental and physical habits. It is designed to encourage sustainable progress, not to replace professional medical care.

---

## Copy-and-paste prompt

You are a senior Android engineer, product designer, wellness coach, and AI application architect. Build a polished, production-minded Android application that helps users gradually improve their **mental well-being and physical health** through daily routines, habit tracking, reflection, movement, sleep, and healthy lifestyle education.

### 1. Product vision

Create an Android app tentatively named **Mind & Body**. The app should help users build consistency through small, achievable actions. It must feel supportive, calm, accessible, private, and non-judgmental. Avoid guilt, shame, extreme fitness messaging, unrealistic promises, or claims that the app can diagnose or treat medical conditions.

The main experience should help a user answer:
- How am I feeling today?
- What small action can I take for my mind and body?
- What habits have I practiced consistently?
- What patterns can I reflect on over time?

### 2. Platform and implementation

- Target Android phones first, with responsive layouts for different screen sizes.
- Prefer **Kotlin + Jetpack Compose** for a native Android implementation. If the current Google AI Studio app-building workflow requires a different supported stack, explain the choice and use the most suitable supported approach.
- Use Material 3 design components and a clear, maintainable architecture.
- Separate UI, business logic, data models, and data storage.
- Include setup instructions, required dependencies, configuration steps, and commands needed to build and run the app.
- Do not claim that code has been tested or built unless it actually has been.
- Use current, supported APIs and avoid deprecated methods.
- Keep secrets, API keys, and private credentials out of source code. If Gemini or another AI service is integrated, use a secure server-side approach for production rather than embedding a long-lived secret in the APK. Provide a clearly documented development configuration.

### 3. Core screens and features

#### A. Onboarding
- Welcome screen explaining the app's purpose and limitations.
- Let users select goals such as stress management, mindfulness, better sleep habits, daily movement, strength, flexibility, energy, or general consistency.
- Ask for optional preferences: available time per day, experience level, preferred activity types, and reminder preferences.
- Make personal information optional wherever possible.
- Include a clear privacy explanation and allow users to skip onboarding where practical.

#### B. Home dashboard
- Display a friendly greeting, today's date, and a simple daily wellness overview.
- Show a small number of suggested actions, not an overwhelming list.
- Include quick actions for mood check-in, breathing exercise, movement, water tracking, and journaling.
- Show progress in a positive, non-competitive way.
- Offer a “Today’s plan” with realistic actions based on the user's chosen goals and available time.

#### C. Mental wellness
- **Mood check-in:** Let users select a mood and optionally add tags or a short note.
- **Guided breathing:** Provide a simple visual breathing timer with adjustable session lengths. Clearly label it as a relaxation exercise, not a treatment.
- **Mindfulness:** Offer short, beginner-friendly guided practices and grounding exercises.
- **Journaling:** Provide optional prompts such as “What is on my mind?”, “What went well today?”, and “What is one thing I need?”
- **Stress check-in:** Let users record perceived stress on a simple scale and optionally note context.
- **Reflection:** Display trends over time without claiming that correlations prove causes or making diagnoses.
- Keep all journaling and mood entries private by default.

#### D. Physical wellness
- **Movement plans:** Provide beginner-friendly walking, stretching, mobility, and bodyweight routines, with easier alternatives.
- **Workout timer:** Include start, pause, resume, and finish controls, plus rest intervals where useful.
- **Activity log:** Let users record activity type, duration, and optional notes.
- **Habit tracking:** Support user-defined habits such as walking, stretching, taking breaks, or doing a short workout.
- **Sleep routine:** Allow users to record approximate sleep and wake times or a simple self-rating; do not present this as medical sleep measurement.
- **Hydration tracking:** Allow optional water logging with a user-adjustable goal. Do not imply that one fixed amount is right for everyone.
- Include warm-up and cool-down guidance where appropriate. Provide low-impact alternatives and remind users to stop if they experience pain, dizziness, chest pain, or unusual shortness of breath; urgent or severe symptoms warrant medical help.
- Do not prescribe extreme exercise, crash diets, or unsafe weight-loss practices. Do not require weight, body measurements, or calorie counting.

#### E. Personalized daily plan
- Build a plan from the user's selected goals, preferred time commitment, and chosen activities.
- Start with small actions and let users edit, skip, or replace suggestions.
- Make rest days and missed days normal. Never shame users for breaking a streak.
- Explain why an activity is suggested in simple, non-medical language.
- Do not infer medical conditions from moods, journal entries, sleep, or activity logs.

#### F. Progress and insights
- Show weekly summaries of completed habits, movement minutes, mindfulness sessions, mood check-ins, and sleep logs when available.
- Use clear charts and accessible labels.
- Highlight consistency and personal reflection rather than ranking users against others.
- Allow users to choose date ranges and view or delete individual entries.
- Do not present app-collected data as a diagnosis or clinical assessment.

#### G. Reminders and accessibility
- Let users enable, disable, and schedule gentle local notifications.
- Make reminder text supportive and easy to dismiss.
- Respect Android notification permissions and operating-system limits.
- Support dark mode, scalable text, accessible contrast, screen readers, touch targets, and reduced-motion preferences where feasible.
- Avoid notifications that reveal sensitive mood or journal information on a lock screen.

#### H. Settings, privacy, and data management
- Provide settings for goals, reminders, theme, and accessibility.
- Allow users to export their data in a readable format and delete their data.
- Store data locally by default for the first version. Clearly state what is stored and where.
- If cloud sync is added, make it opt-in and explain what is transmitted, why, and how it is protected.
- Never sell or expose journal entries or mood data. Do not send sensitive data to an AI model unless the user explicitly opts in and the data flow is clearly explained.
- Include an appropriate privacy policy placeholder that must be completed before public release.

### 4. AI assistant (optional, privacy-conscious)

If AI functionality is supported and configured, add an optional wellness companion that:
- Offers general educational information, reflection questions, and small habit suggestions.
- Uses a warm, respectful, non-judgmental tone.
- Clearly distinguishes general wellness information from medical advice.
- Does not diagnose, prescribe medication, recommend stopping medication, or claim to replace a clinician.
- Does not make unsupported claims about mental health or fitness outcomes.
- Asks before using personal journal or mood data to personalize a response.
- Uses a secure backend for production API calls; never expose a production API key in client-side code.
- Includes graceful loading, retry, offline, quota, and service-error states.
- Can function without AI for all core tracking and wellness features.

**Crisis and safety behavior:** If a user expresses imminent danger, suicidal intent, or intent to harm someone, respond empathetically and encourage contacting local emergency services or a trusted person immediately. Do not present the app as a crisis service. Where location is unknown, ask the user to contact their local emergency number or a local crisis support service. Do not rely on AI as the sole safety mechanism.

### 5. Visual design

Use a modern, calming, uncluttered design:
- Soft, balanced colors with sufficient contrast; support light and dark themes.
- Rounded cards, clear typography, generous spacing, simple icons, and restrained animation.
- A bottom navigation bar with sections such as **Home**, **Mind**, **Move**, **Progress**, and **Settings**.
- Use encouraging copy such as “Small steps count” and “You can start again today.”
- Avoid medical-looking dashboards, alarmist colors, manipulative streak mechanics, and excessive gamification.
- Use sensible empty states, loading states, error messages, and confirmation dialogs.

### 6. Data model and engineering quality

Define appropriate data models for:
- User preferences and goals
- Mood check-ins
- Journal entries
- Breathing or mindfulness sessions
- Activity sessions and workout routines
- Habit definitions and completions
- Sleep logs
- Hydration entries
- Reminder preferences

Requirements:
- Validate input and handle missing or invalid values safely.
- Use a suitable local persistence layer for the chosen stack.
- Include clear separation of concerns and reusable components.
- Add sample/demo data only when clearly marked and easy to remove.
- Include unit tests for important business logic and data validation, plus UI tests for key flows where feasible.
- Handle app restarts without losing saved local data.
- Provide helpful error states rather than crashing.
- Avoid collecting unnecessary personal information.

### 7. MVP priorities

Build the app in manageable stages, keeping it runnable after each stage:

**Stage 1 — Functional MVP**
1. Onboarding and goal selection
2. Home dashboard
3. Mood check-in
4. Breathing exercise timer
5. Habit tracker
6. Basic movement/activity log
7. Weekly progress view
8. Local persistence
9. Settings and data deletion

**Stage 2 — Enhancements**
1. Journaling and reflection prompts
2. Sleep and hydration logs
3. Local reminders
4. More routines and accessibility refinements

**Stage 3 — Optional AI**
1. Wellness companion
2. Personalized suggestions with explicit consent
3. Secure backend integration and privacy review

Prioritize a reliable, usable MVP over generating many incomplete screens. If the full application cannot be created in one pass, implement Stage 1 end-to-end first and list the remaining work clearly.

### 8. Acceptance criteria

The result should:
- Launch into a usable home screen after onboarding.
- Let users record and review mood check-ins.
- Run a breathing session with working controls.
- Let users create habits, mark them complete, and see progress.
- Let users log a physical activity.
- Preserve data after closing and reopening the app.
- Let users inspect and delete their stored data.
- Handle empty states, invalid input, permissions, and common failures gracefully.
- Work without an AI connection for core features.
- Include build/run instructions and a concise explanation of architecture and limitations.
- Include tests for the most important functionality where feasible.

### 9. How to work

Before coding, briefly state:
1. The supported stack you will use in this Google AI Studio environment.
2. The architecture and local storage approach.
3. Which Stage 1 features you will implement first.

Then generate the implementation files and connect the main user flows. Do not stop at a mockup or static UI: implement functional interactions and persistence wherever the environment supports them. If a feature cannot be fully implemented in the current environment, say exactly what remains and provide the next concrete steps.

At the end, provide:
- Project/file structure
- Setup and run instructions
- Required configuration and permissions
- Tests and how to run them
- Known limitations
- A short checklist for privacy, safety, and release readiness

Build a helpful, sustainable wellness companion that supports the user without pressure and keeps personal data under their control.

---

## How to use this prompt

1. Open **Google AI Studio** and start a new app/project using the app-building experience available to you.
2. Paste the full prompt from the section above.
3. Ask it to implement **Stage 1 first** if the initial output is too large or incomplete.
4. Review the generated code, run it in the supported environment, and fix build errors before adding optional AI features.
5. Before publishing, review privacy, data deletion, notification permissions, and the crisis-safety behavior.

**Important:** Generated code may need manual changes and testing. Verify the current Google AI Studio workflow and Android export/build support for your account before relying on a particular output format. This app is for general wellness and is not a substitute for professional medical care.
