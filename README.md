Here's your updated README with the deployment link changed to Koyeb:

```markdown
# Ticket Booking API 🎟️

## Overview
Welcome to the **Ticket Booking API**! 🚀 This API allows users to book and manage event tickets securely using **JWT authentication** and is built using **Spring Boot** with **MongoDB** as the database.

## Features

🔹 **Admin Features:**
- 🛠️ Create, update, delete, and view events.
- 📊 View all customer bookings.
- 👤 View all registered users.

🔹 **User Features:**
- 🎫 Book and cancel tickets.
- 💰 Add and remove balance from the wallet.
- 🔍 Filter events by various criteria (date, category, rating, etc.).
- 🔐 Secure authentication with JWT.
- ⏳ Rate limiting to prevent abuse.

## Live API URL
The API is deployed on **Koyeb** and can be accessed at:

👉 **[Live API](https://administrative-nickie-talha-atif-43eb1dd0.koyeb.app/)**

## API Documentation (Swagger)

Swagger UI is enabled for API testing and documentation.

👉 **[Swagger UI](https://administrative-nickie-talha-atif-43eb1dd0.koyeb.app/swagger-ui.html)**

## Tech Stack 🛠️
- **Spring Boot** (Backend framework)
- **MongoDB** (NoSQL Database)
- **Spring Security** (JWT-based authentication)
- **Stripe API** (For payment processing)
- **SpringDoc OpenAPI** (For API documentation)

## Installation & Setup ⚙️

### 1. Clone the Repository
```sh
$ git clone https://github.com/Progambler227788/TicketManagement-SpringBoot.git
$ cd TicketManagement-SpringBoot
```

### 2. Configure Environment Variables
Create a `.env` file in the root directory and set the required values:

```
SERVER_PORT=8080
MONGO_URI=mongodb+srv://your-username:your-password@cluster.mongodb.net/ticketdb
SECRET_KEY=your-secret-key
STRIPE_SECRET=your-stripe-secret-key
STRIPE_API=your-stripe-api-key
```

### 3. Build & Run the Application
#### Using Maven:
```sh
$ mvn clean install
$ mvn spring-boot:run
```

The API will be accessible at `http://localhost:8080`

## API Endpoints 📌

### 🔐 Authentication APIs
| Method | Endpoint | Description |
|--------|---------|-------------|
| `POST` | `/api/auth/signup` | Register a new user |
| `POST` | `/api/auth/login` | Authenticate and get JWT token |
| `PUT` | `/api/auth/updateProfile` | Update user profile |

### 👑 Admin APIs
| Method | Endpoint | Description |
|--------|---------|-------------|
| `POST` | `/api/admin/events/addEvent` | Add a new event |
| `GET` | `/api/admin/events/getAllEvents` | Get all events |
| `GET` | `/api/admin/users/getAllUsers` | Get all registered users |
| `GET` | `/api/admin/users/getAllBookings` | Get all bookings |
| `DELETE` | `/api/admin/events/{id}` | Delete an event by ID |
| `DELETE` | `/api/admin/events/deleteAllEvents` | Delete all events |

### 👤 User APIs
| Method | Endpoint | Description |
|--------|---------|-------------|
| `POST` | `/api/user/profile/bookTicket` | Book a ticket |
| `DELETE` | `/api/user/profile/cancelTicket` | Cancel a ticket |
| `GET` | `/api/user/profile/getBookings` | Get user bookings |
| `POST` | `/api/user/profile/wallet/addBalance` | Add balance to wallet |
| `POST` | `/api/user/profile/wallet/deductBalance` | Deduct balance from wallet |
| `GET` | `/api/user/profile/wallet/getUserBalance` | Get wallet balance |
| `GET` | `/api/user/profile/events/searchByCategory` | Search events by category |
| `GET` | `/api/user/profile/events/searchByDateRange` | Search events by date range |
| `GET` | `/api/user/profile/events/searchByLocation` | Search events by location |
| `GET` | `/api/user/profile/events/searchByTitleOrDescription` | Search events by title or description |

## Security 🛡️
- **JWT Authentication:** Secure endpoints require a valid JWT token.
- **Role-Based Access Control (RBAC):** Users must have `ROLE_USER` or `ROLE_ADMIN` to access specific endpoints.
- **Rate Limiting:** Protects against abuse by limiting API requests.

## Deployment on Koyeb 🚀

### 1. Create a Koyeb Account
Sign up at [Koyeb](https://www.koyeb.com/)

### 2. Create a New Service
- Connect your GitHub repository
- Select the appropriate branch
- Configure environment variables

### 3. Deploy the Application
Koyeb will automatically build and deploy your application

## Contributing 🤝
Feel free to fork this project, create a feature branch, and submit a pull request. Contributions are always welcome!

---

Happy Coding! 🚀
```
