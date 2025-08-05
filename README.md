# Automated Code Review Assistant
> An intelligent backend system built with Spring Boot that integrates with GitHub to automate code reviews. This tool performs static analysis, provides AI-driven feedback on logic and optimization, and visualizes code quality trends over time.

## Overview
The Automated Code Review Assistant is designed to streamline the development workflow by providing immediate, actionable feedback on code commits and pull requests. It connects directly to GitHub repositories, analyzes code for rule-based violations, and uses the Gemini AI to offer deeper insights into code logic, potential bugs, and optimization opportunities. The goal is to improve code quality, reduce manual review effort, and help development teams maintain high standards.

## Key Features
* **GitHub Integration**: Securely connects to GitHub using OAuth2 for user authentication. It uses the GitHub API to fetch repository data and post feedback directly to pull requests.
* **Webhook Automation**: Automatically creates webhooks on configured repositories to listen for `push` and `pull_request` events, triggering analysis in real-time.
* **Static Code Analysis**: A built-in rule engine (using JavaParser) analyzes Java code for specific violations, such as identifying public static fields that are not final.
* **AI-Powered Feedback**: Integrates with the Google Gemini API to provide high-level, contextual feedback on pull requests. The AI reviews the entire code diff to comment on logic, optimizations, and overall quality.
* **Actionable Feedback**: Posts two types of feedback directly to pull requests:
    * Line-by-line comments for specific rule violations.
    * A general summary comment with the AI's overall review.
* **Review Insights Dashboard**: A user-friendly dashboard that visualizes code quality trends with a chart showing the number of violations detected over time.
* **Database Persistence**: Stores repository configurations and analysis results in a PostgreSQL database for tracking and reporting.

## Tech Stack
* **Backend**: Spring Boot 3, Spring Security (OAuth2), Spring Data JPA
* **Language**: Java 21
* **Build Tool**: Maven
* **Database**: PostgreSQL
* **Frontend**: Thymeleaf, Tailwind CSS
* **Data Visualization**: Chart.js
* **Core Integrations**:
    * GitHub API (`org.kohsuke:github-api`)
    * Google Gemini API
    * JavaParser for static analysis

## Getting Started
Follow these instructions to set up and run the project on your local machine.

### Prerequisites
* Java Development Kit (JDK): Version 21 or higher.
* Apache Maven: For dependency management and building the project.
* PostgreSQL: A running instance of the PostgreSQL database.
* ngrok: To expose your local server to the internet for receiving GitHub webhooks.

### 1. Clone the Repository
```bash
git clone <your-repository-url>
cd code-review-assistant
```

### 2. Configure the Database
* Start your PostgreSQL server.
* Create a new database for the application.
```bash
CREATE DATABASE code_review_db;
```

### 3. Set Up API Keys and Secrets
* In src/main/resources/, create a new file named secrets.properties.
* Add your secret keys to this file. This file is listed in .gitignore and should never be committed.

```bash
# src/main/resources/secrets.properties

# Your PostgreSQL database password
spring.datasource.password=your_db_password

# GitHub Personal Access Token (PAT)
# See instructions below
github.api.token=ghp_YourGitHubPersonalAccessToken

# GitHub OAuth App Credentials
# See instructions below
spring.security.oauth2.client.registration.github.client-id=Iv1.YourGitHubOAuthAppClientID
spring.security.oauth2.client.registration.github.client-secret=YourGitHubOAuthAppClientSecret

# Google AI Studio API Key for Gemini
# See instructions below
ai.model.api-key=YourGoogleGeminiApiKey
```

### 4. Configure the Application
* Update the src/main/resources/application.properties file with your database details and ngrok URL.

```bash

# src/main/resources/application.properties

# --- POSTGRESQL DATABASE CONFIGURATION ---
spring.datasource.url=jdbc:postgresql://localhost:5432/code_review_db
spring.datasource.username=postgres # Or your PostgreSQL username

# --- JPA/HIBERNATE CONFIGURATION ---
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update

# --- IMPORT SECRETS ---
# This line tells Spring Boot to load your secrets file.
spring.config.import=optional:secrets.properties

# --- NGROK PUBLIC URL ---
# Start ngrok (ngrok http 8080) and paste your public https URL here.
app.base-url=[https://your-unique-id.ngrok-free.app](https://your-unique-id.ngrok-free.app)

# --- AI SERVICE CONFIGURATION ---
# The Gemini Pro model endpoint.
ai.model.endpoint=[https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent](https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent)
```

### 5. API Key and OAuth App Setup
* GitHub Personal Access Token (PAT):

  * Go to GitHub Settings > Developer settings > Personal access tokens > Fine-grained tokens.
  * Generate a new token with `Read and write` access to Pull requests and Webhooks, and `Read-only` access to Contents.
  * Grant it access to the repositories you want to test.
  * Copy the token into your `secrets.properties` file.

* GitHub OAuth App:

  * Go to GitHub Settings > Developer settings > OAuth Apps > New OAuth App.
  * Application name: `Code Review Assistant`
  * Homepage URL: `http://localhost:8080`
  * Authorization callback URL: `http://localhost:8080/login/oauth2/code/github`
  * Generate a new client secret, and copy the Client ID and Client Secret into secrets.properties.

* Google Gemini API Key:

  * Go to Google AI Studio.
  * Create a new API key and copy it into `secrets.properties`.

### 6. Build and Run the Application
1. Start ngrok in a separate terminal:

```bash
ngrok http 8080
```

*(Make sure to update the `app.base-url` in `application.properties` with the URL ngrok provides)*

2. Build the project using Maven:

```bash
mvn clean install
```
3. Run the Spring Boot application:

```bash
mvn spring-boot:run
```

The application will be running at `http://localhost:8080`.

### Usage
1. Navigate to http://localhost:8080 in your browser.
2. Log in with your GitHub account.
3. Click on "Configure Repositories".
4. Find the repository you want to analyze and click "Enable Analysis". This will create a webhook on your GitHub repository.
5. To test the analysis:
* Push Event: Make a code change (e.g., add a non-final public static field) and push it to your repository. The results will be saved to the database and visible on the Results page.
* Pull Request Event: Create a new branch, make a code change, and open a pull request. The assistant will post comments directly on the PR.
6. Click on "View Dashboard" to see a chart of code quality trends over time.