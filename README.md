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
The API is deployed on **Heroku** and can be accessed at:

👉 **[Live API](https://tickojet-8f5e27f79912.herokuapp.com/)**

## API Documentation (Swagger)

Swagger UI is enabled for API testing and documentation.

👉 **[Swagger UI](https://tickojet-8f5e27f79912.herokuapp.com/swagger-ui.html)**

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

#### Using Docker:
```sh
$ docker build -t ticket-booking-api .
$ docker run -p 8080:8080 ticket-booking-api
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

## Deployment on Heroku 🚀

### 1. Create a Heroku App
```sh
$ heroku create your-app-name
```

### 2. Add MongoDB (Optional: If using MongoDB Atlas, skip this step)
```sh
$ heroku addons:create mongolab:sandbox
```

### 3. Set Environment Variables
```sh
$ heroku config:set SERVER_PORT=8080 MONGO_URI=your-mongodb-uri SECRET_KEY=your-secret-key STRIPE_SECRET=your-stripe-secret-key STRIPE_API=your-stripe-api-key
```

### 4. Deploy the Application
```sh
$ git push heroku main
```

### 5. Open the App
```sh
$ heroku open
```

## Contributing 🤝
Feel free to fork this project, create a feature branch, and submit a pull request. Contributions are always welcome!

## License 📜
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.

---

Happy Coding! 🚀
