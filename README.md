# QuitCare - Smoking Cessation Support System 🚭

**QuitCare** is a digital platform designed to help users quit smoking through personalized plans, daily tracking, health monitoring, coach consultations, and a supportive online community. The system also integrates a membership model and automated motivational notifications.

---

## 🚀 Key Features

### 1. User Management
- Registration (regular & via Google)
- Login, password reset, email verification
- Role-based access: `GUEST`, `CUSTOMER`, `COACH`, `STAFF`, `ADMIN`
- Profile update and user activation

### 2. Smart Quit Smoking Plan
- Record daily smoking behavior and health notes
- Generate or create personalized quit plans
- Track progress, earned points, money saved, and health trends
- Automatic congratulatory or motivational emails

### 3. Membership & Payment
- Membership packages: `BASIC`, `PREMIUM`
- Only `PREMIUM` users can book coach consultation sessions
- Integrated VNPay payment gateway
- Transaction history and soft-delete supported

### 4. Coach Consultation
- Coaches set available schedules
- Customers can book, coaches confirm
- Google Meet link is generated automatically
- Session rating and feedback supported

### 5. Community Interaction
- Users can create posts, leave comments, and share achievements
- Ranking system based on activity and milestones

### 6. Admin & Coach Dashboards
- Admin: user analytics, revenue, feedback overview
- Coach: consultation count, ratings, today’s schedule

---

## 🏗️ Architecture

- **Backend**: Java 17, Spring Boot, Spring Security, JWT, JPA, Lombok
- **Database**: MySQL (`quitcare_db`)
- **Libraries**: Swagger (OpenAPI), ModelMapper, Jakarta Validation, VNPay SDK

---

## 🗃️ Data Structure (ERD Summary)

- `Account`: User profile and roles
- `MembershipPlan`, `UserMembership`, `PaymentHistory`: Membership and payments
- `SmokingStatus`: Initial user data
- `QuitPlan`, `QuitPlanStage`, `QuitProgress`: Smoking cessation journey
- `Session`, `FeedbackSession`: Coach session booking & reviews
- `CommunityPost`, `Comment`: Community interactions
- `UserAchievement`: Reward badges
- `MessageNotification`: Internal notification/email system
- `Feedback`: User-submitted feedback to admin
