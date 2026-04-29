# CarParts – Work Completed


**David Byrne (X00203114)**

---

## Work I Completed

### Initial Android Application Setup
I completed the initial setup of the Android application, establishing the foundation for the project. This included configuring the development environment, setting up the project structure, managing dependencies, and ensuring the application could build and run correctly on the Android emulator during development and testing.

This initial setup provided a stable base for further development and ensured that all features could be implemented on a properly configured and functioning application.

---

### API and Database Integration
While the backend API and database were developed by Adrian, I handled the integration of these services into the Android application. This involved connecting the app to the provided API endpoints, sending HTTP requests, and handling responses to retrieve and display data such as car parts and related information.

I implemented the logic required to process API responses, including parsing returned data and updating the user interface dynamically. I also handled error scenarios such as failed requests, invalid responses, or missing data to ensure the application remained stable and user-friendly.

This integration was essential in enabling communication between the frontend and backend, transforming the application from a static interface into a fully functional system with real-time data.

---

### Authentication Implementation and Flow Improvements
I implemented and refined the authentication system, including both login and signup functionality. This involved validating user inputs such as email and password formats and ensuring that authentication requests were handled correctly.

I also improved the authentication flow by introducing clearer validation messages and better feedback for failed sign-in attempts. For example, users are now informed when credentials are incorrect or required fields are missing. These improvements enhanced usability and reduced friction during the login process.

---

### Full UI Design and User Interface Improvements
I handled the UI implementation and improved the overall design. This included refining layout structure, spacing, and visual hierarchy to create a more intuitive and user-friendly experience.

A key enhancement was the redesign of the item sidebar, which improved navigation and made it easier for users to browse available car parts.

---

### Item Filtering Feature
I implemented a filtering feature that allows users to narrow down product listings based on relevant criteria. This functionality dynamically updates the displayed items based on user input, reducing the need to manually browse large lists.

---

### Language Toggle and Localisation (EN/GA)
The initial language toggle functionality was developed by Adrian as part of the account page. I extended this feature by integrating it across the application and keeping it as a persistent button visible, rather than being limited to a single screen.

This involved updating text resources and ensuring that all UI components respond correctly to language changes. The toggle now allows users to switch between English and Irish (GA).

---

### Cart and Checkout Functionality
I implemented the cart system and checkout process, allowing users to add items, manage their basket, and complete purchases. This included handling cart state, updating item quantities, calculating totals, and processing checkout actions.

I also ensured that users receive clear feedback during checkout, including confirmation messages for successful transactions and appropriate error messages if issues occur.

---

### Unit Testing and UI/Instrumentation Testing
I implemented both unit tests and UI/instrumentation tests to validate the functionality of key features. These tests covered authentication logic, cart and checkout behaviour, and language-switching functionality.  
<img width="2499" height="450" alt="image" src="https://github.com/user-attachments/assets/fb2dc1e6-8f57-42eb-be09-77c83a228415" />

# CarParts – Work Completed


**Adrian Donnelly (X00194620)**
---
### Backend API Development and Database Config
I developed the ASP.NET Core backend for the CarParts project. This included implementing controllers for GET, POST, PUT, and DELETE operations.

I configured the Supabase database with two tables: Vehicles and AutoParts. The Vehicles table stores make, model, year, and engine type. The AutoParts table contains part details with foreign key relationships linking parts to compatible vehicles, allowing for filtering by vehicle.
 
---

### Azure Deployment and CI/CD Pipeline
I deployed the API to Azure. This involved configuring application settings, managing connection strings, and resolving environment compatibility issues.

I implemented a GitHub Actions workflow that triggers on commits to the main branch which builds the application and deploys to Azure. The pipeline runs the tests before deployment, which has a 67% coverage.
 
---

### Supabase Config
I configured database for local and production envs. Locally, I used .NET User Secrets for credential storage. For Azure, I set connection strings as Application Settings.

---

### API Testing
I tested all endpoints to verify correct operation. This included validating GET requests for retrieving vehicle and parts data, testing POST requests for adding records, and confirming PUT and DELETE operations modified the database correctly.
 
---

### Vehicle Registration and Parts Filtering System
I implemented vehicle registration functionality allowing users to add vehicles to their account by selecting make, model, and year. The system stores this information in the Vehicles table with relationships to user accounts.

I developed the parts filtering system that uses the selected vehicle's specifications to query the database. When a user selects their vehicle (for example, a 2022 BMW X5), the system returns only compatible parts from the AutoParts table, filtering by make, model, and engine type.
 
---

### Admin Controls 
I built the admin functionality accessible through an Admin Panel in the application. This provides privileged users with four capabilities: Add Parts, Add Vehicles, Delete Parts, and Delete Vehicles.

I implemented access control to restrict these operations to admin accounts only.

---

### Internationalization
I implemented internationalization support, allowing for language switching between English and Irish. This involved creating translation files for both languages and configuring the API to serve localized content based on the selected language.

 
