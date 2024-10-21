Claro! Aqui está a documentação em Markdown puro:

# API Documentation for Electronic Point System

## Public Routes

1. **Health Check**
  - **Method:** GET
  - **Endpoint:** /health
  - **Description:** Verifies if the API is running.
  - **Response:**
    - **200 OK**
      - **Body:** "Hello from Kotlin/Ktor API!"

2. **User Registration**
  - **Method:** POST
  - **Endpoint:** /register
  - **Description:** Registers a new user in the system.
  - **Request Body:**
    ```json
    {
      "name": "string",
      "email": "string",
      "plainPassword": "string",
      "timeIntervals": [
        {
          "enter": "HH:mm",
          "exit": "HH:mm"
        }
      ],
      "timeZone": "string"
    }
    ```
  - **Response:**
    - **201 Created**
      - **Body:** User ID of the created user (integer).
3. **User Login**
  - **Method:** POST
  - **Endpoint:** /login
  - **Description:** Authenticates an existing user and returns a JWT token.
  - **Request Body:**
    ```json
    {
      "email": "string",
      "plainPassword": "string"
    }
    ```
  - **Response:**
    - **200 OK**
      - **Body:** JWT token (string).

---

## Authenticated User Routes (Requires JWT)

1. **Update User Password**
  - **Method:** PATCH
  - **Endpoint:** /users/update-password
  - **Description:** Updates the password for the authenticated user.
  - **Request Body:**
    ```json
    {
      "currentPlainPassword": "string",
      "newPlainPassword": "string"
    }
    ```
  - **Response:**
    - **200 OK**
      - **Body:** Success status (boolean).
2. **Create Point**
  - **Method:** POST
  - **Endpoint:** /users/point
  - **Description:** Creates a point for the authenticated user.
  - **Response:**
    - **201 Created**
      - **Body:** Point ID of the created point (integer).
3. **Get User Points**
  - **Method:** GET
  - **Endpoint:** /users/points
  - **Description:** Retrieves all points for the authenticated user.
  - **Response:**
    - **200 OK**
      - **Body:** List of points for the user, formatted as:
        ```json
        [
          {
            "id": "integer",
            "relatedUserId": "integer",
            "instant": "string (ISO-8601 format)"
          }
        ]
        ```
    - **404 Not Found**
      - **Body:** "No points found for requested user ID"

---

## Admin-Only Routes (Requires Admin Authentication)

1. **Admin Health Check**
  - **Method:** GET
  - **Endpoint:** /admin/health
  - **Description:** Verifies if the admin API is running.
  - **Response:**
    - **200 OK**
2. **Get All Users**
  - **Method:** GET
  - **Endpoint:** /admin/users
  - **Description:** Retrieves all users in the system.
  - **Response:**
    - **200 OK**
      - **Body:** List of users, formatted as:
        ```json
        [
          {
            "id": "integer",
            "name": "string",
            "email": "string",
            "timeIntervals": [
              {
                "enter": "HH:mm",
                "exit": "HH:mm"
              }
            ],
            "isAdmin": "boolean"
          }
        ]
        ```
3. **Update User Time Intervals**
  - **Method:** PATCH
  - **Endpoint:** /admin/users/{id}/update-time-intervals
  - **Description:** Updates the time intervals for a specified user.
  - **Path Parameters:**
    - id (integer): ID of the user.
  - **Request Body:**
    ```json
    [
      {
        "enter": "HH:mm",
        "exit": "HH:mm"
      }
    ]
    ```
  - **Response:**
    - **200 OK**
      - **Body:** Success status (boolean).
4. **Delete User**
  - **Method:** DELETE
  - **Endpoint:** /admin/users/{id}
  - **Description:** Deletes a specified user by ID.
  - **Path Parameters:**
    - id (integer): ID of the user.
  - **Response:**
    - **200 OK**
      - **Body:** Success status (boolean).
5. **Get All Points**
  - **Method:** GET
  - **Endpoint:** /admin/points
  - **Description:** Retrieves all points from the database.
  - **Response:**
    - **200 OK**
      - **Body:** List of points, formatted as:
        ```json
        [
          {
            "id": "integer",
            "relatedUserId": "integer",
            "instant": "string (ISO-8601 format)"
          }
        ]
        ```