# URL Shortener

A simple URL shortener

It provides:
- a REST API for creating, redirecting, listing, and deleting shortened URLs
- file-based persistence using H2
- a minimal decoupled frontend for interacting with the API
- automated tests
- Docker support for running the full application locally

<img width="716" height="697" alt="Screenshot from 2026-03-17 12-26-03" src="https://github.com/user-attachments/assets/75dbc8b8-2647-45f4-b4b3-baadd24ff5a9" />

[Screencast from 2026-03-17 13-43-47.webm](https://github.com/user-attachments/assets/316ce50f-27a9-4592-9a46-ae4ee8ea0569)


## Tech stack

### Backend
- Java 17
- Spring Boot 3.5.11
- Maven
- Spring Web
- Spring Validation
- Spring Data JPA
- H2 (file-based)
- Lombok



### Frontend
- Node.js
- Express
- Static HTML / CSS / JavaScript

## API summary

The API follows the provided `openapi.yaml`.

### `POST /shorten`
Creates a shortened URL.

Request body:

```json
{
  "fullUrl": "https://example.com/very/long/url",
  "customAlias": "my-custom-alias"  // optional
}
```

Response (`201 Created`):

```json
{
  "shortUrl": "http://localhost:8080/my-custom-alias" // or a randomly generated alias
}
```

### `GET /{alias}`
Redirects to the original URL.

Response:
- `302 Found` if alias exists
- `404 Not Found` if alias does not exist

### `DELETE /{alias}`
Deletes a shortened URL mapping from persistence.

Response:
- `204 No Content` if deleted
- `404 Not Found` if alias does not exist

### `GET /urls`
Returns all shortened URLs.

Response (`200 OK`):

```json
[
  {
    "alias": "my-custom-alias",
    "fullUrl": "https://example.com/very/long/url",
    "shortUrl": "http://localhost:8080/my-custom-alias"
  },
  {
    "alias": "kqmdz-plnvrtaeuw",
    "fullUrl": "https://example.org/another/long/url",
    "shortUrl": "http://localhost:8080/kqmdz-plnvrtaeuw"
  }
]
```



## Design notes

- `shortUrl` is not persisted
- persisted fields are:
  - `alias`
  - `fullUrl`
- `shortUrl` is derived as `base-url + "/" + alias`
- base-url is read from the environment by the `APP_BASE_URL` or is the default `http://localhost:8080/`
- example:
  - `fullUrl = https://example.com/very/long/url`
  - `alias = my-custom-alias`
  - `shortUrl = http://localhost:8080/my-custom-alias`

### Alias rules

Custom aliases are optional and validated as follows:
- lowercase letters and hyphens only
- must start and end with a lowercase letter
- no consecutive hyphens
- max length: 16 characters

## Validation and error handling

- DTO validation handles basic field validation
- API  layer request validation checks that `fullUrl` is a valid absolute `http` or `https` URL
- business rules such as alias uniqueness are handled in the service layer
- API errors are returned as JSON error responses

Example error response:

```json
{
  "error": "Alias already exists: my-custom-alias"
}
```



## Running with Docker Compose

From the repository root:

```bash
docker-compose up --build
```

Services:
- frontend: `http://localhost:3000`
- backend: `http://localhost:8080`

The backend uses a Docker volume for H2 file persistence.

To stop containers:

```bash
docker-compose down
```

To stop containers and remove the persisted database volume:

```bash
docker-compose down -v
```



## Running locally without Docker

### Prerequisites
- Java 17
- Maven
- Node.js and npm

### Backend
From `backend/url-shortener-api/`:

```bash
./mvnw spring-boot:run
```

### Tests
can be run at the same location by:

```bash
./mvnw test
```


Backend runs on:

```text
http://localhost:8080
```

### Frontend
From the `frontend/` directory:

```bash
npm ci
npm start
```

Frontend runs on:

```text
http://localhost:3000
```

and can be accessed by opening `http://localhost:3000` in a browser

## Frontend

A minimal decoupled frontend is included, it supports:
- creating short URLs
- listing saved URL mappings
- deleting saved URL mappings
- showing API success and error messages

The frontend is served separately, 
the browser essentially retrieves the static web pages 
from the frontend docker container and calls the API directly from the browser.


## Assumptions / scope

- aliases are unique across all saved URL mappings.
- a url can have many aliases.
- H2 file-based persistence being sufficient for this exercise

## Notes

This submission intentionally keeps the solution simple and focused on the requirements:
- manual implementation from the provided OpenAPI contract
- backend-first approach
- minimal decoupled frontend
- simple Docker setup

Time spent: approximately ~15 hours across several sessions.




## Example usage

### Create a short URL
```bash
curl -i -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{
    "fullUrl": "https://example.com/very/long/url",
    "customAlias": "my-custom-alias"
  }'
```

### Redirect using alias
```bash
curl -i http://localhost:8080/my-custom-alias
```

### List all shortened URLs
```bash
curl -i http://localhost:8080/urls
```

### Delete an alias
```bash
curl -i -X DELETE http://localhost:8080/my-custom-alias
```

 
